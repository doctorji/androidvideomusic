package com.niuniu.videomusic.camera.camera2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.TextureView;

import com.niuniu.videomusic.camera.FoucsView;
import com.niuniu.videomusic.camera.camera2.weidget.AutoFitTextureView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by Administrator on 2018/10/19 0019.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class TextureViewTouchEvent implements AutoFitTextureView.AutoFitTextureViewTouchEvent {
    private CameraCharacteristics mCameraCharacteristics;
    private TextureView mTextureView;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mCameraCaptureSession;
    private Handler mHandler;
    private CameraCaptureSession.CaptureCallback mPreviewSessionCallback;
    private FoucsView mFoucsView;
    private float oldDist;
    private int scaleIdex = 0;

    public TextureViewTouchEvent(CameraCharacteristics mCameraCharacteristics, TextureView mTextureView, CaptureRequest.Builder mPreviewBuilder, CameraCaptureSession mCameraCaptureSession, Handler mHandler, CameraCaptureSession.CaptureCallback mPreviewSessionCallback, FoucsView view) {
        this.mCameraCharacteristics = mCameraCharacteristics;
        this.mTextureView = mTextureView;
        this.mPreviewBuilder = mPreviewBuilder;
        this.mCameraCaptureSession = mCameraCaptureSession;
        this.mHandler = mHandler;
        this.mPreviewSessionCallback = mPreviewSessionCallback;
        this.mFoucsView = view;
    }

    @Override
    public boolean onAreaTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            handleFocusMetering(event);
            handlerFoucs(event.getX(), event.getY());
        } else {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = getFingerSpacing(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newDist = getFingerSpacing(event);
                    if (newDist > oldDist && (newDist - oldDist > 2)) {
                        scaleIdex++;
                        if (scaleIdex > 100) {
                            scaleIdex = 100;
                        }
                        handleZoom(scaleIdex);
                    } else if (newDist < oldDist && (oldDist - newDist > 2)) {
                        scaleIdex--;
                        if (scaleIdex < 0) {
                            scaleIdex = 0;
                        }
                        handleZoom(scaleIdex);
                    }
                    oldDist = newDist;
                    break;
            }
        }
        return true;
    }

    private void handleFocusMetering(MotionEvent event) {
        Rect rect = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        Size size = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);
        int areaSize = 200;
        int right = rect.right;
        int bottom = rect.bottom;
        int viewWidth = mTextureView.getWidth();
        int viewHeight = mTextureView.getHeight();
        int ll, rr;
        Rect newRect;
        int centerX = (int) event.getX();
        int centerY = (int) event.getY();
        ll = ((centerX * right) - areaSize) / viewWidth;
        rr = ((centerY * bottom) - areaSize) / viewHeight;
        int focusLeft = clamp(ll, 0, right);
        int focusBottom = clamp(rr, 0, bottom);
        Log.i("focus_position", "focusLeft--->" + focusLeft + ",,,focusTop--->" + focusBottom + ",,,focusRight--->" + (focusLeft + areaSize) + ",,,focusBottom--->" + (focusBottom + areaSize));
        newRect = new Rect(focusLeft, focusBottom, focusLeft + areaSize, focusBottom + areaSize);
        MeteringRectangle meteringRectangle = new MeteringRectangle(newRect, 500);
        MeteringRectangle[] meteringRectangleArr = {meteringRectangle};
        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, meteringRectangleArr);
        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
        updatePreview();
    }

    /**
     * 触摸对焦的计算
     *
     * @param x
     * @param min
     * @param max
     * @return
     */
    private int clamp(int x, int min, int max) {
        if (x < min) {
            return min;
        } else if (x > max) {
            return max;
        } else {
            return x;
        }
    }

    /**
     * 更新预览
     */
    private void updatePreview() {
        try {
            mCameraCaptureSession.setRepeatingRequest(mPreviewBuilder.build(), mPreviewSessionCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("updatePreview", "ExceptionExceptionException");
        }
    }

    public void handlerFoucs(float x, float y) {
        mFoucsView.setVisibility(VISIBLE);
        mFoucsView.setX(x - mFoucsView.getWidth() / 2);
        mFoucsView.setY(y - mFoucsView.getHeight() / 2);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mFoucsView, "scaleX", 1, 0.6f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mFoucsView, "scaleY", 1, 0.6f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mFoucsView, "alpha", 1f, 0.4f, 1f, 0.4f, 1f, 0.4f, 1f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY).before(alpha);
        animSet.setDuration(400);
        animSet.start();
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mFoucsView.setVisibility(GONE);
            }
        });
    }

    /**
     * 获取手指滑动的点的位置的相对坐标系的长度
     *
     * @param event
     * @return
     */
    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void handleZoom(int i) {
        Rect rect = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        int radio = mCameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM).intValue() / 2;
        int realRadio = mCameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM).intValue();
        int centerX = rect.centerX();
        int centerY = rect.centerY();
        int minMidth = (rect.right - ((i * centerX) / 100 / radio) - 1) - ((i * centerX / radio) / 100 + 8);
        int minHeight = (rect.bottom - ((i * centerY) / 100 / radio) - 1) - ((i * centerY / radio) / 100 + 16);
        if (minMidth < rect.right / realRadio || minHeight < rect.bottom / realRadio) {
            Log.i("sb_zoom", "sb_zoomsb_zoomsb_zoom");
            return;
        }
//                    Rect newRect = new Rect(20, 20, rect.right - ((i * centerX) / 100 / radio) - 1, rect.bottom - ((i * centerY) / 100 / radio) - 1);
//                    Log.i("sb_zoom", "left--->" + "20" + ",,,top--->" + "20" + ",,,right--->" + (rect.right - ((i * centerX) / 100 / radio) - 1) + ",,,bottom--->" + (rect.bottom - ((i * centerY) / 100 / radio) - 1));
        Rect newRect = new Rect((i * centerX / radio) / 100 + 40, (i * centerY / radio) / 100 + 40, rect.right - ((i * centerX) / 100 / radio) - 1, rect.bottom - ((i * centerY) / 100 / radio) - 1);
        Log.i("sb_zoom", "left--->" + ((i * centerX / radio) / 100 + 8) + ",,,top--->" + ((i * centerY / radio) / 100 + 16) + ",,,right--->" + (rect.right - ((i * centerX) / 100 / radio) - 1) + ",,,bottom--->" + (rect.bottom - ((i * centerY) / 100 / radio) - 1));
        mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, newRect);
        updatePreview();
    }

}
