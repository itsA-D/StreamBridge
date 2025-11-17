package com.example.edgeapp

object NativeBridge {
    @Volatile var isLoaded: Boolean = false
        private set

    init {
        try {
            System.loadLibrary("c++_shared")
            System.loadLibrary("opencv_java4")
            System.loadLibrary("native-lib")
            isLoaded = true
        } catch (t: Throwable) {
            isLoaded = false
        }
    }

    external fun processFrameNV21(input: ByteArray, width: Int, height: Int): ByteArray?
    external fun convertNV21ToRGBA(input: ByteArray, width: Int, height: Int): ByteArray?
}
