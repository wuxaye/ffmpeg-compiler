#include <jni.h>
#include <android/log.h>
//#include <libavutil/avutil.h>
#include <stdio.h>

extern "C" {
#include <libavutil/avutil.h>
}

#define LOG_TAG "FFmpegJNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)



extern "C" {
JNIEXPORT jstring JNICALL
Java_com_xaye_compiler_FFmpegHelper_getFFmpegVersion(JNIEnv *env, jclass clazz) {
    // 调用FFmpeg API获取版本信息
    const char* version = av_version_info();

    // 调试输出到logcat
    LOGD("Native FFmpeg version: %s", version);

    // 将C字符串转换为Java字符串
    return env->NewStringUTF(version ? version : "Unknown");
}
}