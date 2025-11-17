package com.example.edgeapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.util.Size
import android.view.Surface
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.hardware.camera2.*
import android.opengl.GLSurfaceView
import android.widget.Toast
import java.nio.ByteBuffer
import android.os.Handler
import android.os.HandlerThread
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var glSurface: GLSurfaceView
    private lateinit var renderer: EdgeGLRenderer
    private lateinit var fpsLabel: TextView
    private lateinit var btnToggle: Button

    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null

    private var cameraSize: Size = Size(640, 480)

    @Volatile private var showRaw = false
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private var lastFpsNs: Long = 0L
    private var frames: Int = 0

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openCamera()
        else Toast.makeText(this, "Camera permission required", Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        glSurface = findViewById(R.id.glSurface)
        fpsLabel = findViewById(R.id.fpsLabel)
        btnToggle = findViewById(R.id.btnToggle)

        // Renderer will be created after we determine a supported camera size in openCamera()

        btnToggle.setOnClickListener {
            showRaw = !showRaw
        }

        // Do not open camera here; ensure background thread is started first (in onResume)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onPause() {
        super.onPause()
        closeCamera()
        stopBackgroundThread()
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        }
    }

    private fun openCamera() {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = manager.cameraIdList[0]
            val characteristics = manager.getCameraCharacteristics(cameraId)
            // Choose a supported YUV_420_888 size close to 640x480
            cameraSize = chooseSize(characteristics, Size(640, 480))

            // Ensure background handler is available before wiring ImageReader
            if (backgroundHandler == null) startBackgroundThread()
            setupImageReader(cameraSize.width, cameraSize.height)

            // Initialize the GL renderer once with the chosen size
            if (!::renderer.isInitialized) {
                renderer = EdgeGLRenderer(cameraSize.width, cameraSize.height)
                glSurface.setEGLContextClientVersion(2)
                glSurface.setRenderer(renderer)
                glSurface.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            }

            // Use main looper for device callbacks; session callbacks use default looper below
            manager.openCamera(cameraId, stateCallback, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupImageReader(w: Int, h: Int) {
        imageReader?.close()
        imageReader = ImageReader.newInstance(w, h, ImageFormat.YUV_420_888, 2)
        imageReader!!.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
            val nv21 = imageToNV21(image)
            image.close()
            // Guard against missing native libs to prevent hard crash
            val processed = try {
                if (NativeBridge.isLoaded) {
                    if (showRaw) {
                        NativeBridge.convertNV21ToRGBA(nv21, cameraSize.width, cameraSize.height)
                    } else {
                        NativeBridge.processFrameNV21(nv21, cameraSize.width, cameraSize.height)
                    }
                } else null
            } catch (_: Throwable) { null }
            if (processed != null && ::renderer.isInitialized) {
                renderer.updateFrameBytes(processed)
            }
            updateFps()
        }, backgroundHandler)
    }

    private fun imageToNV21(image: Image): ByteArray {
        val w = image.width
        val h = image.height
        val yRowStride = image.planes[0].rowStride
        val yBuffer = image.planes[0].buffer
        val nv21 = ByteArray(w * h + (w * h) / 2)
        if (yRowStride == w) {
            yBuffer.get(nv21, 0, w * h)
        } else {
            var yPos = 0
            for (row in 0 until h) {
                yBuffer.position(row * yRowStride)
                yBuffer.get(nv21, yPos, w)
                yPos += w
            }
        }
        // This conversion is not always trivial across devices. Here we attempt simple packing (may need adjustments).
        // Interleave V and U
        var pos = w * h
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer
        val uRowStride = image.planes[1].rowStride
        val vRowStride = image.planes[2].rowStride
        val uPixelStride = image.planes[1].pixelStride
        val vPixelStride = image.planes[2].pixelStride
        for (row in 0 until h / 2) {
            for (col in 0 until w / 2) {
                val vIndex = row * vRowStride + col * vPixelStride
                val uIndex = row * uRowStride + col * uPixelStride
                nv21[pos++] = vBuffer.get(vIndex)
                nv21[pos++] = uBuffer.get(uIndex)
            }
        }
        return nv21
    }

    private fun updateFps() {
        if (lastFpsNs == 0L) {
            lastFpsNs = System.nanoTime()
            frames = 0
        }
        frames++
        val now = System.nanoTime()
        val dt = now - lastFpsNs
        if (dt >= 1_000_000_000L) {
            val fps = frames.toDouble() * 1_000_000_000.0 / dt.toDouble()
            frames = 0
            lastFpsNs = now
            runOnUiThread {
                fpsLabel.text = "FPS: " + String.format("%.1f", fps)
            }
        }
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
        } catch (e: InterruptedException) { }
        backgroundThread = null
        backgroundHandler = null
    }

    private fun chooseSize(characteristics: CameraCharacteristics, preferred: Size): Size {
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) as? android.hardware.camera2.params.StreamConfigurationMap
        val sizes: Array<Size> = map?.getOutputSizes(ImageFormat.YUV_420_888) ?: return preferred
        var exact: Size? = null
        var closest: Size? = null
        val prefArea = preferred.width * preferred.height
        for (s in sizes) {
            if (s.width == preferred.width && s.height == preferred.height) {
                exact = s
                break
            }
            val area = s.width * s.height
            if (closest == null || kotlin.math.abs(area - prefArea) < kotlin.math.abs(closest.width * closest.height - prefArea)) {
                closest = s
            }
        }
        return exact ?: closest ?: preferred
    }

    private val stateCallback = object: CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            try {
                val surface = imageReader!!.surface
                val previewRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                previewRequestBuilder.addTarget(surface)
                camera.createCaptureSession(listOf(surface), object: CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                        val request = previewRequestBuilder.build()
                        captureSession!!.setRepeatingRequest(request, null, null)
                    }
                    override fun onConfigureFailed(session: CameraCaptureSession) {}
                }, null)
            } catch (e: Exception) { e.printStackTrace() }
        }
        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            cameraDevice = null
        }
        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            cameraDevice = null
        }
    }

    private fun closeCamera() {
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
        imageReader?.close()
        imageReader = null
    }
}
