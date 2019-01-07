package com.vikaskumar.marsplay

import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import android.view.WindowManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import java.io.IOException
import android.media.ExifInterface
import android.os.Build


class Utility {


    companion object {

        val MEDIA_TYPE_IMAGE = 1
        val MEDIA_TYPE_VIDEO = 2

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

        /** Create a File for saving an image or video */
        public fun getOutputMediaFile(type: Int): File? {
            // To be safe, you should check that the SDCard is mounted
            // using Environment.getExternalStorageState() before doing this.

            val mediaStorageDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MarsPlay"
            )
            // This location works best if you want the created images to be shared
            // between applications and persist after your app has been uninstalled.

            // Create the storage directory if it does not exist
            mediaStorageDir.apply {
                if (!exists()) {
                    if (!mkdirs()) {
                        Log.d("MarsPlay", "failed to create directory")
                        return null
                    }
                }
            }

            // Create a media file name
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            return when (type) {
                MEDIA_TYPE_IMAGE -> {
                    File("${mediaStorageDir.path}${File.separator}IMG_$timeStamp.jpg")
                }
                MEDIA_TYPE_VIDEO -> {
                    File("${mediaStorageDir.path}${File.separator}VID_$timeStamp.mp4")
                }
                else -> null
            }
        }

        @Throws(IOException::class)
        fun handleSamplingAndRotationBitmap(context: Context, selectedImage: Uri): Bitmap? {
            val MAX_HEIGHT = 1024
            val MAX_WIDTH = 1024

            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            var imageStream = context.contentResolver.openInputStream(selectedImage)
            BitmapFactory.decodeStream(imageStream, null, options)
            imageStream!!.close()

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT)

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            imageStream = context.contentResolver.openInputStream(selectedImage)
            var img = BitmapFactory.decodeStream(imageStream, null, options)

            img = rotateImageIfRequired(context, img, selectedImage)
            return img
        }

        private fun calculateInSampleSize(
            options: BitmapFactory.Options,
            reqWidth: Int, reqHeight: Int
        ): Int {
            // Raw height and width of image
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {

                // Calculate ratios of height and width to requested height and width
                val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
                val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())

                // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
                // with both dimensions larger than or equal to the requested height and width.
                inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio

                // This offers some additional logic in case the image has a strange
                // aspect ratio. For example, a panorama may have a much larger
                // width than height. In these cases the total pixels might still
                // end up being too large to fit comfortably in memory, so we should
                // be more aggressive with sample down the image (=larger inSampleSize).

                val totalPixels = (width * height).toFloat()

                // Anything more than 2x the requested pixels we'll sample down further
                val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()

                while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                    inSampleSize++
                }
            }
            return inSampleSize
        }

        @Throws(IOException::class)
        private fun rotateImageIfRequired(context: Context, img: Bitmap, selectedImage: Uri): Bitmap {

            val input = context.contentResolver.openInputStream(selectedImage)
            val ei: ExifInterface
            if (Build.VERSION.SDK_INT > 23)
                ei = ExifInterface(input)
            else
                ei = ExifInterface(selectedImage.path)

            val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> return rotateImage(img, 90)
                ExifInterface.ORIENTATION_ROTATE_180 -> return rotateImage(img, 180)
                ExifInterface.ORIENTATION_ROTATE_270 -> return rotateImage(img, 270)
                else -> return img
            }
        }

        private fun rotateImage(img: Bitmap, degree: Int): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(degree.toFloat())
            val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
            img.recycle()
            return rotatedImg
        }

    }
}