package com.frame.matisse.internal.ui.adapter

import android.database.Cursor
import android.provider.MediaStore
import androidx.recyclerview.widget.RecyclerView

/**
 * Title :
 * Author: Lsy
 * Date: 2020/12/23 3:33 PM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
abstract class RecyclerViewCursorAdapter<VH : RecyclerView.ViewHolder>(private var mCursor: Cursor?) :
    RecyclerView.Adapter<VH>() {
    private var mRowIDColumn = 0

    init {
        setHasStableIds(true)
        swapCursor(mCursor)
    }

    /**
     * 切换光标
     */
    open fun swapCursor(newCursor: Cursor?) {
        if (newCursor === mCursor) {
            return
        }
        if (newCursor != null) {
            mCursor = newCursor
            mRowIDColumn = mCursor!!.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            // notify the observers about the new cursor
            notifyDataSetChanged()
        } else {
            notifyItemRangeRemoved(0, itemCount)
            mCursor = null
            mRowIDColumn = -1
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        check(isDataValid(mCursor)) { "Cannot bind view holder when cursor is in invalid state." }

        //移动光标
        check(null != mCursor && mCursor!!.moveToPosition(position)) {
            "Could not move cursor to position $position when trying to bind view holder"
        }
        onBindViewHolder(holder, mCursor)
    }

    /**
     * 判断数据是否有效
     */
    private fun isDataValid(cursor: Cursor?): Boolean = (cursor != null && !cursor.isClosed)

    protected abstract fun onBindViewHolder(holder: VH, cursor: Cursor?)

    override fun getItemViewType(position: Int): Int {
        check(null != mCursor && mCursor!!.moveToPosition(position)) {
            "Could not move cursor to position $position when trying to get item view type."
        }
        return getItemViewType(position, mCursor)
    }

    protected abstract fun getItemViewType(position: Int, cursor: Cursor?): Int

    override fun getItemCount(): Int = if (isDataValid(mCursor)) mCursor!!.count else 0

    override fun getItemId(position: Int): Long {
        check(isDataValid(mCursor)) {
            "Cannot lookup item id when cursor is in invalid state."
        }
        check(null != mCursor && mCursor!!.moveToPosition(position)) {
            "Could not move cursor to position $position when trying to get an item id"
        }
        return mCursor!!.getLong(mRowIDColumn)
    }

    open fun getCursor(): Cursor? = mCursor
}