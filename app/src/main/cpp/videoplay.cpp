//
// Created by LYC2 on 2017/1/13.
//


#include <jni.h>
#include <android/log.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <sys/socket.h>
#include <unistd.h >




extern "C" {

//这里为什么要包括在里面  是因为ffmpeg是纯C写的   而我的编译器使用clang（Ｃ＋＋）所以在这里要进行
// 进行一个包括，意思就是按C来编译
#include "include/libavcodec/avcodec.h"
#include "include/libavformat/avformat.h"
#include "include/libswscale/swscale.h"
#include "include/libavutil/imgutils.h"
#include "include/libavutil/time.h"
#include "include/libavutil/opt.h"
//这个“”  的根目录是在 这个cpp文件的根目录

#define  LOG_TAG    "videoplay"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
long getCurrentTime() {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return tv.tv_sec * 1000 + tv.tv_usec / 1000;
}

JNIEXPORT jint
JNICALL
Java_com_example_lyc_sampleffpeg_VideoPlayer_play(JNIEnv *env, jclass type, jobject surface) {

    // TODO
    LOGD("HYplay");



    // sd卡中的视频文件地址,可自行修改或者通过jni传入

    char *file_name = "/storage/emulated/0/test1.h264";

    av_register_all();

    AVFormatContext *pFormatCtx = avformat_alloc_context();

    long start_time_play = (long) av_gettime();
    long end_time_play = 0;
    // Open video file
    if (avformat_open_input(&pFormatCtx, file_name, NULL, NULL) != 0) {
        int err_code = avformat_open_input(&pFormatCtx, file_name, NULL, NULL);
        char buf[1024];
        av_strerror(err_code, buf, 1024);
        LOGD("Couldn't open file %s: %d(%s)", file_name, err_code, buf);
        LOGD("Couldn't open file:%s\n", file_name);
        return -1; // Couldn't open file
    }

    // Retrieve stream information
    if (avformat_find_stream_info(pFormatCtx, NULL) < 0) {
        LOGD("Couldn't find stream information.");
        return -1;
    }

    // Find the first video stream
    int videoStream = -1, i;
    for (i = 0; i < pFormatCtx->nb_streams; i++) {
        if (pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO
            && videoStream < 0) {
            videoStream = i;
        }
    }
    if (videoStream == -1) {
        LOGD("Didn't find a video stream.");
        return -1; // Didn't find a video stream
    }

    // Get a pointer to the codec context for the video stream
    AVCodecContext *pCodecCtx = pFormatCtx->streams[videoStream]->codec;

    // Find the decoder for the video stream
    AVCodec *pCodec = avcodec_find_decoder(pCodecCtx->codec_id);
    if (pCodec == NULL) {
        LOGD("Codec not found.");
        return -1; // Codec not found
    }

    if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
        LOGD("Could not open codec.");
        return -1; // Could not open codec
    }

    // 获取native window
    ANativeWindow *nativeWindow = ANativeWindow_fromSurface(env, surface);

    // 获取视频宽高
    int videoWidth = pCodecCtx->width;
    int videoHeight = pCodecCtx->height;

    // 设置native window的buffer大小,可自动拉伸

    LOGD("videoWidth : %d , videoHeight ：%d", videoWidth, videoHeight);
    //1920 1080
    ANativeWindow_setBuffersGeometry(nativeWindow, videoWidth, videoHeight,
                                     WINDOW_FORMAT_RGBA_8888);
    ANativeWindow_Buffer windowBuffer;

    if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
        LOGD("Could not open codec.");
        return -1; // Could not open codec
    }

    // Allocate video frame
    AVFrame *pFrame = av_frame_alloc();

    // 用于渲染
    AVFrame *pFrameRGBA = av_frame_alloc();
    if (pFrameRGBA == NULL || pFrame == NULL) {
        LOGD("Could not allocate video frame.");
        return -1;
    }

    // Determine required buffer size and allocate buffer
    // buffer中数据就是用于渲染的,且格式为RGBA
    int numBytes = av_image_get_buffer_size(AV_PIX_FMT_RGBA, pCodecCtx->width, pCodecCtx->height,
                                            1);
    uint8_t *buffer = (uint8_t *) av_malloc(numBytes * sizeof(uint8_t));
    av_image_fill_arrays(pFrameRGBA->data, pFrameRGBA->linesize, buffer, AV_PIX_FMT_RGBA,
                         pCodecCtx->width, pCodecCtx->height, 1);

    // 由于解码出来的帧格式不是RGBA的,在渲染之前需要进行格式转换
    struct SwsContext *sws_ctx = sws_getContext(pCodecCtx->width,
                                                pCodecCtx->height,
                                                pCodecCtx->pix_fmt,
                                                pCodecCtx->width,
                                                pCodecCtx->height,
                                                AV_PIX_FMT_RGBA,
                                                SWS_BILINEAR,
                                                NULL,
                                                NULL,
                                                NULL);

    int frameFinished;
    AVPacket packet;
    int num = 0;
    int whileNuM = 0;
    int iFrameNum = 0;
    int chaju[11];
    int chajuNum = 0;
    long startTime = 0;
    long endTime = 0;
    int tenOfTheNumOne = 0;
    int shijiChaju = 0;
    int xiumianAllTime = 0;
    int xiuzhengZhi = 0;
    long chaju_play = 0;

    while (av_read_frame(pFormatCtx, &packet) >= 0) {
        whileNuM++;
        if (packet.flags == AV_PKT_FLAG_KEY) {
            LOGD("说明当前帧是I帧 %d", whileNuM);
            int a5 = (int) *(packet.data + 100);
            int a6 = (int) *(packet.data + 200);
            int a7 = (int) *(packet.data + 300);
            LOGD("说明当前帧是I帧 %d  a5 = %d , a6 = %d , a7 = %d", whileNuM, a5, a6, a7);

            if (a5 == 11 && a6 == 22 && a7 == 33) {
                int a8 = (int) *(packet.data + 8);
                int a9 = (int) *(packet.data + 9);
                int a10 = (int) *(packet.data + 10);

                int daoNowTime = (a8 * 10000 + a9 * 100 + a10) * 1000;
                LOGD("进入到应该舍弃的帧的a8 = %d , a9 = %d , a10 = %d , daoNowTime = %d", a8, a9,
                     a10, daoNowTime);
                end_time_play = (long) av_gettime();
                chaju_play = (daoNowTime - (end_time_play - start_time_play)) / 10;

                LOGD("daoNowTime = %ld", (end_time_play - start_time_play));
                LOGD("chaju_play = %ld", chaju_play);

                continue;
            }
        } else {
            int a5 = (int) *(packet.data + 100);
            int a6 = (int) *(packet.data + 200);
            int a7 = (int) *(packet.data + 300);
            LOGD("说明当前帧不是I帧 %d  a5 = %d , a6 = %d , a7 = %d", whileNuM, a5, a6, a7);
            if (a5 == 11 && a6 == 22 && a7 == 33) {
                LOGD("进入到应该舍弃的帧的");
                int a8 = (int) *(packet.data + 8);
                int a9 = (int) *(packet.data + 9);
                int a10 = (int) *(packet.data + 10);
                int daoNowTime = (a8 * 10000 + a9 * 100 + a10) * 1000;
                LOGD("进入到应该舍弃的帧的a8 = %d , a9 = %d , a10 = %d , daoNowTime = %d", a8, a9,
                     a10, daoNowTime);
                end_time_play = (long) av_gettime();
                chaju_play = (daoNowTime - (end_time_play - start_time_play)) / 10;
                LOGD("daoNowTime = %ld", (end_time_play - start_time_play));
                LOGD("chaju_play = %ld", chaju_play);

                continue;
            }

        }

        if (packet.stream_index == videoStream) {
            iFrameNum++;
            avcodec_decode_video2(pCodecCtx, pFrame, &frameFinished, &packet);
            LOGD("Jareld-2:  iFrameNum = %d ,tenOfTheNumOne = %d ", iFrameNum, tenOfTheNumOne);
            if (frameFinished) {


                num++;
                if (pFrame->flags == AV_PKT_FLAG_KEY) {
                    LOGD("说明当前帧是I帧 %d", num);
                }
                // lock native window buffer
                ANativeWindow_lock(nativeWindow, &windowBuffer, 0);
                LOGD("Jareld-3: num = %d", num);
                sws_scale(sws_ctx, (uint8_t const *const *) pFrame->data,
                          pFrame->linesize, 0, pCodecCtx->height,
                          pFrameRGBA->data, pFrameRGBA->linesize);
                // 获取stride
                uint8_t *dst = (uint8_t *) windowBuffer.bits;
                int dstStride = windowBuffer.stride * 4;
                uint8_t *src = (pFrameRGBA->data[0]);
                int srcStride = pFrameRGBA->linesize[0];
                // 由于window的stride和帧的stride不同,因此需要逐行复制
                int h;
                for (h = 0; h < videoHeight; h++) {
                    memcpy(dst + h * dstStride, src + h * srcStride, srcStride);
                }
                ANativeWindow_unlockAndPost(nativeWindow);
                if(chaju_play > 0 ){
                    LOGD("真正休眠了 chaju_play =%ld" ,chaju_play);
                    av_usleep(chaju_play);
                }

            } else {
                LOGD("说明finished为flase");
            }
        } else {
            LOGD("说明流的 index不符合");
        }

        av_packet_unref(&packet);
        LOGD("到unref的时间111 = %d", (av_gettime() - startTime));

    }

    av_free(buffer);
    av_free(pFrameRGBA);

    // Free the YUV frame
    av_free(pFrame);

    // Close the codecs
    avcodec_close(pCodecCtx);

    // Close the video file
    avformat_close_input(&pFormatCtx);
    return 0;
}

//这里是利用jni来推流
JNIEXPORT void JNICALL
Java_com_example_lyc_sampleffpeg_VideoPlayer_jni_1ffmpeg_1push(JNIEnv * env , jclass type,
        jstring
intput_ , jstring output_ ) {
//视频文件所在地址
const char *intput = env->GetStringUTFChars(intput_, 0);
//推送的流媒体地址
const char *output = env->GetStringUTFChars(output_, 0);
LOGD("Jareld:jni_push") ;
//封装格式 写入  和 输出    -------------解封装  得到frame
AVFormatContext *inFmtCtx = NULL, *outFmtCtx = NULL;
//注册组件
av_register_all();
//初始化网络
avformat_network_init();

//定义一个返回的参数  来决定是什么
int ret;
if ( ret = avformat_open_input(&inFmtCtx, intput, 0, 0) < 0 ) {
LOGD("Jareld:无法打开文件") ;
//这里就要退出   关闭输入和输出流
//        goto end;

avformat_free_context(inFmtCtx);
avformat_free_context(outFmtCtx);

}

//获取文件信息：
if ((
ret = avformat_find_stream_info(inFmtCtx, 0)
) < 0) {
LOGD("Jareld:获取文件信息失败");
//   goto  end;

avformat_free_context(inFmtCtx);
avformat_free_context(outFmtCtx);

}

//输出的封装格式   以rtmp协议来输出 我推送的是MP4格式的视频流
avformat_alloc_output_context2(&outFmtCtx, NULL,
"flv", output);

LOGD("Jareld:avformat_alloc_output_context2");

//根据输入的avStream，构造输出流AVStream
int i = 0;

//思考 在输入的时候 需要几个输出流。 因为输入流  有头信息 ，有音频 ，有视频
//这个输入流的nb_streams 是什么呢？ 是这个MP4流里面的封装格式是 头文件，音频，视频，音频，视频
//比如 之前学的flv的封装格式   头文件，音频，视频，音频，视频。。。。。
//这个有多少个 这个nb_streams就有多少个
for (
i = 0;
i<inFmtCtx->
nb_streams;
i++) {
AVStream *input_stream = inFmtCtx->streams[i];
//输入和输出解码器要保持一致
AVStream *out_stream = avformat_new_stream(outFmtCtx, input_stream->codec->codec);
//解码器的上下文的copy 把输入的流的解码器上下文copy到输出流里面去
avcodec_copy_context(out_stream
->codec, input_stream->codec);
//全局的 header
out_stream->codec->
codec_tag = 0;
if (outFmtCtx->oformat->flags == AVFMT_GLOBALHEADER) {
//有一个全局的头
out_stream->codec->
flags = CODEC_FLAG_GLOBAL_HEADER;
}
}

LOGD("Jareld:for循环");

//打开输出的AVIOContext IO流上下文
AVOutputFormat *ofmt = outFmtCtx->oformat;
if (!(ofmt->flags & AVFMT_NOFILE)) {
ret = avio_open(&outFmtCtx->pb, output, AVIO_FLAG_WRITE);
LOGD("Jareld:avio_open%d", ret);
char buf[1024];
av_strerror(ret, buf,
1024);
LOGD("Couldn't open file : %d(%s)", ret, buf);
}
LOGD("Jareld:avio_open%d", ret);
//先写一个头
ret = avformat_write_header(outFmtCtx, NULL);
LOGD("Jareld:avformat_write_heade");

if (ret < 0) {
LOGD("Jareld:推流失败");
char buf[1024];
av_strerror(ret, buf,
1024);
LOGD("Couldn't open file : %d(%s)", ret, buf);
//  goto end;
avformat_free_context(inFmtCtx);
avformat_free_context(outFmtCtx);
}

//获取视频流的索引位置
int videoIndex = -1;
for (
i = 0;
i<inFmtCtx->
nb_streams;
i++) {
if (inFmtCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
videoIndex = i;
break;
}
}
//找到视频帧的索引
int frame_index = 0;
int strem_num = 0;
int64_t start_time = av_gettime();
AVPacket pkt;

while (1) {
AVStream *in_avstream, *out_avstream;
//读取avpacket
ret = av_read_frame(inFmtCtx, &pkt);
if (ret < 0) {
LOGD("Jareld:读取avpacket失败");
break;
}

//裸流中没有这两个数据
//PTS :解码后视频帧要在什么时候显示出来
//DTS ：在送入解码器开始解码的时候标识什么时候开始解码
//这里其实可以注释掉，因为我没有用裸流 所以pts和dts是有的
//        if(pkt.pts == AV_NOPTS_VALUE){
//            //写pkt
//            AVRational time_base1 = inFmtCtx->streams[videoIndex]->time_base;

//            int64_t  clac_duration = (double)AV_TIME_BASE/av_q2d(inFmtCtx->streams[videoIndex]->r_frame_rate);
//            pkt.pts = (double)(frame_index * clac_duration) / (double)(av_q2d(time_base1)*AV_TIME_BASE);
//            pkt.dts = pkt.pts;
//            pkt.duration = (double) clac_duration / (double)(av_q2d(time_base1)*AV_TIME_BASE);
//        }
//

if (pkt.stream_index == videoIndex) {
//ffmpeg处理速度很快 需要延迟，减少流媒体的负担
AVRational time_base = inFmtCtx->streams[videoIndex]->time_base;
AVRational time_base_q = {1, AV_TIME_BASE};
int64_t pts_time = av_rescale_q(pkt.dts, time_base, time_base_q);
int64_t now_time = av_gettime() - start_time;
if ((pts_time - now_time) > 0) {
av_usleep(pts_time
- now_time);
LOGD("pts_time %d", (pts_time - now_time));
}
}

in_avstream = inFmtCtx->streams[pkt.stream_index];
out_avstream = outFmtCtx->streams[pkt.stream_index];

//拷贝这个包packet  如果已经有的情况下
pkt.
pts = av_rescale_q_rnd(pkt.pts, in_avstream->time_base, out_avstream->time_base,
                       (AVRounding)(AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
pkt.
dts = av_rescale_q_rnd(pkt.dts, in_avstream->time_base, out_avstream->time_base,
                       (AVRounding)(AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
pkt.
duration = av_rescale_q(pkt.duration, in_avstream->time_base, out_avstream->time_base);
pkt.
pos = -1;
if (pkt.stream_index == videoIndex) {
LOGD("第%d帧", frame_index);
frame_index++;
}

//这里才是真正的写出去
ret = av_interleaved_write_frame(outFmtCtx, &pkt);

if (ret < 0) {
LOGD("错误的muxing packet");
break;
}

//释放资源
av_free_packet(&pkt);


}
//写结尾
av_write_trailer(outFmtCtx);
LOGD("Jareld:推流成功");
avformat_free_context(inFmtCtx);
avformat_free_context(outFmtCtx);

env->
ReleaseStringUTFChars(intput_, intput
);
env->
ReleaseStringUTFChars(output_, output
);

//    end:
//    avformat_free_context(inFmtCtx);
//    avformat_free_context(outFmtCtx);

}
//录像的时候 先设置输入和输出的参数
JNIEXPORT void JNICALL
Java_com_example_lyc_sampleffpeg_VideoPlayer_jni_1ffmpeg_1hecv_1set_1configuration(JNIEnv
*env,
jclass type,
        jstring
intput_,
jstring output_
) {
const char *intput = env->GetStringUTFChars(intput_, 0);
const char *output = env->GetStringUTFChars(output_, 0);
AVFormatContext *pFormatCtx;
AVOutputFormat *fmt;
AVStream *video_st;
AVCodecContext *pCodecCtx;
AVCodec *pCodec;

uint8_t *picture_buf;
AVFrame *picture;
int size;

//注册所有组件
av_register_all();

pFormatCtx = avformat_alloc_context();
//猜输出的格式
fmt = av_guess_format(NULL, output, NULL);
pFormatCtx->
oformat = fmt;

if (
avio_open(&pFormatCtx
->pb, output, AVIO_FLAG_READ_WRITE) < 0) {
LOGD("Failed to open output file! 输出文件打开失败");
return;
}


env->
ReleaseStringUTFChars(intput_, intput
);
env->
ReleaseStringUTFChars(output_, output
);
}
//录像过程中 buffer的写入
JNIEXPORT void JNICALL
Java_com_example_lyc_sampleffpeg_VideoPlayer_jni_1ffmpeg_1hecv_1set_1buffer(JNIEnv
*env,
jclass type,
        jintArray
buffer_) {
jint *buffer = env->GetIntArrayElements(buffer_, NULL);

// TODO

env->
ReleaseIntArrayElements(buffer_, buffer,
0);
}

int flush_encoder(AVFormatContext *fmt_ctx, unsigned int stream_index) {
    int ret;
    int got_frame;
    AVPacket enc_pkt;
    if (!(fmt_ctx->streams[stream_index]->codec->codec->capabilities & CODEC_CAP_DELAY)) {
        return 0;
    }
    while (1) {
        LOGD("Flushing stream %d encoder\n", stream_index);
        enc_pkt.data = NULL;
        enc_pkt.size = 0;
        av_init_packet(&enc_pkt);
        ret = avcodec_encode_video2(fmt_ctx->streams[stream_index]->codec, &enc_pkt, NULL,
                                    &got_frame);
        av_frame_free(NULL);

        if (ret < 0) {
            break;
        }

        if (!got_frame) {
            ret = 0;
            break;
        }

        LOGD("成功编码第%d帧数据\n", stream_index);

        ret = av_write_frame(fmt_ctx, &enc_pkt);

        if (ret < 0) {
            break;
        }
    }
    return ret;
}

AVCodec *pCodec1;
AVCodecContext *pCodecCtx1 = NULL;
int i, ret, got_output;
FILE *fp_out;
AVFrame *pFrame;
AVPacket pkt;
int y_size;
int framecnt = 0;
char filename_out[] = "/storage/emulated/0/test111.h264";
int in_w = 1920, in_h = 1080;
int count = 0;
JNIEXPORT jint
JNICALL
        Java_com_example_lyc_sampleffpeg_VideoPlayer_jni_1ffmpeg_1setVersion(JNIEnv * env, jclass
type) {

// TODO
avcodec_register_all();

pCodec1 = avcodec_find_encoder(AV_CODEC_ID_HEVC);
if (!pCodec1) {
LOGD("Codec not found\n");
return -1;
}
pCodecCtx1 = avcodec_alloc_context3(pCodec1);
if (!pCodecCtx1) {
LOGD("Could not allocate video codec context\n");
return -1;
}
pCodecCtx1->
bit_rate = 400000;
pCodecCtx1->
width = in_h;
pCodecCtx1->
height = in_w;
pCodecCtx1->time_base.
num = 1;
pCodecCtx1->time_base.
den = 30;
pCodecCtx1->
gop_size = 10;
pCodecCtx1->
max_b_frames = 5;
pCodecCtx1->
pix_fmt = AV_PIX_FMT_YUV420P;

av_opt_set(pCodecCtx1
->priv_data, "preset", "superfast", 0);
//  av_opt_set(pCodecCtx->priv_data, "preset", "slow", 0);
av_opt_set(pCodecCtx1
->priv_data, "tune", "zerolatency", 0);


if (
avcodec_open2(pCodecCtx1, pCodec1, NULL
) < 0) {
LOGD("Could not open codec\n");
return -1;
}
if ((
fp_out = fopen(filename_out, "wb")
) == NULL) {
LOGD("out shibai");
return -1;
}
y_size = pCodecCtx1->width * pCodecCtx1->height;
LOGD("返回1");
return 1;

}
JNIEXPORT jbyteArray
JNICALL
        Java_com_example_lyc_sampleffpeg_VideoPlayer_jni_1handle_1nv21_1to_1nv12(JNIEnv * env,
                                                                                 jclass
type,
jbyteArray input_,

        jint
m_width,
jint m_height
) {

jbyte *input = env->GetByteArrayElements(input_, NULL);


int framesize = m_width * m_height;
int j = 0;
jbyte temp = 0;


for (
j = 0;
j<framesize / 2; j += 2) {
temp = input[j + framesize - 1];
input[framesize + j - 1] = input[j + framesize];
input[j + framesize] =
temp;
}


jbyteArray it = env->NewByteArray(framesize * 3 / 2);
env->
SetByteArrayRegion(it,
0, framesize * 3 / 2, input);
env->
ReleaseByteArrayElements(input_, input,
0);

return
it;


}

void send_remote_request(const char *msg)
{
    int localsocket, len;
    struct sockaddr remote;

    if ((localsocket = socket(AF_UNIX,SOCK_STREAM, 0)) == -1) {
    exit(1);
    }

    const char *name="Jareld";//与java上层相同哦
    remote.sa_data[0] = '\0';
    strcpy(remote.sa_data+1, name);
    remote.sa_family = AF_LOCAL;
    int nameLen = strlen(name);
    LOGD(" namelen = %d" , nameLen);
    len = 1 + nameLen + offsetof(sockaddr, sa_data);
    LOGD(" len = %d" , len);
    int ret = connect(localsocket, &remote, len);
    if (ret == -1) {
    return;
    }

    LOGD("Jareld : connect(localsocket, &remote, len) = %d" , ret);
    if (send(localsocket, msg,strlen(msg),0) == -1) {
    return;
    }
    LOGD("Jareld : send zhi hou " );
    shutdown(localsocket , 2);

}

JNIEXPORT void JNICALL
Java_com_example_lyc_sampleffpeg_MainActivity_sendDataFromNativeSocket(JNIEnv *env, jclass type,
                                                                       jstring string_) {
    const char *string = env->GetStringUTFChars(string_, 0);


    send_remote_request(string);
    // TODO

    env->ReleaseStringUTFChars(string_, string);
}
JNIEXPORT void JNICALL
                                                     Java_com_example_lyc_sampleffpeg_VideoPlayer_sendDataFromNativeSocket(JNIEnv *env, jclass type,
                                                                      jstring string_) {
    const char *string = env->GetStringUTFChars(string_, 0);
    send_remote_request(string);
    // TODO

    env->ReleaseStringUTFChars(string_, string);


}

JNIEXPORT void JNICALL
Java_com_example_lyc_sampleffpeg_VideoPlayer_getDataFromNativeSocket(JNIEnv *env, jclass type) {
    int server_sockfd, client_sockfd;
    socklen_t server_len, client_len;
    struct sockaddr  server_addr;
    struct sockaddr  client_addr;
    char ch;
    LOGD("Jareld ：server start ");
    // TODO
    //创建socket
    if ((server_sockfd = socket(AF_UNIX,  SOCK_STREAM,  0)) == -1) {
        LOGD("Jareld ：server socket error ");

    }
    //命令socket
    const char *name="Jareld";//与java上层相同哦
    server_addr.sa_data[0] = '\0';
    strcpy(server_addr.sa_data+1, name);
    server_addr.sa_family = AF_LOCAL;
    int nameLen = strlen(name);
    LOGD(" Jareld namelen = %d" , nameLen);
    server_len = 1 + nameLen + offsetof(sockaddr, sa_data);
    LOGD("Jareld len = %d" , server_len);

    LOGD("Jareld ：server bind... ");
    bind(server_sockfd,  &server_addr, server_len);
    LOGD("Jareld ：server listen... ");

    listen(server_sockfd, 5);

    LOGD("Jareld ：server waiting... ");

//accept client connect
        client_len = sizeof(client_addr);
        client_sockfd = accept(server_sockfd,(struct sockaddr*)&client_addr, &client_len);

//read  data from client socket
        LOGD("Jareld : receview zhihou ");


}
}