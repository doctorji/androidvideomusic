package com.niuniu.videomusic.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.niuniu.videomusic.R;
import com.niuniu.videomusic.util.BitmapUtil;


/**
 * Created by Administrator on 2018/9/21 0021.
 */

public class MyImageView extends View {
    Paint paint = new Paint();
    Bitmap bitmap;

    public MyImageView(Context context) {
        this(context, null);
    }

    public MyImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        bitmap = BitmapUtil.loadBigImg(getResources(), R.drawable.ye, 300, 200);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, 0, 0, paint);
    }
}
