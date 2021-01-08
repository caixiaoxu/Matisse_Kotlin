package com.frame.matisse.internal.ui.adapter

import android.content.Context
import android.content.res.TypedArray
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.TextView
import com.lsy.matisse.R
import com.frame.matisse.internal.entity.Album
import com.frame.matisse.internal.entity.SelectionSpec
import java.lang.String

/**
 * Title : 相册专辑Adapter
 * Author: Lsy
 * Date: 2020/12/23 11:45 AM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class AlbumsAdapter : CursorAdapter {

    constructor(context: Context, c: Cursor?, autoRequery: Boolean) : super(
        context, c, autoRequery
    ) {
        initAttr(context)
    }

    constructor(context: Context, c: Cursor?, flags: Int) : super(context, c, flags) {
        initAttr(context)
    }

    private var mPlaceholder: Drawable? = null

    private fun initAttr(context: Context) {
        val ta: TypedArray =
            context.theme.obtainStyledAttributes(intArrayOf(R.attr.album_thumbnail_placeholder))
        mPlaceholder = ta.getDrawable(0)
        ta.recycle()
    }

    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View =
        LayoutInflater.from(context).inflate(R.layout.album_list_item, parent, false)

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val album: Album = Album.valueOf(cursor)
        (view.findViewById<View>(R.id.album_name) as TextView).text = album.getDisplayName(context)
        (view.findViewById<View>(R.id.album_media_count) as TextView).text =
            String.valueOf(album.getCount())

        // do not need to load animated Gif
        SelectionSpec.imageEngine?.loadThumbnail(
            context, context.resources.getDimensionPixelSize(R.dimen.media_grid_size), mPlaceholder,
            view.findViewById<View>(R.id.album_cover) as ImageView, album.getCoverUri()
        )
    }
}