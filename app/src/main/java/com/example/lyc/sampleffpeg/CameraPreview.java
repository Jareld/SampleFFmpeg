package com.example.lyc.sampleffpeg;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

public class CameraPreview
        extends SurfaceView
        implements SurfaceHolder.Callback
{

    private static final String TAG = "CP";

    private SurfaceHolder       mHolder;
    private Camera              mCamera;
    private Camera.Size         mPreviewSize;
    private int                 mFrameCount;
    int num = 0;

    public static long starttime = 0;
    public static long  endtime=0;
    public  static  long numTenEndTime = 0;
    public  static  long numTenStartTime = 0;

    private MediacoEncod mMediacoEncod;

    @SuppressWarnings("deprecation")
    public CameraPreview(Context context, Camera camera ) {
        super(context);
        mFrameCount = 0;
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();

        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    public CameraPreview(Context context, Camera camera, MediacoEncod avcCodec) {
        super(context);
        mFrameCount = 0;
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();

        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mMediacoEncod = avcCodec;
    }


    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the
        // preview.
        try {
            mCamera.setPreviewDisplay(holder);
            // mCamera.setPreviewCallback( new PreviewCallback(){
            // @Override
            // public void onPreviewFrame(byte[] data, Camera camera) {
            // Log.d(CP, "Preview Callback in CameraPreview.java");
            // }
            // });
            //	mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
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
        // reformatting changes here
        Parameters param = mCamera.getParameters();
        param.setPreviewFormat(ImageFormat.NV21);
        param.setPreviewSize(1920 , 1080  );
         param.setPreviewFpsRange(30000,30000);
        mCamera.setDisplayOrientation(90);
        mCamera.setParameters(param);

        mPreviewSize = mCamera.getParameters()
                              .getPreviewSize();

        // start preview with new settings
        try {

            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    Log.d(TAG, "onPreviewFrame: 开始预览camera"+data.length +":次数"+num);


                }
            });

            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {

                    if(starttime == 0){
                        starttime = System.currentTimeMillis();
                        mMediacoEncod.handleYUVData();
                    }
                    putYUVData(data,data.length);
                    mCamera.addCallbackBuffer(data);

                }
            });

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
        private static int                        yuvqueuesize = 10;
        //待解码视频缓冲队列，静态成员！
        public static  ArrayBlockingQueue<byte[]> YUVQueue     = new ArrayBlockingQueue<byte[]>(yuvqueuesize);

        public static void putYUVData(byte[] buffer, int length) {
            if (YUVQueue.size() >= 10) {
                Log.d(TAG, "putYUVData: 放弃了一些资源" + Thread.currentThread().getName());
                YUVQueue.poll();
            }
            Log.d(TAG, "putYUVData: 正常加入到buffer里面");
            YUVQueue.add(buffer);
        }
}
