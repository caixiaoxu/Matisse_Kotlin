package com.frame.matisse.func.internal.entity

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Parcelable
import com.frame.matisse.func.R
import com.frame.matisse.func.internal.loader.AlbumLoader
import kotlinx.android.parcel.Parcelize

/**
 * Title : 相册专辑数据类
 * Author: Lsy
 * Date: 2020/12/22 5:24 PM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
@Parcelize
data class Album(
    private val mId: String, private val mCoverUri: Uri,
    private val mDisplayName: String, private var mCount: Long
) : Parcelable {
    companion object {
        val ALBUM_ID_ALL: String = "-1"
        const val ALBUM_NAME_ALL = "All"

        /**
         * Constructs a new [Album] entity from the [Cursor].
         * This method is not responsible for managing cursor resource, such as close, iterate, and so on.
         */
        fun valueOf(cursor: Cursor): Album {
            val clumn = cursor.getString(cursor.getColumnIndex(AlbumLoader.COLUMN_URI))
            return Album(
                cursor.getString(cursor.getColumnIndex("bucket_id")),
                Uri.parse(clumn ?: ""),
                cursor.getString(cursor.getColumnIndex("bucket_display_name")),
                cursor.getLong(cursor.getColumnIndex(AlbumLoader.COLUMN_COUNT))
            )
        }
    }

    fun getId(): String = mId

    fun getCoverUri(): Uri = mCoverUri

    fun getCount(): Long = mCount

    fun addCaptureCount() {
        mCount++
    }

    fun getDisplayName(context: Context): String = if (isAll()) {
        context.getString(R.string.album_name_all)
    } else mDisplayName

    fun isAll(): Boolean = ALBUM_ID_ALL == mId

    fun isEmpty(): Boolean = mCount == 0L


}