package com.example.lyc.sampleffpeg;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.File;

public class MainActivity
        extends AppCompatActivity
        implements SurfaceHolder.Callback, View.OnClickListener
{


    private static final String TAG                       = "MainActivity";
    private static final int    MY_WRITE_EXTERNAL_STORAGE = 1;
    private SurfaceHolder mSurfaceHolder;
    private Button        mBtn_play;
    private TextView      mTv;
    private Button        mBtn_push;
    private Button        mBtn_ffmpeg_hevc;
    private Camera        mCamera;
    private CameraPreview mPreview;
    private FrameLayout   mFl_surface_view;
    private MediacoEncod  mAvcCodec;

    int width = 1920;

    int height = 1080;

    int framerate = 30;

    int biterate = 8500 * 1000;
    private MediaDecod mPlayerThread;
    private boolean mIsPreview = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checKPermission();
        initView();
        initData();
        initEvent();
        new Thread(){
            @Override
            public void run() {

                VideoPlayer.getDataFromNativeSocket();

            }
        }.start();
    }

    private void checKPermission() {

        //看返回的内容    如果是返回PackageManager.PERMISSION_GRANTED就是已经申请过权限了
        //               如果是返回PackageManager.PERMISSION_DENIED那么就是需要申请权限
        Log.d(TAG, "checKStoragePermission: ");
        if (ContextCompat.checkSelfPermission(this,
                                              Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                                              new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                           Manifest.permission.CAMERA},
                                              MY_WRITE_EXTERNAL_STORAGE);

        } else {

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //处理回调
        switch (requestCode) {
            case MY_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    //这里是不允许的时候

                }
                break;

        }

    }

    private void initEvent() {
        mBtn_play.setOnClickListener(this);
        mBtn_push.setOnClickListener(this);
        mBtn_ffmpeg_hevc.setOnClickListener(this);


        //进行一个任务的接受
//        new Thread(){
//            @Override
//            public void run() {
//                try {
//                    LocalServerSocket localServerSocket = new LocalServerSocket("Jareld");
//                    Log.d(TAG, "onClick: 接受之前");
//                    LocalSocket accept = localServerSocket.accept();
//                    Log.d(TAG, "onClick: 接受之后");
//                    InputStream inputStream = accept.getInputStream();
//
//                    byte[] bys = new byte[1024 * 100];
//                    int len ;
//                    while((len = inputStream.read(bys)) !=  -3){
//
//                        String string = new String(bys , 0 , len);
//                        Log.d(TAG, "onClick: jareld "+string);
//                    }
//                    Log.d(TAG, "run: jareld 结束");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();


    }

    private void initData() {
        mTv.setText("测试");
    }

    private void initView() {

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mFl_surface_view = (FrameLayout) findViewById(R.id.fl_sufuce_view);

        mTv = (TextView) findViewById(R.id.sample_text);
        mBtn_play = (Button) findViewById(R.id.play);
        mBtn_push = (Button) findViewById(R.id.jni_push);
        mBtn_ffmpeg_hevc = (Button) findViewById(R.id.ffmpeg_hevc);
        //创建AvEncoder对象
        mAvcCodec = new MediacoEncod(width, height, framerate, biterate);
        //启动编码线程
        // mAvcCodec.StartEncoderThread();

    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    @Override
    public void surfaceCreated(SurfaceHolder holder) {


    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick:  ");
        switch (v.getId()) {

            case R.id.play:
                //
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: 进行了播放");

                        VideoPlayer.play(mSurfaceHolder.getSurface());
                    }
                }).start();
                //                if (mPlayerThread == null) {
                //                    mPlayerThread = new MediaDecod(mSurfaceHolder.getSurface());
                //                    mPlayerThread.start();
                //                }
                break;
            case R.id.jni_push:
                VideoPlayer.jni_ffmpeg_push("/storage/emulated/0/test2.flv",
                                            "rtmp://192.168.1.116/live/Jareld");
                break;
            case R.id.ffmpeg_hevc:
              //  int i = VideoPlayer.jni_ffmpeg_setVersion();
//                Log.d(TAG, "onClick: 进行预览");
//                if (mIsPreview) {
//                    mCamera.stopPreview();
//                    mIsPreview = false;
//                } else {
//                    mCamera.startPreview();
//                    mIsPreview = true;
//
                VideoPlayer.sendDataFromNativeSocket("from socket");



                //                if(i == -1){
                //                    Log.d(TAG, "onClick: 设置参数失败");
                //                }
                //                mCamera.startPreview();
                //                Log.d(TAG, "onClick: 开始预览");
                File file = new File("/storage/emulated/0/bigbuckbunny_480x272.h265");


                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCamera = Camera.open(0);

        mPreview = new CameraPreview(this, mCamera, mAvcCodec);

        mFl_surface_view.addView(mPreview);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCamera != null) {
            mFl_surface_view.removeView(mPreview);
            mPreview = null;
            mCamera.release();
            mCamera = null;
        }
    }

    //    private static int yuvqueuesize = 10;
    //    //待解码视频缓冲队列，静态成员！
    //    public static ArrayBlockingQueue<byte[]> YUVQueue = new ArrayBlockingQueue<byte[]>(yuvqueuesize);
    //
    //    public static void putYUVData(byte[] buffer, int length) {
    //        if (YUVQueue.size() >= 10) {
    //
    //            Log.d(TAG, "putYUVData: 放弃了一些资源" + Thread.currentThread().getName());
    //            YUVQueue.poll();
    //        }
    //        Log.d(TAG, "putYUVData: 正常加入到buffer里面");
    //        YUVQueue.add(buffer);
    //    }


}
