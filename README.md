<div align="center">

# StreamBridge

Android Camera2 + OpenCV (C++) + OpenGL ES 2.0 + Web Viewer (TypeScript)

[![Android](https://img.shields.io/badge/Android-AGP%208.x-3DDC84?logo=android&logoColor=white)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.x-7F52FF?logo=kotlin&logoColor=white)](#)
[![OpenCV](https://img.shields.io/badge/OpenCV-4.x-5C3EE8?logo=opencv&logoColor=white)](#)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.x-3178C6?logo=typescript&logoColor=white)](#)
[![License: ISC](https://img.shields.io/badge/license-ISC-blue)](#license)

<sub>Real‑time edge detection on Android with a simple web viewer.</sub>

</div>

---

## 🧾 Description

StreamBridge is a real‑time edge‑detection pipeline for Android that bridges on‑device image processing (OpenCV/C++) with efficient rendering (OpenGL ES 2.0) and an optional web viewer (TypeScript). It is designed as a clean, educational codebase you can clone and extend to your needs: prototype vision algorithms, experiment with camera pipelines, or evolve into full streaming (WebSocket/WebRTC) scenarios.

Why it exists
- Provide a minimal, understandable Camera2 → JNI → OpenCV → GL flow without Android Studio boilerplate.
- Teach how to pass NV21 frames to C++, do zero‑surprise color conversion, and return RGBA efficiently.
- Offer clear integration points for adding transport (saving, serving, or streaming frames) to the web.

Who it’s for
- Android/Kotlin developers who want native performance for image processing.
- Students/researchers prototyping computer-vision ideas on real hardware.
- Teams building a baseline for future streaming or analytics features.

What you get
- Production‑ready skeleton with tight loops and explicit configuration.
- Two OpenCV integration modes (CMake or bundled libs) to match your workflow.
- A tiny TS viewer you can connect to any future transport.

## 🧭 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [Quick Start](#-quick-start)
    - [Android: Build & Run](#android-build--run)
    - [Web Viewer](#web-viewer)
- [OpenCV Integration Details](#-opencv-integration-details)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Screenshots](#-screenshots)
- [Troubleshooting](#-troubleshooting)
- [Performance Notes](#-performance-notes)
- [Roadmap](#-roadmap)
- [FAQ](#-faq)
- [Contributing](#-contributing)
- [License](#-license)
- [Acknowledgements](#-acknowledgements)

## 📌 Overview

StreamBridge is a compact, production‑ready skeleton that:
- Captures camera frames with Camera2 (YUV_420_888 → NV21).
- Processes frames natively with OpenCV (C++) via JNI (Canny edges out‑of‑the‑box).
- Renders frames efficiently using OpenGL ES 2.0 (GLSurfaceView).
- Ships a minimal TypeScript web viewer for preview or future streaming.

**Important:** This repository contains all source code. You **must** install the Android NDK, CMake and provide the OpenCV Android SDK (headers & native libs) as described below before building.

## ✨ Key Features

- Camera2 capture pipeline with background threading.
- JNI bridge and native OpenCV processing (NV21 → RGBA → Canny → RGBA).
- GPU path using a single RGBA texture upload per frame.
- Modular layout: Kotlin UI/capture, C++ processing, GL rendering, TypeScript viewer.
- Clear integration points to extend into WebSocket/WebRTC streaming.

## ⚡ Quick Start

Prerequisites
- Android Studio (AGP 8.x), SDK Platform 34+ (or your target), NDK, CMake
- JDK 17 for Gradle
- OpenCV‑android‑sdk 4.x (download from opencv.org)
- Node.js 18+ for the web viewer

### Android: Build & Run
1) Install tools: Android Studio, SDK, NDK, CMake.
2) Download and unzip OpenCV‑android‑sdk 4.x.
3) Integrate OpenCV (choose one):
   - Option A (CMake): Set `OpenCV_DIR` to `.../OpenCV-android-sdk/sdk/native/jni` in `app/CMakeLists.txt` or pass Gradle cmake argument.
   - Option B (jniLibs): Copy `libopencv_java4.so` (and `libc++_shared.so`) into `app/src/main/jniLibs/<abi>/`.
4) Open the repo root in Android Studio (open `settings.gradle`).
5) Sync Gradle, then run on a physical device (emulator cameras are limited).

PowerShell build commands (optional):

```powershell
Set-Location "A:\shinobi no shuriken\github repo\FLAM\StreamBridge"
./gradlew clean assembleDebug
```

### Web Viewer
1) Open a terminal in the repo root.
2) Compile TypeScript and serve `web/` locally:

```powershell
cd web
npx tsc
npx http-server -c-1
```

3) Open the printed `http://127.0.0.1:<port>` and load `index.html`.

## 🛠️ OpenCV Integration Details

Option A — CMake (recommended)
```cmake
set(OpenCV_DIR A:/OpenCV-android-sdk/sdk/native/jni)
find_package(OpenCV REQUIRED)
include_directories(${OpenCV_INCLUDE_DIRS})
target_link_libraries(native-lib ${OpenCV_LIBS} c++_shared)
```

Gradle can also pass the variable:
```groovy
externalNativeBuild {
    cmake {
        arguments "-DOpenCV_DIR=A:/OpenCV-android-sdk/sdk/native/jni"
    }
}
```

Option B — jniLibs (manual)
- Create `app/src/main/jniLibs/arm64-v8a/` (and other ABIs you need).
- Copy from OpenCV SDK: `sdk/native/libs/arm64-v8a/libopencv_java4.so`.
- Copy from your NDK: `libc++_shared.so` (path varies by NDK version).
- To simplify, restrict to one ABI:
```groovy
defaultConfig {
    ndk { abiFilters "arm64-v8a" }
}
```

## 🧩 Architecture

Core modules
- `MainActivity.kt`: Camera2 setup, permissions, frame collection (NV21).
- `NativeBridge.kt`: Loads native libs, JNI APIs for processing.
- `EdgeGLRenderer.kt`: Uploads RGBA to a texture and renders a fullscreen quad.
- `native-lib.cpp`: NV21→RGBA, Canny, GRAY→RGBA, returns bytes to JVM.
- `CMakeLists.txt`: Native build + OpenCV linking.
- `web/viewer.ts`: Minimal canvas viewer (extensible to realtime streaming).

Data flow

```
Camera2 (YUV_420_888)
   → Kotlin NV21 buffer
   → JNI bridge
   → C++ OpenCV (color convert + edges)
   → RGBA bytes
   → OpenGL texture upload
   → On‑screen frame
   → (optional) export/stream to the web
```

## 🗂️ Project Structure

- `app/` — Android module (Kotlin + C++ + GL)
- `web/` — TypeScript viewer (`viewer.ts`, `index.html`, assets)
- `scripts/` — helper scripts (e.g., `create_dev_commits.ps1`)
- `build.gradle`, `settings.gradle`, `gradle.properties` — build config
- `.gitignore` — excludes build outputs, IDE caches, secrets

## 🖼️ Screenshots

Add your captures under `web/assets/screenshots/` or link externally:

![Android Preview](web/assets/screenshots/android_preview.png)
![Web Preview](web/assets/screenshots/web_preview.png)
![Demo GIF](web/assets/screenshots/demo.gif)

## 🛣️ Roadmap

- Live preview streaming over WebSocket (simple MJPEG or RGBA chunking)
- Optional WebRTC path (low‑latency, bidirectional control)
- On‑device parameter controls (thresholds, modes) with persisted settings
- Zero‑copy paths (External OES textures; GPU processing experiments)
- Packaging script to fetch OpenCV SDK and verify `jniLibs` completeness

## ❓ FAQ

- Why not just process on the CPU in Kotlin?
    - OpenCV’s native SIMD/NEON paths and better memory control reduce GC pressure and improve throughput.
- Do I need to commit `.so` files?
    - Not required if you use CMake + OpenCV SDK. If you prefer bundling, commit only the ABIs you ship.
- Will it run on an emulator?
    - Camera is limited on emulators; use a physical device for reliable results.
- How do I stream frames to the web?
    - Add a transport (e.g., WebSocket server in the app or a companion desktop/server) and feed RGBA/JPEG to the viewer.

## 🧪 Troubleshooting

- OpenCV not found during CMake: ensure `OpenCV_DIR` points to `.../sdk/native/jni`.
- `UnsatisfiedLinkError: libc++_shared.so`: include it in `jniLibs/<abi>/` or link via CMake.
- `MergeNativeLibs` ABI errors: put `.so` files under ABI folders, not directly under `jniLibs/`.
- Emulator camera quirks: prefer a physical device for reliable camera feeds.

## 🚀 Performance Notes

- Start with 640×480; raise resolution gradually while monitoring FPS.
- Avoid copying large buffers repeatedly; prefer direct ByteBuffer/texture paths.
- Future optimizations: external textures, GPU image processing, zero‑copy pipelines.

## 🤝 Contributing

1. Fork and clone
2. Create a feature branch
3. Keep changes small and descriptive
4. Open a PR with screenshots and rationale

Tip: If you need to generate a commit history after development, run `scripts/create_dev_commits.ps1` (PowerShell) and review with `git log --oneline`.

## 📝 License

This project is licensed under the ISC License. See the license field in `package.json`.

## 🙏 Acknowledgements

- OpenCV team for the Android SDK
- Android frameworks and samples for Camera2 and GL patterns
- TypeScript community for simple tooling

[↑ Back to top](#streambridge)

