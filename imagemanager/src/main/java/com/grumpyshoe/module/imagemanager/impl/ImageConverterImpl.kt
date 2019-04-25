package com.grumpyshoe.module.imagemanager.impl

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.grumpyshoe.module.imagemanager.ImageManager
import java.io.ByteArrayOutputStream


class ImageConverterImpl : ImageManager.ImageConverter {


    /**
     * return value without any size checks
     *
     * @param filePath
     * @return base64 string
     */
    override fun toBase64(filePath: String): String {

        // get image as byte array
        val b =
            fileToByteArray(filePath) ?: throw java.lang.IllegalArgumentException("File $filePath could not vbe found")

        // convert byte array to base64 encoded string
        return Base64.encodeToString(b, Base64.NO_WRAP)

    }


    /**
     * return value without any size checks
     *
     * @param bitmap
     * @param compressFormat
     * @param quality
     * @return base 64 string
     */
    override fun toBase64(bitmap: Bitmap, compressFormat: Bitmap.CompressFormat, quality: Int): String {

        val byteArrayBitmapStream = ByteArrayOutputStream()
        bitmap.compress(compressFormat, quality, byteArrayBitmapStream)
        val b = byteArrayBitmapStream.toByteArray()
        val b64 = Base64.encodeToString(b, Base64.NO_WRAP)
        return b64
    }


    /**
     * convert image to byte array
     *
     * @param filePath
     * @return
     */
    private fun fileToByteArray(filePath: String): ByteArray? {
        try {
            val bm = BitmapFactory.decodeFile(filePath)
            val baos = ByteArrayOutputStream()
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            bm.recycle()

            val b = baos.toByteArray()
            baos.close()
            return b
        } catch (e: Exception) {
            Log.e("ImageManager.Converter", e.message, e)
        }

        return null
    }
}