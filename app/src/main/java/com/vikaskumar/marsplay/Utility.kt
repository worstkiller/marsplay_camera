package com.vikaskumar.marsplay

import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.view.WindowManager


class Utility {


    companion object {

        /**
         * checks if sd card is available
         * @param context
         */
        fun isSdCardAvailable(context: Context): Boolean {
            val sd = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED.equals(sd)
        }

        /**
         * checks if camera is available
         * @param context
         */
        fun isCameraAvailable(context: Context): Boolean {
            return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
        }


        /**
         * get device height for proper size and ratio
         */
        fun getDeviceHeight(context: Context): Int {
            return (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.height
        }

        /**
         * get device width
         */
        fun getDeviceWidth(context: Context): Int {
            return (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.width
        }

        /**
         * get device aspect ratio
         */
        fun getAspectRatio(context: Context): Float {
            return getDeviceWidth(context).toFloat() / getDeviceHeight(context).toFloat()
        }

    }
}