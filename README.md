# Real-Time Edge Detection App
Android (Camera2) + OpenCV (C++) + OpenGL ES 2.0 + Web Viewer (TypeScript)

## Overview
This project is a minimal, modular implementation demonstrating:
- Camera2 capture (ImageReader -> YUV frames).
- Native C++ processing (OpenCV) via JNI (Canny edge detection).
- Rendering processed frames using OpenGL ES 2.0 (GLSurfaceView).
- A lightweight TypeScript-based web viewer that displays a sample processed frame.

**Important:** This repository contains all source code. You **must** install the Android NDK, CMake and provide the OpenCV Android SDK (headers & native libs) as described below before building.

## Contents
- `/app` - Android Studio project (module) with Kotlin sources and native C++ sources.
- `/web` - Plain TypeScript + HTML viewer (build with `tsc`).

## How to build (Android)
1. Install Android Studio, Android SDK, Android NDK (r21+ recommended), and CMake.
2. Download OpenCV Android SDK (e.g., OpenCV-4.x-android-sdk) from https://opencv.org/releases/
3. Unzip OpenCV SDK and note the path (called `OPENCV_ANDROID_SDK` here).
4. Copy OpenCV native libraries (`sdk/native/libs/<abi>/libopencv_java4.so`) into:
   `app/src/main/jniLibs/<abi>/libopencv_java4.so` for each ABI you want (armeabi-v7a, arm64-v8a, x86).
   Or configure `CMakeLists.txt` to point to `${OPENCV_ANDROID_SDK}/sdk/native/jni` include and libraries.
5. Open this folder in Android Studio (open the top-level `settings.gradle`).
6. Edit `app/CMakeLists.txt` to set `OpenCV_DIR` or update include/link paths according to your OpenCV SDK layout.
   Example:
   ```
   set(OpenCV_DIR /path/to/OpenCV-android-sdk/sdk/native/jni)
   find_package(OpenCV REQUIRED)
   include_directories(${OpenCV_INCLUDE_DIRS})
   ```
7. Build & run on a device (recommended) or an emulator (emulator camera support is limited).

## How to build (Web viewer)
1. Install Node.js + npm & TypeScript (`npm i -g typescript`) or use local `npx tsc`.
2. `cd web`
3. `tsc` (compiles `viewer.ts` to `viewer.js`)
4. Open `index.html` in a browser (no server required for the embedded base64 image), or serve via simple `http-server`.

## Architecture
- **MainActivity.kt** - manages Camera2, requests camera permission, and forwards each frame (NV21 byte array) to native JNI for processing.
- **NativeBridge.kt** - small wrapper that loads native library and declares JNI method `processFrame`.
- **EdgeGLRenderer.kt** - GLSurfaceView.Renderer that receives processed RGBA byte arrays from Java, uploads to GL texture and renders a fullscreen quad with a basic shader.
- **native-lib.cpp** - JNI implementation. Converts NV21 to RGBA (cv::cvtColor), applies Canny edge detector, and returns RGBA byte array to Java.
- **CMakeLists.txt** - build rules for native code. You will need to configure OpenCV include & library paths.

## Notes & Caveats
- This is a developer-focused skeleton. For production apps consider performance optimizations:
  - Avoid round-tripping byte[] between Java and native each frame; use shared ByteBuffer or direct texture uploads.
  - Use GL external textures and perform processing in native/GPU when possible.
- You must provide OpenCV binaries (not included here due to size and licensing). The README explains how to integrate them.
- The included native code compiles against OpenCV APIs; ensure CMake points to the SDK.

## Files included
See the repository tree. After adjusting OpenCV paths, you should be able to open the project in Android Studio and build.

--- End of README
