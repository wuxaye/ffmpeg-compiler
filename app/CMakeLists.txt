# 最低 CMake 版本要求
cmake_minimum_required(VERSION 3.4.1)

# 项目设置
project("ffmpeg_jni")

# 设置 C 标准（FFmpeg 需要 C11）
set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 11)

# 打印当前 ABI 用于调试
message("Current ABI: ${ANDROID_ABI}")

# 设置 FFmpeg 库路径（根据实际路径调整）
set(FFMPEG_LIBS_DIR ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})

# 定义 FFmpeg 核心库（按依赖顺序）
set(FFMPEG_LIBS
        avutil
        swresample
        avcodec
        avformat
        swscale
        # 可选添加其他库：postproc, avfilter 等
)

# 导入预编译的 FFmpeg 共享库
foreach(LIB ${FFMPEG_LIBS})
    add_library(${LIB} SHARED IMPORTED)
    set_target_properties(${LIB} PROPERTIES
            IMPORTED_LOCATION "${FFMPEG_LIBS_DIR}/lib${LIB}.so"
            # 对于 Android 8.0+ 需要设置 SONAME
            IMPORTED_SONAME "lib${LIB}.so"
    )
    message("Imported lib: ${FFMPEG_LIBS_DIR}/lib${LIB}.so")
endforeach()

# 添加 Android 日志库
find_library(log-lib log)

# 设置 JNI 源文件
file(GLOB JNI_SOURCES src/main/cpp/*.cpp)

# 创建 JNI 库
add_library(ffmpeg_jni SHARED ${JNI_SOURCES})

# 头文件包含路径（根据 FFmpeg 头文件位置调整）
target_include_directories(ffmpeg_jni PRIVATE
        ${CMAKE_SOURCE_DIR}/src/main/cpp/include  # 头文件放在这里
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/include  # 如果 FFmpeg 头文件随库提供
)

# 链接库
target_link_libraries(ffmpeg_jni
        android
        ${log-lib}
        ${FFMPEG_LIBS}  # 按依赖顺序自动链接
)

# 编译选项优化
target_compile_options(ffmpeg_jni PRIVATE
        -Wall
        -Werror
        -fno-exceptions
        -fno-rtti
        -fvisibility=hidden
)

