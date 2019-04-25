package com.grumpyshoe.module.imagemanager.impl

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.grumpyshoe.getimage.R
import com.grumpyshoe.module.imagemanager.ImageManager
import com.grumpyshoe.module.intentutils.open
import com.grumpyshoe.module.intentutils.openForResult
import com.grumpyshoe.module.permissionmanager.PermissionManager
import com.grumpyshoe.module.permissionmanager.model.PermissionRequestExplanation

class GalleryManagerImpl(val permissionManager: PermissionManager) :
    ImageManager.GalleryManager {

    private val LOG_TAG = "ImageManagerImpl"
    private lateinit var mFilePath: String


    /**
     * check permission for handling gallery images
     *
     * @param activity - activity source
     *
     */
    override fun selectImageFromGallery(activity: Activity):Int {

        permissionManager.checkPermissions(
            activity = activity,
            permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            onPermissionResult = { permissionResult ->
                triggerGallery(activity)
            },
            permissionRequestPreExecuteExplanation = PermissionRequestExplanation(
                title = activity.getString(R.string.imagemanager_gallery_permission_explanation_title),
                message = activity.getString(R.string.imagemanager_gallery_permission_explanation_message)
            ),
            permissionRequestRetryExplanation = PermissionRequestExplanation(
                title = activity.getString(R.string.imagemanager_gallery_permission_explanation_retry_title),
                message = activity.getString(R.string.imagemanager_gallery_permission_explanation_retry_message)
            ),
            requestCode = ImageManager.ImageSources.GALLERY.permissionRequestCode
        )

        return ImageManager.ImageSources.GALLERY.permissionRequestCode
    }


    /**
     * trigger gallery to choose an image
     *
     * @param activity - base activity to start other actvities
     *
     */
    override fun triggerGallery(activity: Activity) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_PICK

        intent.openForResult(activity, ImageManager.ImageSources.GALLERY.dataRequestCode)
    }


    /**
     * handle intent result
     *
     * @param uri - Uri of image file
     * @param activity - activity source
     * @param onResult - function called on successfully image handling
     *
     * @return flag if the result has been handeld
     */
    override fun onIntentResult(uri: Uri, activity: Activity, onResult: (Bitmap) -> Unit): Boolean {
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            val cursor =
                activity.applicationContext.contentResolver.query(
                    uri,
                    proj,
                    null,
                    null,
                    null
                )
            if (cursor != null) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()
                val s = cursor.getString(columnIndex)
                cursor.close()

                val bmOptions = BitmapFactory.Options()
                val bitmap = BitmapFactory.decodeFile(s, bmOptions)
                onResult(bitmap)

                return true
            }
        } catch (ex: IllegalArgumentException) {
            Log.e(LOG_TAG, ex.message, ex)
        }
        return false
    }


}