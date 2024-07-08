#include <jni.h>
#include <string>

extern "C"
jstring
Java_com_huntersmeadow_wordpuzzleassist_MainActivity_stringFromJNI(
        JNIEnv *env,
        jObject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
