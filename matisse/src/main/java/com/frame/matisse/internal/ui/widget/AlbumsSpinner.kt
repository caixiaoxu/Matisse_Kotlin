package com.frame.matisse.internal.ui.widget

import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.view.View
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.CursorAdapter
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow
import com.lsy.matisse.R
import com.frame.matisse.internal.entity.Album
import com.frame.matisse.internal.utils.Platform

/**
 * Title : 相册专辑弹窗
 * Author: Lsy
 * Date: 2020/12/23 11:24 AM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class AlbumsSpinner(context: Context) {
    companion object {
        private const val MAX_SHOWN_COUNT = 6
    }

    //适配器
    private var mAdapter: CursorAdapter? = null

    //触发的控件
    private var mSelected: TextView? = null

    //点击事件
    private var mOnItemSelectedListener: OnItemSelectedListener? = null
    private val mListPopupWindow: ListPopupWindow = ListPopupWindow(
        context, null, R.attr.listPopupWindowStyle
    ).apply {
        isModal = true
        val density = context.resources.displayMetrics.density
        setContentWidth((216 * density).toInt())
        horizontalOffset = (16 * density).toInt()
        verticalOffset = (-12 * density).toInt()

        setOnItemClickListener { parent, view, position, id ->
            this@AlbumsSpinner.onItemSelected(parent.context, position)
            mOnItemSelectedListener?.onItemSelected(parent, view, position, id)
        }
    }

    /**
     * 点击事件
     */
    fun setOnItemSelectedListener(listener: OnItemSelectedListener) {
        mOnItemSelectedListener = listener
    }

    /**
     * 选择的相册
     */
    fun setSelection(context: Context, position: Int) {
        mListPopupWindow.setSelection(position)
        onItemSelected(context, position)
    }

    /**
     * 点击Item
     */
    private fun onItemSelected(context: Context, position: Int) {
        mListPopupWindow.dismiss()
        mAdapter?.let {
            //移动光标
            val cursor = it.cursor
            cursor.moveToPosition(position)
            //显示名
            mSelected?.let { tv ->
                //获取光标所在的值
                val album: Album = Album.valueOf(cursor)
                val displayName: String = album.getDisplayName(context)
                if (tv.visibility == View.VISIBLE) {
                    tv.text = displayName
                } else {
                    tv.alpha = 0.0f
                    tv.visibility = View.VISIBLE
                    tv.text = displayName
                    tv.animate().alpha(1.0f).setDuration(
                        context.resources.getInteger(android.R.integer.config_longAnimTime).toLong()
                    ).start()
                }
            }
        }
    }

    /**
     * 设置适配器
     */
    fun setAdapter(adapter: CursorAdapter) {
        mListPopupWindow.setAdapter(adapter)
        mAdapter = adapter
    }

    /**
     * 设置触发控件
     */
    fun setSelectedTextView(textView: TextView) {
        mSelected = textView
        mSelected?.let {
            // tint dropdown arrow icon
            val drawables = it.compoundDrawablesRelative
            val right = drawables[2]
            val ta = it.context.theme.obtainStyledAttributes(intArrayOf(R.attr.album_element_color))
            val color = ta.getColor(0, 0)
            ta.recycle()
            if (Platform.beforeAndroidTen()) right.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            else right.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_IN)
            it.visibility = View.GONE
            it.setOnClickListener { v ->
                mAdapter?.let { adapter ->
                    val itemHeight = v.resources.getDimensionPixelSize(R.dimen.album_item_height)
                    mListPopupWindow.height =
                        if (adapter.count > MAX_SHOWN_COUNT) itemHeight * MAX_SHOWN_COUNT
                        else itemHeight * adapter.count
                    mListPopupWindow.show()
                }
            }
            it.setOnTouchListener(mListPopupWindow.createDragToOpenListener(mSelected))
        }
    }

    /**
     * 锚点View
     */
    fun setPopupAnchorView(view: View?) {
        mListPopupWindow.anchorView = view
    }
}