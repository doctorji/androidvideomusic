package com.niuniu.videomusic.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.niuniu.videomusic.R;

public class CameraPlayActivity extends AppCompatActivity {
    private CameraPreview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera_play);
        // Create an instance of Camera
        // Create our Preview view and set it as the content of our activity.

        if (Camera1Util.checkCameraHardware(this)) {

            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            FoucsView foucsView = new FoucsView(this);
            foucsView.setVisibility(View.GONE);
            mPreview = new CameraPreview(this, (float) 16 / 9, foucsView);

            findViewById(R.id.button_capture).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPreview.take();
                }
            });
            findViewById(R.id.button_switch).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPreview.switchCamera();
                }
            });
            preview.addView(mPreview);
            preview.addView(foucsView);
        } else {
            Toast.makeText(this, "本手机没有可用摄像头", Toast.LENGTH_SHORT).show();
        }

    }


}
