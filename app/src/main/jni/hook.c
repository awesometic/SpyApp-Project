#include <jni.h>

JNIEXPORT jstring JNICALL
Java_com_example_ydg_spyapp_HOOK_firstMessage(JNIEnv *env, jobject instance) {
    return (*env)->NewStringUTF(env, "NDK working success");
}