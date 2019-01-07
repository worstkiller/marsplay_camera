@file:Suppress("DEPRECATION")

package com.vikaskumar.marsplay

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Camera
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.vikaskumar.marsplay.Utility.Companion.getDeviceHeight
import com.vikaskumar.marsplay.Utility.Companion.getDeviceWidth
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity(), Camera.PictureCallback {

    private lateinit var camera: Camera
    private val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    companion object {
        const val TAG = "MAIN_ACTIVITY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Optional: Hide the status bar at the top of the window
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        initViews()
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        setupCaptureListener()
    }

    private fun setupCaptureListener() {
        ivCapture.setOnClickListener { camera.takePicture(null, null, this) }
    }

    private fun askPermissionIfApplicable() {
        for (item in permissions) {
            val permission = ActivityCompat.checkSelfPermission(this@MainActivity, item)
            if (permission == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this@MainActivity, permissions, 101)
                return
            }
        }

        setUpCameraPreview()
    }

    override fun onResume() {
        super.onResume()
        if (!Utility.isCameraAvailable(this@MainActivity)) {
            //show some error message
            showMessage(getString(R.string.no_camera_found))
            return
        }
        if (!Utility.isSdCardAvailable(this@MainActivity)) {
            //show some error message
            showMessage(getString(R.string.no_sd_card))
            return
        }

        askPermissionIfApplicable()
    }

    /**
     * create a camera here and other initialization goes
     */
    private fun setUpCameraPreview() {
        camera = getCameraInstance()!!
        val cameraPreview = CameraPreview(this@MainActivity, camera)
        val context = this@MainActivity
        val layoutParams = LinearLayout.LayoutParams(getDeviceWidth(context), getDeviceHeight(context))
        cameraPreview.layoutParams = layoutParams

        // Adding the camera preview after the FrameLayout
        flCamera.addView(cameraPreview, 0)
    }

    /**
     * get instance of camera
     */
    private fun getCameraInstance(): Camera? {
        return try {
            Camera.open()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onPause() {
        super.onPause()
        removeCameraResource()
    }

    /**
     * release all the resources attached with camera preview
     */
    private fun removeCameraResource() {
        flCamera.removeAllViews()
        if (::camera.isInitialized) {
            camera.release()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.menuGallery) {
            //here open the gallery
            showMessage("Gallery is opening shortly")
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            var count = 0
            for (results in grantResults) {
                if (results == PackageManager.PERMISSION_DENIED) {
                    count++
                }
            }
            if (count == 0) {
                setUpCameraPreview()
            } else {
                showMessage(getString(R.string.permission_required_placeholder))
                finish()
            }
        }
        Log.d(TAG, "on permission result called")
    }

    /**
     * show message
     */
    private fun showMessage(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
        val picture: File = Utility.getOutputMediaFile(Utility.MEDIA_TYPE_IMAGE) ?: run {
            Log.d(TAG, ("Error creating media file, check storage permissions"))
            return
        }
        try {
            val outputStream = FileOutputStream(picture)
            outputStream.write(data)
            outputStream.close()
            displayThumbnail(picture)
            camera!!.stopPreview()
            camera.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun displayThumbnail(picture: File) {
        val alert = AlertDialog.Builder(this@MainActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen).create()
        val view = layoutInflater.inflate(R.layout.layout_thumbnail, null, false)
        view.findViewById<ImageView>(R.id.ivClose).setOnClickListener { alert.dismiss() }
        view.findViewById<ImageView>(R.id.ivThumbnail).setImageURI(Uri.fromFile(picture))
        alert.setView(view)
        alert.setCancelable(false)
        val params = alert.getWindow().getAttributes()
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        alert.getWindow().setAttributes(params as android.view.WindowManager.LayoutParams)
        alert.show()
    }

}
