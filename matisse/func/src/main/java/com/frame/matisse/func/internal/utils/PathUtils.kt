package com.frame.matisse.func.internal.utils

import android.annotation.TargetApi
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.frame.matisse.func.internal.utils.Platform.afterKitKat

/**
 * http://stackoverflow.com/a/27271131/4739220
 */
object PathUtils {
    private val SCHEME_CONTENT = "content"
    private val SCHEME_FILE = "file"

    /**
     * 根据Uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    fun getPathFromUri(context: Context, uri: Uri?): String? =
        uri?.let {
            if (SCHEME_CONTENT == uri.scheme) {
                return if (afterKitKat()) {
                    getRealPathFromUriAboveApi19(context, uri)
                } else {
                    getRealPathFromUriBelowAPI19(context.contentResolver, uri)
                }
            }
            return uri.path
        }

    /**
     * 获取Uri路径
     * @param resolver 内容解析器
     * @param uri 图片Uri
     */
    private fun getRealPathFromUriBelowAPI19(resolver: ContentResolver, uri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            cursor = resolver.query(
                uri, arrayOf(MediaStore.Images.ImageColumns.DATA),
                null, null, null
            )
            if (cursor == null || !cursor.moveToFirst()) {
                null
            } else cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA))
        } finally {
            cursor?.close()
        }
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun getRealPathFromUriAboveApi19(context: Context, uri: Uri): String? {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            val docId = DocumentsContract.getDocumentId(uri)
            if (isExternalStorageDocument(uri)) {
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return context.getExternalFilesDir(split[1])?.absolutePath
                }
            } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), docId.toLong()
                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) { // MediaProvider
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                when (type) {
                    "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if (SCHEME_CONTENT.equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if (SCHEME_FILE.equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private fun getDataColumn(
        context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = MediaStore.Images.ImageColumns.DATA
        val projection = arrayOf(column)
        try {
            cursor =
                context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }
}