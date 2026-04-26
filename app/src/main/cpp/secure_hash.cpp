#include <jni.h>
#include <string>
#include <cstring>
#include <cstdint>
#include <android/log.h>

#define LOG_TAG "SecureLock_Native"

// Constant-time comparison — prevents timing attacks on password hashes
extern "C" JNIEXPORT jboolean JNICALL
Java_com_secure_applock_util_CryptoUtil_nativeSecureCompare(
        JNIEnv* env, jobject /* this */, jstring a, jstring b) {

    const char* sa = env->GetStringUTFChars(a, nullptr);
    const char* sb = env->GetStringUTFChars(b, nullptr);
    size_t la = strlen(sa), lb = strlen(sb);

    volatile uint8_t diff = 0;
    size_t maxLen = la > lb ? la : lb;
    for (size_t i = 0; i < maxLen; i++) {
        uint8_t ca = i < la ? (uint8_t)sa[i] : 0;
        uint8_t cb = i < lb ? (uint8_t)sb[i] : 0;
        diff |= ca ^ cb;
    }
    diff |= (uint8_t)(la ^ lb);

    env->ReleaseStringUTFChars(a, sa);
    env->ReleaseStringUTFChars(b, sb);
    return diff == 0 ? JNI_TRUE : JNI_FALSE;
}

// Obfuscated app salt — harder to extract via reverse engineering
extern "C" JNIEXPORT jstring JNICALL
Java_com_secure_applock_util_CryptoUtil_nativeGetAppSalt(
        JNIEnv* env, jobject /* this */) {
    // Split salt components to avoid plain-text in binary
    const char p1[] = {0x53, 0x4C, 0x4B, 0x5F, 0};   // SLK_
    const char p2[] = {0x53, 0x45, 0x43, 0x5F, 0};   // SEC_
    const char p3[] = {0x32, 0x30, 0x32, 0x35, 0};   // 2025
    const char p4[] = {0x5F, 0x42, 0x44, 0x58, 0};   // _BDX
    std::string salt = std::string(p1) + p2 + p3 + p4;
    return env->NewStringUTF(salt.c_str());
}
