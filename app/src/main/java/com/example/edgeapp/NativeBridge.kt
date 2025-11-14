package com.example.edgeapp

object NativeBridge {
    init {
        System.loadLibrary("native-lib")
    }

    // Accepts NV21 bytes and returns processed RGBA bytes (width*height*4) or null on error.
    external fun processFrameNV21(input: ByteArray, width: Int, height: Int): ByteArray?
    
    external fun convertNV21ToRGBA(input: ByteArray, width: Int, height: Int): ByteArray?
}
