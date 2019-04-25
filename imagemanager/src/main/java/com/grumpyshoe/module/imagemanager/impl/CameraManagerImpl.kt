package com.grumpyshoe.module.imagemanager.impl

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.grumpyshoe.getimage.R
import com.grumpyshoe.module.imagemanager.ImageManager
import com.grumpyshoe.module.intentutils.openForResult
import com.grumpyshoe.module.permissionmanager.PermissionManager
import com.grumpyshoe.module.permissionmanager.model.PermissionRequestExplanation
import java.io.File
import java.io.IOException

class CameraManagerImpl(val permissionManager: PermissionManager) :
    ImageManager.CameraManager {

    private var cameraImageUri: Uri? = null


    /**
     * check permission for handling camera
     *
     * @param activity - activity source
     *
     */
    override fun selectImageFromCamera(activity: Activity): Int {

        permissionManager.checkPermissions(
            activity = activity,
            permissions = arrayOf(Manifest.permission.CAMERA),
            onPermissionResult = { permissionResult ->
                triggerCamera(activity)
            },
            permissionRequestPreExecuteExplanation = PermissionRequestExplanation(
                title = activity.getString(R.string.imagemanager_camera_permission_explanation_title),
                message = activity.getString(R.string.imagemanager_camera_permission_explanation_message)
            ),
            permissionRequestRetryExplanation = PermissionRequestExplanation(
                title = activity.getString(R.string.imagemanager_camera_permission_explanation_retry_title),
                message = activity.getString(R.string.imagemanager_camera_permission_explanation_retry_message)
            ),
            requestCode = ImageManager.ImageSources.CAMERA.permissionRequestCode
        )

        return ImageManager.ImageSources.CAMERA.permissionRequestCode

    }


    /**
     * trigger camera to create image
     *
     * @param activity - base activity to start other actvities
     */
    @Throws(IOException::class)
    override fun triggerCamera(activity: Activity) {

        val context = activity.applicationContext
        val filename = "photo"

        val previousFile = File(activity.externalCacheDir.toString() + filename + ".jpg")
        if (previousFile.exists()) {
            previousFile.delete()
        }

        val photoFile = File.createTempFile(filename, ".jpg", activity.externalCacheDir)
        cameraImageUri = FileProvider.getUriForFile(activity, activity.packageName + ".fileprovider", photoFile)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val resolvedIntentActivities =
            context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolvedIntentInfo in resolvedIntentActivities) {
            val packageName = resolvedIntentInfo.activityInfo.packageName

            context.grantUriPermission(
                packageName,
                cameraImageUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
        intent.openForResult(activity, ImageManager.ImageSources.CAMERA.dataRequestCode)
    }


    /**
     * handle intent result
     *
     * @param context - Context source
     * @param onResult - function called on successfully image handling
     *
     * @return flag if the result has been handeld
     */
    override fun onIntentResult(context: Context, onResult: (Bitmap) -> Unit): Boolean {

        var bitmap: Bitmap? = null
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, cameraImageUri)

            onResult(bitmap)

            return true
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return false
    }
}