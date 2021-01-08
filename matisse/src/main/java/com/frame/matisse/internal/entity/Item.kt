package com.frame.matisse.internal.entity

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import com.frame.matisse.MimeTypeManager
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Item(val id: Long, val mimeType: String, val size: Long, val duration: Long) :
    Parcelable {

    val uri: Uri

    init {
        val contentUri = when {
            isImage() -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            isVideo() -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else -> MediaStore.Files.getContentUri("external")
        }
        uri = ContentUris.withAppendedId(contentUri, id)
    }

    fun getContentUri(): Uri = uri

    fun isCapture(): Boolean = id == ITEM_ID_CAPTURE

    fun isImage(): Boolean = MimeTypeManager.isImage(mimeType)

    fun isGif(): Boolean = MimeTypeManager.isGif(mimeType)

    fun isVideo(): Boolean = MimeTypeManager.isVideo(mimeType)

    override fun equals(other: Any?): Boolean {
        if (other !is Item) return false

        val otherItem = other as Item?
        return ((id == otherItem?.id && (mimeType == otherItem.mimeType)) && (uri == otherItem.uri) && size == otherItem.size && duration == otherItem.duration)
    }

    override fun hashCode(): Int {
        var result = 1
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + size.toString().hashCode()
        result = 31 * result + duration.toString().hashCode()
        return result
    }

    companion object {
        const val ITEM_ID_CAPTURE: Long = -1
        const val ITEM_DISPLAY_NAME_CAPTURE = "Capture"

        fun valueOf(cursor: Cursor): Item {
            return Item(
                cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)),
                cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)),
                cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)),
                cursor.getLong(cursor.getColumnIndex("duration"))
            )
        }
    }
}