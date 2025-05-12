#include <stdio.h>
#include <android/log.h>

int test_function() {
    __android_log_print(ANDROID_LOG_INFO, "NativeLib", "Hello from C!");
    return 20250429;
}
