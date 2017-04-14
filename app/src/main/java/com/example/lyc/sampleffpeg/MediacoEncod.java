package com.example.lyc.sampleffpeg;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static android.media.MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
import static android.media.MediaCodec.BUFFER_FLAG_KEY_FRAME;

/*
 *  @项目名：  SampleFFpeg 
 *  @包名：    com.example.lyc.sampleffpeg
 *  @文件名:   MediacoEncod
 *  @创建者:   LYC2
 *  @创建时间:  2017/3/2 20:54
 *  @描述：    TODO
 */
public class MediacoEncod {

    private final static String TAG        = "MeidaCodec";
    private static final int    ONEMIAOGUO = 1000;

    private       int TIMEOUT_USEC = 12000;
    public static int handleCishu  = 0;

    private MediaCodec mediaCodec;
    private int        m_width;
    private int        m_height;
    private int        m_framerate;
    public static boolean isNumTen = false;
    public byte[] configbyte;
    private int numTenNum = 0;
    private byte[] mKeyframe;
    private byte[] mData;

    public MediacoEncod(int width, int height, int framerate, int bitrate) {

        m_width = width;
        m_height = height;
        m_framerate = framerate;
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                               MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 5);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        try {
            mediaCodec = MediaCodec.createEncoderByType("video/avc");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //配置编码器参数
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        //启动编码器
        mediaCodec.start();
        //创建保存编码后数据的文件
        createfile();
        mHandler.sendEmptyMessageDelayed(ONEMIAOGUO, 2000);

    }

    boolean isOneMiaoGuoqu = false;
    Handler mHandler       = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ONEMIAOGUO:
                    isOneMiaoGuoqu = true;
                    mHandler.sendEmptyMessageDelayed(ONEMIAOGUO, 1000);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private static String path = Environment.getExternalStorageDirectory()
                                            .getAbsolutePath() + "/test1.h264";
    private BufferedOutputStream outputStream;

    private void createfile() {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void StopEncoder() {
        try {
            mediaCodec.stop();
            mediaCodec.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isRuning = true;

    public void StopThread() {
        isRuning = false;
        try {
            StopEncoder();
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long pts           = 0;
    private long generateIndex = 0;
    private int  flagNum       = 0;
    private int  qudeShujuNum  = 0;
    //    public void StartEncoderThread(){
    //        Thread EncoderThread = new Thread(new Runnable() {
    //
    //            @Override
    //            public void run() {
    //                isRuning = true;
    //                byte[] input = null;
    //                long pts =  0;
    //                long generateIndex = 0;
    //                int flagNum = 0;
    //                int qudeShujuNum = 0 ;
    //                while (isRuning) {
    //
    //                   // Log.d(TAG, "run: jinru dao run");
    //                    //访问MainActivity用来缓冲待解码数据的队列
    //                    if (MainActivity.YUVQueue.size() >0){
    //                    if(ifFirst){
    //                        ifFirst = false;
    //                    first_click =SystemClock.currentThreadTimeMillis();
    //                    }else{
    //                        second_click =SystemClock.currentThreadTimeMillis();
    //                        Log.d(TAG, "run: 两次去buff的时间"+(second_click - first_click) +(Thread.currentThread().getName()));
    //                        first_click = second_click;
    //                    }
    //                        long startime = SystemClock.currentThreadTimeMillis();
    //
    //                        //从缓冲队列中取出一帧
    //                        input = MainActivity.YUVQueue.poll();
    //                       byte[] yuv420sp = new byte[m_width*m_height*3/2];
    ////                        //把待编码的视频帧转换为YUV420格式
    //                    input =  VideoPlayer.jni_handle_nv21_to_nv12(input,m_width,m_height);
    //
    ////                         //NV21ToNV12(input,yuv420sp,m_width,m_height);
    //                        long endtime = SystemClock.currentThreadTimeMillis();
    //
    //                        Log.d(TAG, "run: 处理一个NV21--NV12的时间" + (endtime - startime) +"endtime " + endtime + "starttime" + startime);
    //                     //   input = yuv420sp;
    //                        qudeShujuNum++;
    //                        Log.d(TAG, "run: 取得数据的次数qudeShujuNum " +qudeShujuNum);
    //                    }
    //
    //                    if (input != null) {
    //                        long startime = SystemClock.currentThreadTimeMillis();
    //                        try {
    //                            long startMs = System.currentTimeMillis();
    //                            //编码器输入缓冲区
    //                            ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
    //                            //编码器输出缓冲区
    //                            ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
    //                            int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
    //                            if (inputBufferIndex >= 0) {
    //                                pts = computePresentationTime(generateIndex);
    //                                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
    //                                inputBuffer.clear();
    //                                //把转换后的YUV420格式的视频帧放到编码器输入缓冲区中
    //                                inputBuffer.put(input);
    //                                mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, pts, 0);
    //                                generateIndex += 1;
    //                            }
    //
    //                            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
    //                            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
    //                            while (outputBufferIndex >= 0) {
    //                                //Log.i("AvcEncoder", "Get H264 Buffer Success! flag = "+bufferInfo.flags+",pts = "+bufferInfo.presentationTimeUs+"");
    //                                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
    //                                byte[] outData = new byte[bufferInfo.size];
    //                                outputBuffer.get(outData);
    //                                if(bufferInfo.flags == BUFFER_FLAG_CODEC_CONFIG){
    //                                    flagNum ++;
    //                                    configbyte = new byte[bufferInfo.size];
    //                                    configbyte = outData;
    //                                    Log.d(TAG, "run: flagNum" + flagNum);
    //
    //                                }else if(bufferInfo.flags == BUFFER_FLAG_KEY_FRAME){
    //                                    num++;
    //                                    byte[] keyframe = new byte[bufferInfo.size + configbyte.length];
    //                                    System.arraycopy(configbyte, 0, keyframe, 0, configbyte.length);
    //                                    //把编码后的视频帧从编码器输出缓冲区中拷贝出来
    //                                    System.arraycopy(outData, 0, keyframe, configbyte.length, outData.length);
    //                                    outputStream.write(keyframe, 0, keyframe.length);
    //                                    outputStream.flush();
    //                                    Log.d(TAG, "run: 写入到文件中"+num +"长度" + keyframe.length);
    //                                }else{
    //                                    num++;
    //                                    //写到文件中
    //                                    outputStream.write(outData, 0, outData.length);
    //                                    outputStream.flush();
    //                                    Log.d(TAG, "run: 写入到文件中"+num + "lenght " + outData.length);
    //                                }
    //
    //                                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
    //                                outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
    //                            }
    //
    //                        } catch (Throwable t) {
    //                            t.printStackTrace();
    //                        }
    //                        long endtime = SystemClock.currentThreadTimeMillis();
    //
    //                        Log.d(TAG, "run: input != null" + (endtime - startime));
    //                    } else {
    //                        Log.d(TAG, "run: 取不到数据了" +MainActivity.YUVQueue.size());
    ////                        try {
    ////                            Log.d(TAG, "run: 休眠了？");
    //////                            Thread.sleep(500);
    ////                        } catch (InterruptedException e) {
    ////                            e.printStackTrace();
    ////                        }
    //                    }
    //                }
    //            }
    //        });
    //        EncoderThread.setPriority(Thread.MAX_PRIORITY);
    //        EncoderThread.start();
    //
    //    }
    private static byte[] input;
    int  iPrame          = 0;
    int  otherPrame      = 0;
    long handleStartTime = 0;
    long handleEndTime   = 0;
    byte a1;
    byte a2;
    byte a3;
    byte a4;
    byte a5         = 0;
    int  daoNowTime = 0;
    byte[] copyByte;
    public static boolean isHandle = false;
    private       boolean isFirst  = true;

    Thread mEncoderThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (isRuning) {
                mData = CameraPreview.YUVQueue.poll();

                if (mData == null) {
                    Log.d(TAG, "run: 获取的data为null");
                    SystemClock.sleep(50);
                    continue;
                }
                input = VideoPlayer.jni_handle_nv21_to_nv12(mData, m_width, m_height);
                Log.d(TAG, "run: 看input是不是null" + (input == null));
                if (input != null) {
                    long startime = System.currentTimeMillis();
                    try {
                        Log.d(TAG, "handleYUVData: 进入到这里面");
                        //编码器输入缓冲区
                        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
                        //编码器输出缓冲区
                        ByteBuffer[] outputBuffers    = mediaCodec.getOutputBuffers();
                        int          inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
                        if (inputBufferIndex >= 0) {
                            // pts = computePresentationTime(generateIndex);
                            pts = System.nanoTime();
                            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                            inputBuffer.clear();
                            //把转换后的YUV420格式的视频帧放到编码器输入缓冲区中
                            inputBuffer.put(input);
                            mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, pts, 0);
                            generateIndex += 1;
                        }

                        MediaCodec.BufferInfo bufferInfo        = new MediaCodec.BufferInfo();
                        int                   outputBufferIndex = mediaCodec.dequeueOutputBuffer(
                                bufferInfo,
                                0);
                        while (outputBufferIndex >= 0) {
                            //Log.i("AvcEncoder", "Get H264 Buffer Success! flag = "+bufferInfo.flags+",pts = "+bufferInfo.presentationTimeUs+"");

                            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                            byte[]     outData      = new byte[bufferInfo.size];
                            outputBuffer.get(outData);
                            Log.d(TAG, "handleYUVData: 进入到这里面1");

                            if (bufferInfo.flags == BUFFER_FLAG_CODEC_CONFIG) {
                                configbyte = new byte[bufferInfo.size];
                                configbyte = outData;
                            } else if (bufferInfo.flags == BUFFER_FLAG_KEY_FRAME) {
                                handleCishu++;
                                mKeyframe = new byte[bufferInfo.size + configbyte.length];
                                System.arraycopy(configbyte, 0, mKeyframe, 0, configbyte.length);
                                //把编码后的视频帧从编码器输出缓冲区中拷贝出来
                                System.arraycopy(outData,
                                                 0,
                                                 mKeyframe,
                                                 configbyte.length,
                                                 outData.length);
                                if (handleCishu == 10) {
                                    h264Data.add(mKeyframe);
                                    //先写时间  再写十个帧需要消耗的时间
                                    if (isFirst) {

                                        for (int i = 0; i < 10; i++) {
                                            try {
                                                outputStream.write(h264Data.get(i),
                                                                   0,
                                                                   h264Data.get(i).length);
                                                outputStream.flush();


                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        isFirst = false;
                                        h264Data.clear();
                                        handleCishu = 0;
                                        isHandle = false;
                                        continue;
                                    }

                                    copyByte = new byte[mKeyframe.length];
                                    System.arraycopy(mKeyframe, 0, copyByte, 0, mKeyframe.length);

                                    copyByte[100] = 11;
                                    copyByte[200] = 22;
                                    copyByte[300] = 33;


                                    //写标记
                                    daoNowTime = (int) (System.currentTimeMillis() - CameraPreview.starttime);
                                    // 8 - 10 位来记录时间

                                    copyByte[8] = (byte) (daoNowTime / 10000);
                                    copyByte[9] = (byte) (daoNowTime / 100 % 100);
                                    copyByte[10] = (byte) (daoNowTime % 100);
                                    copyByte[copyByte.length - 4] = 0;
                                    copyByte[copyByte.length - 3] = 0;
                                    copyByte[copyByte.length - 2] = 0;
                                    copyByte[copyByte.length - 1] = 0;


                                    try {
                                        outputStream.write(copyByte, 0, copyByte.length);
                                        outputStream.flush();
                                        //再写数据
                                        for (int i = 0; i < 10; i++) {
                                            outputStream.write(h264Data.get(i),
                                                               0,
                                                               h264Data.get(i).length);
                                            outputStream.flush();
                                        }

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    //写完之后   重置参数
                                    h264Data.clear();
                                    handleCishu = 0;
                                    isHandle = false;

                                } else {
                                    h264Data.add(mKeyframe);
                                }


                            } else {
                                handleCishu++;
                                if (handleCishu == 10) {

                                    h264Data.add(outData);

                                    isHandle = true;
                                    if (isFirst) {

                                        for (int i = 0; i < 10; i++) {
                                            try {
                                                outputStream.write(h264Data.get(i),
                                                                   0,
                                                                   h264Data.get(i).length);
                                                outputStream.flush();


                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        isFirst = false;
                                        h264Data.clear();
                                        handleCishu = 0;
                                        isHandle = false;
                                        continue;
                                    }
                                    //一起写进去   先写 时间
                                   copyByte = new byte[outData.length];
                                    System.arraycopy(outData, 0, copyByte, 0, outData.length);

                                    copyByte[100] = 11;
                                    copyByte[200] = 22;
                                    copyByte[300] = 33;
                                    //写标记
                                    daoNowTime = (int) (System.currentTimeMillis() - CameraPreview.starttime);
                                    // 8 - 10 位来记录时间

                                    copyByte[8] = (byte) (daoNowTime / 10000);
                                    copyByte[9] = (byte) (daoNowTime / 100 % 100);
                                    copyByte[10] = (byte) (daoNowTime % 100);

                                    copyByte[copyByte.length - 4] = 0;
                                    copyByte[copyByte.length - 3] = 0;
                                    copyByte[copyByte.length - 2] = 0;
                                    copyByte[copyByte.length - 1] = 0;

                                    try {
                                        outputStream.write(copyByte, 0, copyByte.length);
                                        outputStream.flush();
                                        for (int i = 0; i < 10; i++) {
                                            Log.d(TAG, "run: 要写诗词");
                                            outputStream.write(h264Data.get(i),
                                                               0,
                                                               h264Data.get(i).length);
                                            outputStream.flush();
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    //再写数据

                                    //写完之后   重置参数
                                    h264Data.clear();
                                    handleCishu = 0;
                                    isHandle = false;


                                } else {
                                    h264Data.add(outData);
                                }


                            }

                            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo,
                                                                               TIMEOUT_USEC);

                        }

                    } catch (Throwable t) {
                        t.printStackTrace();
                    }

                } else {

                }
            }

        }
    });

    public void handleYUVData() {
        mEncoderThread.setPriority(Thread.MAX_PRIORITY);
        Log.d(TAG, "handleYUVData: 开始这个县城");
        mEncoderThread.start();

    }

    private void NV21ToNV12(byte[] nv21, byte[] nv12, int width, int height) {
        if (nv21 == null || nv12 == null) { return; }
        int framesize = width * height;
        int i         = 0, j = 0;
        System.arraycopy(nv21, 0, nv12, 0, framesize);
        for (i = 0; i < framesize; i++) {
            nv12[i] = nv21[i];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j - 1] = nv21[j + framesize];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j] = nv21[j + framesize - 1];
        }
    }

    /**
     * Generates the presentation time for frame N, in microseconds.
     */
    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / m_framerate;
    }

    private static ByteBuffer buffer = ByteBuffer.allocate(8);

    public static byte[] longToBytes(long x) {
        buffer.putLong(0, x);
        return buffer.array();
    }

    public ArrayList<byte[]> h264Data    = new ArrayList<>();
    public ArrayList<Byte>   chaJuList   = new ArrayList<>();
    public ArrayList<Byte>   yuanshiList = new ArrayList<>();

    public static long bytes2long(byte[] b) {
        long temp = 0;
        long res  = 0;
        for (int i = 0; i < 8; i++) {
            res <<= 8;
            temp = b[i] & 0xff;
            res |= temp;
        }
        return res;
    }

    public static byte[] long2bytes(long num) {
        byte[] b = new byte[8];
        for (int i = 0; i < 8; i++) {
            b[i] = (byte) (num >>> (56 - (i * 8)));
        }
        return b;
    }
}
