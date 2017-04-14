package com.example.lyc.sampleffpeg;


import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/*
 *  @项目名：  SampleFFpeg 
 *  @包名：    com.example.lyc.sampleffpeg
 *  @文件名:   MediaDecod
 *  @创建者:   LYC2
 *  @创建时间:  2017/3/7 11:44
 *  @描述：    TODO
 */
public class MediaDecod extends  Thread {
    private static final String TAG = "MediaDecod";
    private MediaExtractor mediaExtractor;
    /** 用来读取音視频文件 提取器 */
    private MediaCodec     mediaCodec;
    /** 用来解码 解碼器 */
    private Surface        surface;
    private DataInputStream mInputStream;
    private MediaCodec mCodec;
    private boolean mStopFlag = false;
    private final MediaExtractor mMediaExtractor;

    public MediaDecod(Surface surface) {
        this.surface = surface;
        mMediaExtractor = new MediaExtractor();
    }

    @Override
    public void run() {
        try {
            //获取文件输入流
            mInputStream = new DataInputStream(new FileInputStream(new File("/storage/emulated/0/test1.h264")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            mCodec = MediaCodec.createDecoderByType("video/avc");
        } catch (IOException e) {
            Log.d(TAG, "run: 创建decode失败");
            e.printStackTrace();
        }
        final MediaFormat mediaformat = MediaFormat.createVideoFormat("video/avc", 1920, 1080);
        //获取h264中的pps及sps数据
          Boolean isUsePpsAndSps = false;
        if (isUsePpsAndSps) {
            byte[] header_sps = {0, 0, 0, 1, 103, 66, 0, 42, (byte) 149, (byte) 168, 30, 0, (byte) 137, (byte) 249, 102, (byte) 224, 32, 32, 32, 64};
            byte[] header_pps = {0, 0, 0, 1, 104, (byte) 206, 60, (byte) 128, 0, 0, 0, 1, 6, (byte) 229, 1, (byte) 151, (byte) 128};
            mediaformat.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
            mediaformat.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
        }
        //设置帧率
        mediaformat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        mCodec.configure(mediaformat, surface, null, 0);
        mCodec.start();
        //存放目标文件的数据
        ByteBuffer[] inputBuffers = mCodec.getInputBuffers();
        //解码后的数据，包含每一个buffer的元数据信息，例如偏差，在相关解码器中有效的数据大小
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        mCodec.dequeueOutputBuffer(info , 0);
        Log.d(TAG, "run: info first" +  info.presentationTimeUs);
        long startMs = System.currentTimeMillis();
        long timeoutUs = 10000;
        byte[] marker0 = new byte[]{0, 0, 0, 1};
        byte[] dummyFrame = new byte[]{0x00, 0x00, 0x01, 0x20};
        byte[] streamBuffer = null;
        try {
            streamBuffer = getBytes(mInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int bytes_cnt = 0;
        while (mStopFlag == false) {
            bytes_cnt = streamBuffer.length;
            if (bytes_cnt == 0) {
                streamBuffer = dummyFrame;
            }

            int startIndex = 0;
            int remaining = bytes_cnt;
            while (true) {
                if (remaining == 0 || startIndex >= remaining) {
                    break;
                }
                int nextFrameStart = KMPMatch(marker0, streamBuffer, startIndex + 2, remaining);
                if (nextFrameStart == -1) {
                    nextFrameStart = remaining;
                } else {
                }

                int inIndex = mCodec.dequeueInputBuffer(timeoutUs);
                if (inIndex >= 0) {
                    ByteBuffer byteBuffer = inputBuffers[inIndex];
                    byteBuffer.clear();
                    byteBuffer.put(streamBuffer, startIndex, nextFrameStart - startIndex);
                    //在给指定Index的inputbuffer[]填充数据后，调用这个函数把数据传给解码器
                    mCodec.queueInputBuffer(inIndex, 0, 0, 122, MediaCodec.BUFFER_FLAG_END_OF_STREAM);

                    int i = mMediaExtractor.readSampleData(byteBuffer, 0);
                    long sampleTime = mMediaExtractor.getSampleTime();
                    Log.d(TAG, "run: mMediaExtractor.readSampleData(byteBuffer, 0)" + i + "mMediaExtractor.getSampleTime()"+ sampleTime);
                    startIndex = nextFrameStart;
                } else {
                    Log.d(TAG, "aaaaa");
                    continue;
                }

                int outIndex = mCodec.dequeueOutputBuffer(info, timeoutUs);
                Log.d(TAG, "run: bufferinfo" + info.presentationTimeUs);
                if (outIndex >= 0) {
                    //帧控制是不在这种情况下工作，因为没有PTS H264是可用的

                    while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    boolean doRender = (info.size != 0);
                    //对outputbuffer的处理完后，调用这个函数把buffer重新返回给codec类。
                    mCodec.releaseOutputBuffer(outIndex, doRender);
                } else {
                    Log.d(TAG, "bbbb");
                }
            }
            mStopFlag = true;
            Log.d(TAG, "run: 播放结束");
        }

    }
    private int KMPMatch(byte[] pattern, byte[] bytes, int start, int remain) {
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int[] lsp = computeLspTable(pattern);

        int j = 0;  // Number of chars matched in pattern
        for (int i = start; i < remain; i++) {
            while (j > 0 && bytes[i] != pattern[j]) {
                // Fall back in the pattern
                j = lsp[j - 1];  // Strictly decreasing
            }
            if (bytes[i] == pattern[j]) {
                // Next char matched, increment position
                j++;
                if (j == pattern.length)
                    return i - (j - 1);
            }
        }
        return -1;  // Not found
    }

    private int[] computeLspTable(byte[] pattern) {
        int[] lsp = new int[pattern.length];
        lsp[0] = 0;  // Base case
        for (int i = 1; i < pattern.length; i++) {
            // Start by assuming we're extending the previous LSP
            int j = lsp[i - 1];
            while (j > 0 && pattern[i] != pattern[j])
                j = lsp[j - 1];
            if (pattern[i] == pattern[j])
                j++;
            lsp[i] = j;
        }
        return lsp;
    }   public static byte[] getBytes(InputStream is) throws IOException {
        int len;
        int size = 1024;
        byte[] buf;
        if (is instanceof ByteArrayInputStream) {
            size = is.available();
            buf = new byte[size];
            len = is.read(buf, 0, size);
        } else {
            //            BufferedOutputStream bos=new BufferedOutputStream(new ByteArrayOutputStream());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            buf = new byte[size];
            while ((len = is.read(buf, 0, size)) != -1)
                bos.write(buf, 0, len);
            buf = bos.toByteArray();
        }
        Log.e(TAG, "bbbb");
        return buf;
    }

}

