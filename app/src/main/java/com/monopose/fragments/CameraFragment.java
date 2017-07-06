package com.monopose.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.longtailvideo.jwplayer.JWPlayerFragment;
import com.longtailvideo.jwplayer.JWPlayerView;
import com.monopose.R;
import com.monopose.activities.VideoTutorialActivity;
import com.monopose.databinding.FragmentCameraBinding;

import java.util.List;

public class CameraFragment extends BaseFragment {


    public CameraFragment() {
        // Required empty public constructor
    }

    public static CameraFragment newInstance() {
        CameraFragment fragment = new CameraFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }


    FragmentCameraBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return (binding = FragmentCameraBinding.inflate(inflater)).getRoot();
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.barcodeScanner.setRotationX(0.5f);
        binding.barcodeScanner.decodeContinuous(callback);
        binding.barcodeScanner.setStatusText("");
    }

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                VideoTutorialActivity.start(getActivity());
            }
            //Do something with code result
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };


    @Override
    public void onPause() {
        binding.barcodeScanner.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        binding.barcodeScanner.resume();
        super.onResume();
    }

    public void requestCameraPermission(final Context context, final String permission) {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(permission)) {
                AlertDialog a = new AlertDialog.Builder(context)
                        .setTitle("We need permission to access your camera!")
                        .setMessage("We need to access your camera, plz can has access?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestCameraPermission(context, permission);
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CODE.CAMERA);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        requestCameraPermission(context, Manifest.permission.CAMERA);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
