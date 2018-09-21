package com.niuniu.videomusic.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.niuniu.videomusic.R;

/**
 * Created by Administrator on 2018/9/21 0021.
 */

public class BitmapUtil {
    public static Bitmap loadBigImg(Resources res,int dr, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//获取图片的信息
        BitmapFactory.decodeResource(res,dr, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);//获取缩放的比例
        options.inJustDecodeBounds = false;//设置可以获取bitmap
        return BitmapFactory.decodeResource(res,dr, options);
    }


    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


}
