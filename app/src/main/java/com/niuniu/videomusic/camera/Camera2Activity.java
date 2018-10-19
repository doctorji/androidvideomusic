package com.niuniu.videomusic.camera;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.niuniu.videomusic.R;
import com.niuniu.videomusic.camera.camera2.Camera2Preview;
import com.niuniu.videomusic.camera.camera2.weidget.AutoFitTextureView;

public class Camera2Activity extends AppCompatActivity {

    private Camera2Preview camera2Preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera2);
        AutoFitTextureView autoFitTextureView = new AutoFitTextureView(this);
        FoucsView foucsView = new FoucsView(this);
        foucsView.setVisibility(View.GONE);
        camera2Preview = new Camera2Preview(this, autoFitTextureView,foucsView);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        findViewById(R.id.button_switch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera2Preview.switchCamera();
            }
        });
        findViewById(R.id.button_capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera2Preview.takePicture();
            }
        });
        preview.addView(autoFitTextureView);
        preview.addView(foucsView);
    }
}
