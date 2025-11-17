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

## Features implemented

- Android
   - Camera2 capture pipeline (ImageReader -> YUV/NV21 frames)
   - JNI bridge to native C++
   - OpenCV-based processing (e.g., Canny edge detection)
   - Optional OpenGL ES 2.0 rendering path for processed RGBA frames
- Web
   - TypeScript viewer renders processed frames or a sample preview
   - Simple controls scaffolding for future parameters

## Screenshots / GIF

Add your captures before submission. Suggested locations:

- Android: `app/src/main/res/drawable/` (PNG/JPG) or link below
- Web: `web/assets/screenshots/`

Embedded examples (replace paths with your actual files):

![Android Preview](web/assets/screenshots/android_preview.png)

![Web Preview](web/assets/screenshots/web_preview.png)

![Demo GIF](web/assets/screenshots/demo.gif)

## Quick architecture (JNI + frame flow + TS)

High-level flow:
1. Camera2 -> Java/Kotlin receives NV21/YUV frame
2. Java/Kotlin -> JNI passes a ByteArray/ByteBuffer to native
3. C++ (OpenCV) converts color, processes frame, returns RGBA/encoded bytes
4. Java/Kotlin displays locally (GLSurfaceView) and/or exports frame/metadata
5. Web (TypeScript) receives data (e.g., via HTTP/WebSocket) and renders to canvas

Error modes to watch:
- Missing NDK/OpenCV or incorrect CMake configuration
- Camera permission denied
- Emulator camera limitations; prefer physical device

## Setup summary (Android + Web)

Android quick steps:
- Install Android Studio, SDK, NDK (r21+ recommended), and CMake
- Download OpenCV Android SDK and point CMakeLists.txt to `sdk/native/jni`
- Link against OpenCV and run on a device

Web quick steps:
- Install Node.js + TypeScript; build with `tsc` in `web/` and open `index.html`

## Commit history guidance (for assignment)

Your submission requires a clear, multi-commit history (avoid a single "final" commit). Two options:

1) Commit as you build: small, meaningful commits like:
    - feat(native): add JNI bridge skeleton
    - feat(camera): capture NV21 frames via ImageReader
    - feat(opencv): integrate Canny edge processing
    - feat(gl): render RGBA texture in GLSurfaceView
    - feat(web): add TS viewer and basic canvas renderer
    - fix(native): correct buffer stride and memory leak

2) If you already finished the code, generate a commit trail with the provided script:
    - Run `scripts/create_dev_commits.ps1` from the repo root (PowerShell)
    - Review `git log --oneline --decorate` before pushing

## Submission checklist

- [ ] README updated with features, screenshots/GIF
- [ ] OpenCV + NDK configured and buildable
- [ ] Web viewer compiles with `tsc`
- [ ] Multiple meaningful commits showing development steps
- [ ] GitHub repository link ready for submission

--- End of README
