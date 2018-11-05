@file:Suppress("DEPRECATION")

package com.vikaskumar.marsplay

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.*
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import com.vikaskumar.marsplay.Utility.Companion.getDeviceHeight
import com.vikaskumar.marsplay.Utility.Companion.getDeviceWidth


class MainActivity : AppCompatActivity() {

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
}
