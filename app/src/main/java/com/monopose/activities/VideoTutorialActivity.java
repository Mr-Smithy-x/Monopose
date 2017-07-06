package com.monopose.activities;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.configuration.PlayerConfig;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;
import com.monopose.R;
import com.monopose.databinding.ActivityVideoTutorialBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class VideoTutorialActivity extends AppCompatActivity {


    ActivityVideoTutorialBinding binding;
    private float x1;
    private float x2;
    static final int MIN_DISTANCE = 150;
    private JWPlayerView jwPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_tutorial);
        binding.clarifaiButton.setOnClickListener(v -> CameraActivity.start(VideoTutorialActivity.this));

        PlayerConfig.Builder playerConfig = new PlayerConfig.Builder()
                .preload(true)
                .autostart(true)
                .repeat(true)
                .controls(false);
        String videoUrl = "http://content.jwplatform.com/videos/95ZU1It9-BUfnmF8A.mp4";
        try {
            JSONObject jsonObject = new JSONObject(json);
            playerConfig.file(videoUrl = jsonObject.getString("video"));
            playerConfig.image(jsonObject.getString("picture"));
            binding.title.setText(jsonObject.getString("title"));
            binding.description.setText(jsonObject.getString("description"));
        } catch (JSONException e) {
            e.printStackTrace();
             playerConfig.file(videoUrl);

        }
        // Create a PlaylistItem


        jwPlayer = new JWPlayerView(this, playerConfig.build());
        final PlaylistItem video = new PlaylistItem(videoUrl);
        binding.jwContainer.addView(jwPlayer);

        jwPlayer.addOnCompleteListener(() -> {
            jwPlayer.load(video);
            jwPlayer.play();
        });
        jwPlayer.play();
    }

    @Override
    protected void onResume() {
        super.onResume();
        jwPlayer.onResume();
    }

    @Override
    protected void onPause() {
        jwPlayer.onPause();
        super.onPause();
    }

    private void showPosturez(boolean show, View view) {
        float[] showView = {0, 1};
        float[] hideVide = {1, 0};
        if (!show && Double.valueOf(view.getAlpha()).intValue() == 0) {
            return;
        } else if (show && Double.valueOf(view.getAlpha()).intValue() == 1) {
            return;
        }
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(show ? showView : hideVide);
        valueAnimator.setDuration(500);
        valueAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            view.setAlpha(value);
        });
        valueAnimator.start();
    }


    public static void start(Context context) {
        ContextCompat.startActivity(context, new Intent(context, VideoTutorialActivity.class), ActivityOptionsCompat.makeBasic().toBundle());
    }


    String json = "{\n" +
            "    \"title\": \"Sqauts\",\n" +
            "    \"description\": \"Feet are placed slightly wide than shoulder width apart, bend knees and go as low as possible, and return to starting position. Slow controlled movements are best.\",\n" +
            "    \"picture\":\"http://content.jwplatform.com/thumbs/95ZU1It9-480.jpg\",\n" +
            "    \"video\":\"http://content.jwplatform.com/videos/95ZU1It9-BUfnmF8A.mp4\",\n" +
            "    \"tags\":[\"goodpose\", \"badpose\"],\n" +
            "    \"model\": \"bacb237dfbd64731b6c9a9c8307c372b\"\n" +
            "}\n";

    @Override
    protected void onDestroy() {
        jwPlayer.onDestroy();
        binding.unbind();
        super.onDestroy();
    }
}
