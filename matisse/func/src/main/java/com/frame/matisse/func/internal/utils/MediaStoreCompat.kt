package com.frame.matisse.func.internal.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.core.os.EnvironmentCompat
import androidx.fragment.app.Fragment
import com.frame.matisse.func.internal.entity.CaptureStrategy
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

/**
 * Title : 拍照媒体处理类
 * Author: Lsy
 * Date: 2020/12/22 5:59 PM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class MediaStoreCompat(
    activity: Activity, fragment: Fragment? = null, private var mCaptureStrategy: CaptureStrategy
) {
    companion object {
        /**
         * 检查设备是否有摄像功能。
         * Checks whether the device has a camera feature or not.
         *
         * @param context a context to check for camera feature.
         * @return true if the device has a camera feature. false otherwise.
         */
        fun hasCameraFeature(context: Context): Boolean {
            val pm = context.applicationContext.packageManager
            return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
        }
    }

    private val mContext: WeakReference<Activity> = WeakReference(activity)
    private val mFragment: WeakReference<Fragment>? =
        if (null == fragment) null else WeakReference(fragment)
    private var mCurrentPhotoUri: Uri? = null //当前照片的Uri
    private var mCurrentPhotoPath: String? = null//当前照片的路径

    /**
     * 检查设备是否有摄像功能。
     * Checks whether the device has a camera feature or not.
     *
     * @param context a context to check for camera feature.
     * @return true if the device has a camera feature. false otherwise.
     */
    fun hasCameraFeature(context: Context): Boolean =
        context.applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)

    /**
     * 设置拍照规则
     */
    fun setCaptureStrategy(strategy: CaptureStrategy) {
        mCaptureStrategy = strategy
    }

    fun getCurrentPhotoUri(): Uri? {
        return mCurrentPhotoUri
    }

    fun getCurrentPhotoPath(): String? {
        return mCurrentPhotoPath
    }

    /**
     * 打开系统相机
     */
    fun dispatchCaptureIntent(context: Context, requestCode: Int) {
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (captureIntent.resolveActivity(context.packageManager) != null) {
            mCurrentPhotoUri = createCurrentPhotoUri()
            mCurrentPhotoUri?.let {
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoUri)
                captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                if (mFragment != null) {
                    mFragment.get()!!.startActivityForResult(captureIntent, requestCode)
                } else {
                    mContext.get()!!.startActivityForResult(captureIntent, requestCode)
                }
            }
        }
    }

    /**
     * 获取拍照图片的Uri
     */
    private fun createCurrentPhotoUri(): Uri? {
        return mContext.get()?.let {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = String.format("JPEG_%s.jpg", timeStamp)

            try {
                if (Platform.beforeAndroidTen()) {
                    createImageFile(it, imageFileName)
                } else {
                    createImageFileForQ(it, imageFileName)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(context: Context, fileName: String): Uri? {
        var storageDir: File? =
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return null
        if (mCaptureStrategy.directory != null) {
            storageDir = File(storageDir, mCaptureStrategy.directory ?: "")
            if (!storageDir.exists()) storageDir.mkdirs()
        }
        // 创建文件
        val tempFile = File(storageDir, fileName)
        return if (Environment.MEDIA_MOUNTED != EnvironmentCompat.getStorageState(tempFile)) {
            null
        } else {
            mCurrentPhotoPath = tempFile.absolutePath
            FileProvider.getUriForFile(context, mCaptureStrategy.authority, tempFile)
        }
    }

    /**
     * 创建图片地址Uri，用于保存拍照后的照片(Android Q)
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createImageFileForQ(context: Context, fileName: String): Uri? {
        return mContext.get()?.let {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                val path = if (null != mCaptureStrategy.directory)
                    "${Environment.DIRECTORY_PICTURES}/${mCaptureStrategy.directory}"
                else Environment.DIRECTORY_PICTURES
                put(MediaStore.Images.Media.RELATIVE_PATH, path)
            }
            val uri = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                it.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
                )
            } else {
                it.contentResolver.insert(
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI, contentValues
                )
            }
            mCurrentPhotoPath = PathUtils.getPathFromUri(context, uri)
            uri
        }
    }
}