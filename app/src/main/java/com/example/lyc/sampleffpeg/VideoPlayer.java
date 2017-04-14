package com.example.lyc.sampleffpeg;

import android.util.Log;

import java.io.File;

/*
 *  @项目名：  SampleFFpeg 
 *  @包名：    com.example.lyc.sampleffpeg
 *  @文件名:   VideoPlayer
 *  @创建者:   LYC2
 *  @创建时间:  2017/1/13 11:23
 *  @描述：    TODO
 */
public class VideoPlayer {

    private static final String TAG ="HYVideoPlayer" ;

    static {
        Log.d(TAG, "static initializer: 开始加载");
        System.loadLibrary("videoplay");
        File file=new File("/storage/emulated/0/test.mp4");
    }

    public static native int play(Object surface);

    public static native void jni_ffmpeg_push(String intput , String output);
    public static native void jni_ffmpeg_hecv_set_configuration(String intput , String output);
    public static native void jni_ffmpeg_hecv_set_buffer(int[] buffer);


    public static native int jni_ffmpeg_setVersion();

    public static native byte[] jni_handle_nv21_to_nv12(byte[] input, int m_width, int m_height);
    public static native void sendDataFromNativeSocket(String string);
    public static native void getDataFromNativeSocket();
}
