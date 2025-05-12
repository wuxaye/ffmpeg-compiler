package com.xaye.compiler;

public class FFmpegHelper {
    static {
        // 按依赖顺序加载FFmpeg库
        System.loadLibrary("avutil");
        System.loadLibrary("swresample");
        System.loadLibrary("avcodec");
        System.loadLibrary("avformat");
        System.loadLibrary("swscale");
        System.loadLibrary("ffmpeg_jni"); // 我们的JNI库
    }

    public static native String getFFmpegVersion();
}
