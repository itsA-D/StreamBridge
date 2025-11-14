#include <jni.h>
#include <string>
#include <android/log.h>
#include <opencv2/opencv.hpp>

#define  LOG_TAG    "NativeEdge"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

using namespace cv;

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_edgeapp_NativeBridge_processFrameNV21(JNIEnv *env, jobject thiz, jbyteArray input_, jint width, jint height) {
    if (input_ == nullptr) return nullptr;
    jsize len = env->GetArrayLength(input_);
    jbyte* input = env->GetByteArrayElements(input_, NULL);

    // NV21 to Mat (YUV420sp)
    Mat yuv(height + height/2, width, CV_8UC1, (unsigned char*)input);
    Mat rgba;
    try {
        cvtColor(yuv, rgba, COLOR_YUV2RGBA_NV21);
    } catch (cv::Exception& e) {
        LOGE("OpenCV cvtColor error: %s", e.what());
        env->ReleaseByteArrayElements(input_, input, 0);
        return NULL;
    }

    // Convert to gray and Canny
    Mat gray;
    cvtColor(rgba, gray, COLOR_RGBA2GRAY);
    Mat edges;
    Canny(gray, edges, 80, 150);

    // Convert edges (single channel) to RGBA (edges white on black background)
    Mat out;
    cvtColor(edges, out, COLOR_GRAY2RGBA);

    // Prepare output byte array
    int outSize = out.total() * out.elemSize();
    jbyteArray outArr = env->NewByteArray(outSize);
    env->SetByteArrayRegion(outArr, 0, outSize, (jbyte*)out.data);

    env->ReleaseByteArrayElements(input_, input, 0);
    return outArr;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_edgeapp_NativeBridge_convertNV21ToRGBA(JNIEnv *env, jobject thiz, jbyteArray input_, jint width, jint height) {
    if (input_ == nullptr) return nullptr;
    jbyte* input = env->GetByteArrayElements(input_, NULL);

    Mat yuv(height + height/2, width, CV_8UC1, (unsigned char*)input);
    Mat rgba;
    try {
        cvtColor(yuv, rgba, COLOR_YUV2RGBA_NV21);
    } catch (cv::Exception& e) {
        LOGE("OpenCV cvtColor error: %s", e.what());
        env->ReleaseByteArrayElements(input_, input, 0);
        return NULL;
    }

    int outSize = rgba.total() * rgba.elemSize();
    jbyteArray outArr = env->NewByteArray(outSize);
    env->SetByteArrayRegion(outArr, 0, outSize, (jbyte*)rgba.data);

    env->ReleaseByteArrayElements(input_, input, 0);
    return outArr;
}
