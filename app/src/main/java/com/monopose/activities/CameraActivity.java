package com.monopose.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.monopose.BaseFullScreenActivity;
import com.monopose.R;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.ClarifaiResponse;
import clarifai2.api.request.ClarifaiRequest;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.image.ClarifaiImage;
import clarifai2.dto.model.ConceptModel;
import clarifai2.dto.model.Model;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.model.output_info.ConceptOutputInfo;
import clarifai2.dto.prediction.Concept;
import es.dmoral.toasty.Toasty;

public class CameraActivity extends BaseFullScreenActivity implements Camera2BasicFragment.OnPhotoTakenCallback {

    AppCompatEditText edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        edit = (AppCompatEditText) findViewById(R.id.train_tv);
        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, Camera2BasicFragment.newInstance())
                    .commit();
        }
    }



    String[] watch = {"watch", "time", "clock"};
    String[] pose = {"goodpose", "badpose"};
    AlertDialog alertDialog = null;
    Handler handler = new Handler();

    public void createModel(ClarifaiClient client, File file){
        String trained = edit.getText().toString();
        new Thread(() -> {

            try {
                ClarifaiResponse<List<ClarifaiInput>> inputs = client.addInputs()
                        .plus(
                                ClarifaiInput.forImage(ClarifaiImage.of(file))
                                        .withConcepts(Concept.forID(trained))
                        )
                        .executeSync();

                if (inputs.get() == null || inputs.get().size() == 0) {
                    handler.post(() -> {
                        Toast.makeText(this, "Not successful training model!", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                ClarifaiResponse<ConceptModel> clarifaiResponse = client.createModel(inputs.get().get(0).id())
                        .withOutputInfo(ConceptOutputInfo.forConcepts(
                                Concept.forID(trained)
                        ))
                        .executeSync();
                ClarifaiResponse<Model<?>> trainedModel = client.trainModel(clarifaiResponse.get().id()).executeSync();

                handler.post(() -> {
                    Toasty.success(this, "Trained!", Toast.LENGTH_SHORT, true).show();
                    edit.setText("");
                });
            }catch (Exception ex){
                handler.post(() -> {
                    Toasty.error(this, "Unable to train image size to big!", Toast.LENGTH_SHORT, true).show();
                });
            }
        }).start();
    }

    public void findPreview(File bytes) {
        String clientId = "6kBQdBwGxn22rgDWUuV6RKB-UWRR4Gg66eLLeGWo";
        String clientSecret = "8T5oaE6fLnIqoWnGarMPw-M_S6Al7Cw04oWN2eCB";

        ClarifaiClient client = new ClarifaiBuilder(clientId, clientSecret).buildSync();

        if(edit.getText().length() > 0){
            createModel(client, bytes);
        }else{
            guess(client, bytes, pose);
        }
    }
    public String badpose_model_id = "Pose";
    public String keyboard_model_id = "bacb237dfbd64731b6c9a9c8307c372b";

    public void guess(ClarifaiClient client, File bytes, String[] watch){

        client.getModelByID(badpose_model_id)
                .executeAsync(model -> model.predict().withInputs(ClarifaiInput.forImage(ClarifaiImage.of(bytes)))
                        .executeAsync(callback -> {
                            List<Concept> conceptList = (List<Concept>) callback.get(0).data();
                            List<Concept> conceptOptional = null;
                            conceptList.stream().filter(concept -> concept.name() != null)
                                    .collect(Collectors.toList());

                            for(Concept c : conceptList){
                                Log.e("NAME", c.name());
                                if(c.name().contains("badpose")){
                                    runOnUiThread(() -> {
                                        Toasty.error(CameraActivity.this,"You're pose is a bit off.",Toast.LENGTH_LONG, true).show();
                                    });
                                    break;
                                }else if(c.name().contains("goodpose")){
                                    runOnUiThread(() -> {
                                        Toasty.success(CameraActivity.this,"Good Pose!!!",Toast.LENGTH_LONG, true).show();
                                    });
                                    break;
                                }
                            }


                        },code -> {

                            Log.e("Status Code", "" + code);
                        }, e->{
                            runOnUiThread(() -> {
                                Toasty.error(CameraActivity.this,e.getMessage(),Toast.LENGTH_LONG, true).show();
                            });
                            Log.e("Error", "Error", e);
                        }), code -> {

                    Log.e("Status Code", "" + code);
                }, e -> {
                    Log.e("Error", "Error", e);

                    runOnUiThread(() -> {
                        Toasty.error(CameraActivity.this,e.getMessage(),Toast.LENGTH_LONG, true).show();
                    });
                });
    }



    public static void start(Context context) {
        ContextCompat.startActivity(context, new Intent(context, CameraActivity.class), ActivityOptionsCompat.makeBasic().toBundle());
    }

    @Override
    public void OnPhotoTaken(File photo) {
        findPreview(photo);
    }
}
