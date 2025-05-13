#include <jni.h>
#include <android/log.h>
//#include <libavutil/avutil.h>
#include <stdio.h>

extern "C" {
#include <libavutil/avutil.h>
#include "libavformat/avformat.h"
}

#define LOG_TAG "FFmpegJNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)


extern "C" {
JNIEXPORT jstring JNICALL
Java_com_xaye_compiler_FFmpegHelper_getFFmpegVersion(JNIEnv *env, jclass clazz) {
    // 调用FFmpeg API获取版本信息
    const char *version = av_version_info();

    // 调试输出到logcat
    LOGD("Native FFmpeg version: %s", version);

    // 将C字符串转换为Java字符串
    return env->NewStringUTF(version ? version : "Unknown");
}
}

//解析视频时长、分辨率、音频采样率等元数据
extern "C" JNIEXPORT jstring JNICALL
Java_com_xaye_compiler_FFmpegHelper_getMediaInfo(JNIEnv *env, jclass clazz, jstring filePath) {
    const char *path = env->GetStringUTFChars(filePath, nullptr);
    AVFormatContext *fmt_ctx = nullptr;
    avformat_open_input(&fmt_ctx, path, nullptr, nullptr);
    avformat_find_stream_info(fmt_ctx, nullptr);

    // 提取信息
    int video_stream_idx = av_find_best_stream(fmt_ctx, AVMEDIA_TYPE_VIDEO, -1, -1, nullptr, 0);
    int audio_stream_idx = av_find_best_stream(fmt_ctx, AVMEDIA_TYPE_AUDIO, -1, -1, nullptr, 0);

    char info[1024];
    snprintf(info, sizeof(info),
             "Duration: %.2fs\nVideo: %dx%d\nAudio: %dHz",
             fmt_ctx->duration / (float)AV_TIME_BASE,
             fmt_ctx->streams[video_stream_idx]->codecpar->width,
             fmt_ctx->streams[video_stream_idx]->codecpar->height,
             fmt_ctx->streams[audio_stream_idx]->codecpar->sample_rate);

    avformat_close_input(&fmt_ctx);
    env->ReleaseStringUTFChars(filePath, path);
    return env->NewStringUTF(info);
}