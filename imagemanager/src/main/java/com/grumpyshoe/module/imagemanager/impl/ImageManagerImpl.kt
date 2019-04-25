package com.grumpyshoe.module.imagemanager.impl

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.grumpyshoe.getimage.R
import com.grumpyshoe.module.imagemanager.ImageManager
import com.grumpyshoe.module.permissionmanager.PermissionManager
import com.grumpyshoe.module.permissionmanager.impl.PermissionManagerImpl


class ImageManagerImpl : ImageManager {

    val permissionManager: PermissionManager = PermissionManagerImpl
    override var cameraManager: ImageManager.CameraManager =
        CameraManagerImpl(permissionManager)
    override var galleryManager: ImageManager.GalleryManager =
        GalleryManagerImpl(permissionManager)
    override var imageConverter: ImageManager.ImageConverter =
        ImageConverterImpl()


    private lateinit var mCurrectActivity: Activity
    private lateinit var onImageReceived: (Bitmap) -> Unit
    private var requestCodeTrigger: Int = 0

    override fun getImage(
        activity: Activity,
        sources: List<ImageManager.ImageSources>,
        onImageReceived: (Bitmap) -> Unit
    ) {

        if (sources.isEmpty()) {
            throw IllegalArgumentException("At least one source needs to be defined")
        }

        this.onImageReceived = onImageReceived
        mCurrectActivity = activity

        if (sources.size == 1) {

            // choose object from source directly
            if (sources[0].equals(ImageManager.ImageSources.CAMERA)) {
                requestCodeTrigger = cameraManager.selectImageFromCamera(activity)
            } else {
                requestCodeTrigger = galleryManager.selectImageFromGallery(activity)
            }
        } else {

            // start dialog to select source
            showSourceChooserDialog(
                dialogTitle = activity.getString(R.string.imagemanager_source_chooser_dialog_title),
                takePhotoTitle = activity.getString(R.string.imagemanager_add_image_from_camera_dialog_title),
                getImageFromGallerytitle = activity.getString(R.string.imagemanager_add_image_from_gallery_dialog_title)
            )
        }

    }


    /**
     * Builds Dialog for user to choose where to load image from.
     * Possibilities are gallery or photo.
     *
     * Triggers imagesource according to users choice
     *
     * @param activity
     * @param dialogTitle
     * @param takePhotoTitle
     * @param getImageFromGallerytitle
     * @param callback
     */
    fun showSourceChooserDialog(
        dialogTitle: String,
        takePhotoTitle: String,
        getImageFromGallerytitle: String
    ) {

        val items = arrayOf<CharSequence>(takePhotoTitle, getImageFromGallerytitle)

        val builder = AlertDialog.Builder(mCurrectActivity)
        builder.setTitle(dialogTitle)
        builder.setItems(items) { dialog, index ->
            if (items[index] == takePhotoTitle) {
                requestCodeTrigger = cameraManager.selectImageFromCamera(mCurrectActivity)
            } else {
                requestCodeTrigger = galleryManager.selectImageFromGallery(mCurrectActivity)
            }
        }
        builder.setNegativeButton("Abbrechen", null)
        Handler(Looper.getMainLooper()).post { builder.create().show() }
    }


    /**
     * handle request permission result for creating/getting a picture
     *
     * @param requestCode - the requestcode to identifiy trigger
     * @param permissions - list of permissions
     * @param grantResults - grant result
     *
     * @return flag if the result has been handeld
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {

        return if (requestCode == ImageManager.ImageSources.CAMERA.permissionRequestCode && requestCodeTrigger == ImageManager.ImageSources.CAMERA.permissionRequestCode) {

            // check if permission has been requested for camera and the last requested permission is also camera

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraManager.triggerCamera(mCurrectActivity)
            }
            true

        } else if (requestCode == ImageManager.ImageSources.GALLERY.permissionRequestCode && requestCodeTrigger == ImageManager.ImageSources.GALLERY.permissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                galleryManager.triggerGallery(mCurrectActivity)
            }
            true
        } else {
            false
        }
    }


    /**
     * Gets image mimeType on given path
     *
     * @param imagePath
     */
    override fun getMimeType(imagePath: String): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(imagePath)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }


    /**
     * handle activity result for creating/getting a picture
     *
     * @param context - the context where the result has been catched
     * @param requestCode - the requestcode to identifiy trigger
     * @param resultCode - the result code
     * @param intent - the intent returned by the previous activity
     *
     * @return flag if the result has been handeld
     */
    override fun onActivityResult(context: Context, requestCode: Int, resultCode: Int, intent: Intent?): Boolean {

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == ImageManager.ImageSources.CAMERA.dataRequestCode && intent != null) {

                return cameraManager.onIntentResult(context) {
                    onImageReceived(it)
                }

            } else if (requestCode == ImageManager.ImageSources.GALLERY.dataRequestCode && intent != null && intent.data != null) {
                return galleryManager.onIntentResult(intent.data, mCurrectActivity) {
                    onImageReceived(it)
                }
            }
        }

        return false
    }
}