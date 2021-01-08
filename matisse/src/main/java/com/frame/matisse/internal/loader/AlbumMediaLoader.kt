package com.frame.matisse.internal.loader

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.database.MergeCursor
import android.provider.MediaStore
import androidx.loader.content.CursorLoader
import com.frame.matisse.internal.entity.Album
import com.frame.matisse.internal.entity.Item
import com.frame.matisse.internal.entity.SelectionSpec
import com.frame.matisse.internal.utils.MediaStoreCompat

/**
 * Title : 相册专辑内容加载器
 * Author: Lsy
 * Date: 2020/12/23 3:13 PM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class AlbumMediaLoader(
    context: Context, selection: String, selectionArgs: Array<String>, private val capture: Boolean
) : CursorLoader(context, QUERY_URI, PROJECTION, selection, selectionArgs, ORDER_BY) {

    companion object {
        private val QUERY_URI = MediaStore.Files.getContentUri("external")
        private val PROJECTION = arrayOf(
            MediaStore.Files.FileColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE, MediaStore.MediaColumns.SIZE, "duration"
        )

        // === params for album ALL && showSingleMediaType: false ===
        private const val SELECTION_ALL = ("(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0")
        private val SELECTION_ALL_ARGS = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
        )

        // ===========================================================
        // === params for album ALL && showSingleMediaType: true ===
        private const val SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE =
            (MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" + " AND " + MediaStore.MediaColumns.SIZE + ">0")

        private fun getSelectionArgsForSingleMediaType(mediaType: Int): Array<String> {
            return arrayOf(mediaType.toString())
        }

        // =========================================================
        // === params for ordinary album && showSingleMediaType: false ===
        private const val SELECTION_ALBUM = ("(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)"
                + " AND "
                + " bucket_id=?"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0")

        private fun getSelectionAlbumArgs(albumId: String): Array<String> {
            return arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
                albumId
            )
        }

        // ===============================================================
        // === params for ordinary album && showSingleMediaType: true ===
        private const val SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE =
            (MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND "
                    + " bucket_id=?"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0")

        private fun getSelectionAlbumArgsForSingleMediaType(
            mediaType: Int, albumId: String
        ): Array<String> {
            return arrayOf(mediaType.toString(), albumId)
        }

        // ===============================================================
        // === params for album ALL && showSingleMediaType: true && MineType=="image/gif"
        private const val SELECTION_ALL_FOR_GIF = (MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + " AND "
                + MediaStore.MediaColumns.MIME_TYPE + "=?"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0")

        private fun getSelectionArgsForGifType(mediaType: Int): Array<String> {
            return arrayOf(mediaType.toString(), "image/gif")
        }

        // ===============================================================
        // === params for ordinary album && showSingleMediaType: true  && MineType=="image/gif" ===
        private const val SELECTION_ALBUM_FOR_GIF = (MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + " AND "
                + " bucket_id=?"
                + " AND "
                + MediaStore.MediaColumns.MIME_TYPE + "=?"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0")

        private fun getSelectionAlbumArgsForGifType(
            mediaType: Int, albumId: String
        ): Array<String> {
            return arrayOf(mediaType.toString(), albumId, "image/gif")
        }

        // ===============================================================
        private const val ORDER_BY = MediaStore.Images.Media.DATE_TAKEN + " DESC"

        /**
         * 实例化
         */
        fun newInstance(context: Context, album: Album, capture: Boolean): CursorLoader {
            val selection: String
            val selectionArgs: Array<String>
            val enableCapture: Boolean
            if (album.isAll()) {
                when {
                    SelectionSpec.onlyShowGif() -> {
                        selection = SELECTION_ALL_FOR_GIF
                        selectionArgs = getSelectionArgsForGifType(
                            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                        )
                    }
                    SelectionSpec.onlyShowImages() -> {
                        selection = SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE
                        selectionArgs = getSelectionArgsForSingleMediaType(
                            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                        )
                    }
                    SelectionSpec.onlyShowVideos() -> {
                        selection = SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE
                        selectionArgs = getSelectionArgsForSingleMediaType(
                            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                        )
                    }
                    else -> {
                        selection = SELECTION_ALL
                        selectionArgs = SELECTION_ALL_ARGS
                    }
                }
                enableCapture = capture
            } else {
                when {
                    SelectionSpec.onlyShowGif() -> {
                        selection = SELECTION_ALBUM_FOR_GIF
                        selectionArgs = getSelectionAlbumArgsForGifType(
                            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE, album.getId()
                        )
                    }
                    SelectionSpec.onlyShowImages() -> {
                        selection = SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE
                        selectionArgs = getSelectionAlbumArgsForSingleMediaType(
                            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE,
                            album.getId()
                        )
                    }
                    SelectionSpec.onlyShowVideos() -> {
                        selection = SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE
                        selectionArgs = getSelectionAlbumArgsForSingleMediaType(
                            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
                            album.getId()
                        )
                    }
                    else -> {
                        selection = SELECTION_ALBUM
                        selectionArgs = getSelectionAlbumArgs(album.getId())
                    }
                }
                enableCapture = false
            }
            return AlbumMediaLoader(context, selection, selectionArgs, enableCapture)
        }
    }

    override fun loadInBackground(): Cursor? {
        val result = super.loadInBackground()
        if (!capture || !MediaStoreCompat.hasCameraFeature(context)) {
            return result
        }
        val dummy = MatrixCursor(PROJECTION)
        dummy.addRow(arrayOf<Any>(Item.ITEM_ID_CAPTURE, Item.ITEM_DISPLAY_NAME_CAPTURE, "", 0, 0))
        return MergeCursor(arrayOf(dummy, result))
    }
}