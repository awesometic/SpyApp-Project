#include <jni.h>

JNIEXPORT jstring JNICALL
Java_com_example_ydg_spyapp_MainActivity_getMessage(JNIEnv *env, jobject instance) {

    // TODO


    return (*env)->NewStringUTF(env, "Hello JNI!");
}