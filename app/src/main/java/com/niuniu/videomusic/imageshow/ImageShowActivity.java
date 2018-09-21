package com.niuniu.videomusic.imageshow;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.niuniu.videomusic.R;
import com.niuniu.videomusic.util.BitmapUtil;

/**
 * Created by Administrator on 2018/9/21 0021.
 * 音视频学习的第一步展示图片
 */

public class ImageShowActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mIvShow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageshow);
        findViewById(R.id.bt_image_view).setOnClickListener(this);
        mIvShow = (ImageView) findViewById(R.id.iv_show);

        /*  surfaceview具体的用法看这篇博客  https://blog.csdn.net/android_cmos/article/details/68955134*/
        SurfaceView sv = (SurfaceView) findViewById(R.id.sv);
        sv.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (holder == null) {
                    return;
                }

                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.STROKE);

                Bitmap bitmap =BitmapUtil.loadBigImg(getResources(), R.drawable.ye, 300, 200);  // 获取bitmap
                Canvas canvas = holder.lockCanvas();  // 先锁定当前surfaceView的画布
                canvas.drawBitmap(bitmap, 0, 0, paint); //执行绘制操作
                holder.unlockCanvasAndPost(canvas); // 解除锁定并显示在界面上
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_image_view:
                //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ye); //默认采用的是Bitmap.Config.ARGB_8888

             /*
                 https://blog.csdn.net/showdy/article/details/54378637  参考这篇博客
                inpreferredConfig参数有四个值:
                ALPHA_8: 每个像素用占8位,存储的是图片的透明值,占1个字节
                RGB_565:每个像素用占16位,分别为5-R,6-G,5-B通道,占2个字节
                ARGB-4444:每个像素占16位,即每个通道用4位表示,占2个字节
                ARGB_8888:每个像素占32位,每个通道用8位表示,占4个字节*/
              /*  BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig= Bitmap.Config.ARGB_8888;
                Bitmap bitmap =BitmapFactory.decodeResource(getResources(), R.drawable.ye,options);*/
                mIvShow.setImageBitmap(BitmapUtil.loadBigImg(getResources(), R.drawable.ye, 300, 200));
                break;
        }
    }
}
