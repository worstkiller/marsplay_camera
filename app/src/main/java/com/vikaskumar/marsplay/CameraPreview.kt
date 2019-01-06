@file:Suppress("DEPRECATION")

package com.vikaskumar.marsplay

import android.content.Context
import android.graphics.PixelFormat
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.IOException

/**
 * camera preview class
 */
class CameraPreview(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private lateinit var cameraLocal: Camera
    private var surfaceHolder: SurfaceHolder = holder
    private var defaultPicHeight = 720
    private var defaultPicWidth = 1280


    constructor(context: Context, camera: Camera) : this(context) {
        cameraLocal = camera
        applyCameraSettings(100)
    }

    companion object {
        const val TAG = "CAMERA_PREVIEW"
    }

    init {
        surfaceHolder.addCallback(this)
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

        if (surfaceHolder.surface == null) return

        // start preview with new settings
        try {
            cameraLocal.stopPreview()
            cameraLocal.setDisplayOrientation(90)
            cameraLocal.setPreviewDisplay(surfaceHolder)
            cameraLocal.startPreview()

        } catch (e: Exception) {
            Log.d(TAG, "Error starting camera preview: " + e.message)
        }

    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        //release resources here
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        //start preview settings here
        try {
            cameraLocal.setPreviewDisplay(holder)
            cameraLocal.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(TAG, "Error setting camera preview")
        }
    }

    /**
     * Setting the right parameters in the camera
     */
    private fun applyCameraSettings(quality: Int) {
        val params = getCameraParams()
        params.setPictureSize(getDefaultWidth(), getDefaultHeight())
        params.pictureFormat = PixelFormat.JPEG
        params.jpegQuality = quality
        params.focusMode = if (params.focusMode.equals(Camera.Parameters.FOCUS_MODE_AUTO)) Camera.Parameters.FOCUS_MODE_AUTO else params.focusMode
        cameraLocal.parameters = params
    }

    /**
     * get default height of the pic
     */
    fun getDefaultHeight(): Int {
        return defaultPicHeight
    }

    /**
     * get default width of the pic
     */
    fun getDefaultWidth(): Int {
        return defaultPicWidth
    }

    /**
     * set the default height of the pic
     */
    fun setDefaultHeight(height: Int) {
        this.defaultPicHeight = height
    }

    /**
     * set the default width of the pic
     */
    fun setDefaultWidth(width: Int) {
        this.defaultPicWidth = width
    }

    /**
     * get the maximum possible camera size
     */
    fun getPreviewWidth(): Int {
        return getCameraParams().previewSize.width
    }

    /**
     * get the preview height
     */
    fun getPreviewHeight(): Int {
        return getCameraParams().previewSize.height
    }

    /**
     * get the camera parameters
     */
    private fun getCameraParams(): Camera.Parameters {
        return cameraLocal.parameters
    }
}