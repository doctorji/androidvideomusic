package com.niuniu.videomusic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.niuniu.videomusic.audio.AudioRecordActivity;
import com.niuniu.videomusic.camera.Camera2Activity;
import com.niuniu.videomusic.camera.CameraPlayActivity;
import com.niuniu.videomusic.imageshow.ImageShowActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.bt_image).setOnClickListener(this);
        findViewById(R.id.bt_audio).setOnClickListener(this);
        findViewById(R.id.bt_im).setOnClickListener(this);
        findViewById(R.id.bt_im2).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_image:
                startActivity(new Intent(this, ImageShowActivity.class));
                break;
            case R.id.bt_audio:
                startActivity(new Intent(this, AudioRecordActivity.class));
                break;
            case R.id.bt_im:
                startActivity(new Intent(this, CameraPlayActivity.class));
                break;
            case R.id.bt_im2:
                startActivity(new Intent(this, Camera2Activity.class));
                break;
        }
    }
}
