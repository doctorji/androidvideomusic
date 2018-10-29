package com.niuniu.videomusic.camera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.niuniu.videomusic.camera.encoder.MediaMuxerThread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static android.graphics.Bitmap.createBitmap;

/**
 * Created by Administrator on 2018/10/9 0009.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener, Camera.PreviewCallback {
    private Context cxt;
    private FoucsView mFoucsView;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private int SELECTED_CAMERA = -1; //选中的摄像头的id
    private int CAMERA_POST_POSITION = -1;//后置摄像头id
    private int CAMERA_FRONT_POSITION = -1;//前置摄像头的id
    private boolean isPreviewing = false;  //是否在预览中的标志
    private float screenProp = 0f;
    private int cameraAngle = 90;//摄像头角度   默认为90度
    private float oldDist = 1f;
    ; //缩放的区域
    private boolean canFocus = true;
    private SensorManager sm;
    private double angle; //照片需要旋转的角度
    private int nowAngle;//照片的角度
    private boolean isRecording = false;

    public CameraPreview(Context context, float screenProp, FoucsView foucsView) {
        super(context);
        this.screenProp = screenProp;
        this.mFoucsView = foucsView;
        this.cxt = context;

        getAvailableCameras();
        SELECTED_CAMERA = CAMERA_POST_POSITION;
        cameraAngle = Camera1Util.getCameraDisplayOrientation(context,
                SELECTED_CAMERA);//获取摄像头的角度
        openCamera(SELECTED_CAMERA);
        registerSensorManager(cxt);
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        doStartPreview();

    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        stopVideo();
        doDestroyCamera();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();// 获得相机参数
            Camera.Size s = parameters.getPictureSize();
           /* Camera.Size s = Camera1Util.getPreviewSize(parameters
                    .getSupportedPreviewSizes(), 1200, screenProp);*/
            double w = s.width;
            double h = s.height;
            Log.e(TAG, s.width + "aaa" + s.height + "bbbb" + width + "ccc" + height);
            if (width > height) {
                setLayoutParams(new FrameLayout.LayoutParams((int) (height * (h / w)), height));
            } else {
                setLayoutParams(new FrameLayout.LayoutParams(width, (int) (width * (w / h))));
            }
            parameters.setPreviewSize(width, height); // 设置预览图像大小
        } else {
            openCamera(SELECTED_CAMERA);
            mCamera.setDisplayOrientation(cameraAngle);//浏览角度
        }

        // start preview with new settings*/
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    public void take() {
        if (mCamera == null) {
            return;
        }
        isPreviewing = false;
        switch (cameraAngle) {
            case 90:
                nowAngle = Math.abs((int) angle + cameraAngle) % 360;
                break;
            case 270:
                nowAngle = Math.abs(cameraAngle - (int) angle);
                break;
        }
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Matrix matrix = new Matrix();
                        if (SELECTED_CAMERA == CAMERA_POST_POSITION) {
                            matrix.setRotate(nowAngle);
                        } else if (SELECTED_CAMERA == CAMERA_FRONT_POSITION) {
                            matrix.setRotate(360 - nowAngle);
                            matrix.postScale(-1, 1);
                        }

                        bitmap = createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        FileOutputStream out = null;
                        try {
                            File externalStorageDirectory = Environment.getExternalStorageDirectory();
                            File file = new File(externalStorageDirectory,
                                    "picture.jpg");
                            out = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            out.flush();
                            out.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (out != null) {
                                try {
                                    out.close();
                                    doStartPreview();
                                } catch (IOException e) {
                                    // Ignore
                                }
                            }
                        }
                    }
                }).start();

            }
        });

    }

    /**
     * 获取可以支持的摄像头的个数
     * 区分前后摄像头的id
     */
    private void getAvailableCameras() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        int cameraNum = Camera.getNumberOfCameras();
        for (int i = 0; i < cameraNum; i++) {
            Camera.getCameraInfo(i, info);
            switch (info.facing) {
                case Camera.CameraInfo.CAMERA_FACING_FRONT:
                    CAMERA_FRONT_POSITION = info.facing;
                    break;
                case Camera.CameraInfo.CAMERA_FACING_BACK:
                    CAMERA_POST_POSITION = info.facing;
                    break;
            }
        }
    }

    /**
     * 切换摄像头
     */
    public synchronized void switchCamera() {
        if (SELECTED_CAMERA == CAMERA_POST_POSITION) {
            SELECTED_CAMERA = CAMERA_FRONT_POSITION;
        } else {
            SELECTED_CAMERA = CAMERA_POST_POSITION;
        }
        doDestroyCamera();
        openCamera(SELECTED_CAMERA);
        if (Build.VERSION.SDK_INT > 17 && this.mCamera != null) {
            try {
                this.mCamera.enableShutterSound(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        doStartPreview();
    }

    /**
     * 切换闪光灯、
     *
     * @param flashMode
     */
    public void setFlashMode(String flashMode) {
        if (mCamera == null)
            return;
        Camera.Parameters params = mCamera.getParameters();
        params.setFlashMode(flashMode);
        mCamera.setParameters(params);
    }

    /**
     * 开启摄像头
     *
     * @param id
     */
    private synchronized void openCamera(int id) {
        try {
            this.mCamera = Camera.open(id);
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        if (Build.VERSION.SDK_INT > 17 && this.mCamera != null) {
            try {
                this.mCamera.enableShutterSound(false);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("CJT", "enable shutter sound faild");
            }
        }
    }

    /**
     * 销毁Camera
     */
    void doDestroyCamera() {
        if (null != mCamera) {
            try {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                //这句要在stopPreview后执行，不然会卡顿或者花屏
                mCamera.setPreviewDisplay(null);
                //mHolder = null;
                isPreviewing = false;
                mCamera.release();
                mCamera = null;
                unregisterSensorManager(cxt);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * doStartPreview
     */
    public void doStartPreview() {
        if (isPreviewing) {
            return;
        }
        if (mHolder == null) {
            return;
        }
        if (mCamera != null) {
            try {
                Camera.Parameters mParams = mCamera.getParameters();
                Camera.Size pictureSize = Camera1Util.getPictureSize(mParams
                        .getSupportedPictureSizes(), 1500, screenProp);

                mParams.setPictureSize(pictureSize.width, pictureSize.height);
                if (Camera1Util.isSupportedFocusMode(
                        mParams.getSupportedFocusModes(),
                        Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }
                if (Camera1Util.isSupportedPictureFormats(mParams.getSupportedPictureFormats(),
                        ImageFormat.JPEG)) {
                    mParams.setPictureFormat(ImageFormat.JPEG);
                    mParams.setJpegQuality(100);
                }
                mParams.setPreviewFormat(ImageFormat.NV21);
                mCamera.setParameters(mParams);
                mCamera.setPreviewDisplay(mHolder);  //SurfaceView
                mCamera.setDisplayOrientation(cameraAngle);//浏览角度
                mCamera.setPreviewCallback(this); //每一帧回调
                mCamera.startPreview();//启动浏览
                mCamera.cancelAutoFocus();
                isPreviewing = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!canFocus) {
            return false;
        }
        canFocus = false;
        if (event.getPointerCount() == 1) {
            handleFocusMetering(event, mCamera);
            handlerFoucs(event.getX(), event.getY());
        } else {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = getFingerSpacing(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newDist = getFingerSpacing(event);
                    if (newDist > oldDist && (newDist - oldDist > 5)) {
                        handleZoom(true, mCamera);
                    } else if (newDist < oldDist && (oldDist - newDist > 5)) {
                        handleZoom(false, mCamera);
                    }
                    oldDist = newDist;
                    break;
            }
        }
        return true;
    }

    private void handleZoom(boolean isZoomIn, Camera camera) {
        Camera.Parameters params = camera.getParameters();
        if (params.isZoomSupported()) {
            int maxZoom = params.getMaxZoom();
            int zoom = params.getZoom();
            if (isZoomIn && zoom < maxZoom) {
                zoom++;
            } else if (zoom > 0) {
                zoom--;
            }
            params.setZoom(zoom);
            camera.setParameters(params);
        } else {
            Log.i(TAG, "zoom not supported");
        }
    }

    private void handleFocusMetering(MotionEvent event, Camera camera) {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f, viewWidth, viewHeight);
        Rect meteringRect = calculateTapArea(event.getX(), event.getY(), 1.5f, viewWidth, viewHeight);

        camera.cancelAutoFocus();
        Camera.Parameters params = camera.getParameters();
        if (params.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(focusRect, 800));
            params.setFocusAreas(focusAreas);
        } else {
            Log.i(TAG, "focus areas not supported");
        }
        if (params.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreas = new ArrayList<>();
            meteringAreas.add(new Camera.Area(meteringRect, 800));
            params.setMeteringAreas(meteringAreas);
        } else {
            Log.i(TAG, "metering areas not supported");
        }
        if (Camera1Util.isSupportedFocusMode(
                params.getSupportedFocusModes(),
                Camera.Parameters.FOCUS_MODE_AUTO)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        camera.setParameters(params);

        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    canFocus = true;
                    camera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
                }
            }
        });
    }

    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 相机的对焦坐标和屏幕坐标的转换
     *
     * @param x
     * @param y
     * @param coefficient
     * @param width
     * @param height
     * @return
     */
    private Rect calculateTapArea(float x, float y, float coefficient, int width, int height) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerX = (int) (x / width * 2000 - 1000);
        int centerY = (int) (y / height * 2000 - 1000);

        int halfAreaSize = areaSize / 2;
        RectF rectF = new RectF(clamp(centerX - halfAreaSize, -1000, 1000)
                , clamp(centerY - halfAreaSize, -1000, 1000)
                , clamp(centerX + halfAreaSize, -1000, 1000)
                , clamp(centerY + halfAreaSize, -1000, 1000));
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
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

    void registerSensorManager(Context context) {
        if (sm == null) {
            sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager
                .SENSOR_DELAY_NORMAL);
    }

    void unregisterSensorManager(Context context) {
        if (sm == null) {
            sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
        sm.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (Sensor.TYPE_ACCELEROMETER != event.sensor.getType()) {
            return;
        }
        float[] values = event.values;
        angle = Camera1Util.getSensorAngle(values[0], values[1]);
      /*  float ax = values[0];
        float ay = values[1];

        double g = Math.sqrt(ax * ax + ay * ay);
        double cos = ay / g;
        if (cos > 1) {
            cos = 1;
        } else if (cos < -1) {
            cos = -1;
        }
        double rad = Math.acos(cos);
        if (ax < 0) {
            rad = 2 * Math.PI - rad;
        }
        WindowManager wm=(WindowManager)cxt.getSystemService(Context.WINDOW_SERVICE);
        int uiRot = wm.getDefaultDisplay().getRotation();
        double uiRad = Math.PI / 2 * uiRot;
        rad -= uiRad;
       angle = rad;*/
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        MediaMuxerThread.addVideoFrameData(bytes);
    }

    /**
     * 开始录像
     */
    public void startVideo() {
        MediaMuxerThread.startMuxer();
    }

    public void stopVideo() {
        MediaMuxerThread.stopMuxer();
    }
}
