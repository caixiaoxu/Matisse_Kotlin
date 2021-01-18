package com.frame.matisse.internal.ui.adapter

import android.content.Context
import android.database.Cursor
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.frame.matisse.R
import com.frame.matisse.func.internal.entity.Album
import com.frame.matisse.func.internal.entity.IncapableCause
import com.frame.matisse.func.internal.entity.Item
import com.frame.matisse.func.internal.entity.SelectionSpec
import com.frame.matisse.func.internal.model.SelectedItemCollection
import com.frame.matisse.internal.ui.widget.CheckView
import com.frame.matisse.internal.ui.widget.MediaGrid

/**
 * Title : 媒体Adapter
 * Author: Lsy
 * Date: 2020/12/23 3:32 PM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class AlbumMediaAdapter(
    context: Context,
    private val selectedCollection: SelectedItemCollection,
    private val recyclerView: RecyclerView
) : RecyclerViewCursorAdapter<RecyclerView.ViewHolder>(null), MediaGrid.OnMediaGridClickListener {

    companion object {
        private const val VIEW_TYPE_CAPTURE = 0x01
        private const val VIEW_TYPE_MEDIA = 0x02
    }

    //默认图
    private var mPlaceholder: Drawable? = null

    //选中事件
    private var mCheckStateListener: CheckStateListener? = null

    //点击事件
    private var mOnMediaClickListener: OnMediaClickListener? = null
    private var mImageResize = 0

    init {
        val ta = context.theme.obtainStyledAttributes(intArrayOf(R.attr.item_placeholder))
        mPlaceholder = ta.getDrawable(0)
        ta.recycle()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CAPTURE -> {
                //拍照按钮
                val v = LayoutInflater.from(parent.context).inflate(
                    R.layout.photo_capture_item, parent, false
                )
                CaptureViewHolder(v).apply {
                    itemView.setOnClickListener {
                        if (it.context is OnPhotoCapture) (it.context as OnPhotoCapture).capture()
                    }
                }
            }
            else -> {
                //媒体界面
                val v = LayoutInflater.from(parent.context).inflate(
                    R.layout.media_grid_item, parent, false
                )
                MediaViewHolder(v)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, cursor: Cursor?) {
        when (holder) {
            is MediaViewHolder -> {
                //获取光标所在的数据
                val item = Item.valueOf(cursor!!)
                //绑定预显示数据
                holder.mMediaGrid.preBindMedia(
                    MediaGrid.PreBindInfo(
                        getImageResize(holder.mMediaGrid.context), mPlaceholder,
                        SelectionSpec.countable, holder
                    )
                )
                //绑定媒体数据
                holder.mMediaGrid.bindMedia(item)
                //设置点击和选中事件
                holder.mMediaGrid.setOnMediaGridClickListener(this)
                //设置选中状态
                setCheckStatus(item, holder.mMediaGrid)
            }
            is CaptureViewHolder -> {
                //改变按钮颜色
                val drawables = holder.mHint.compoundDrawables
                val ta =
                    holder.itemView.context.theme.obtainStyledAttributes(intArrayOf(R.attr.capture_textColor))
                val color = ta.getColor(0, 0)
                ta.recycle()
                for (i in drawables.indices) {
                    val drawable = drawables[i]
                    if (drawable != null) {
                        val state = drawable.constantState ?: continue
                        val newDrawable = state.newDrawable().mutate()
                        newDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                        newDrawable.bounds = drawable.bounds
                        drawables[i] = newDrawable
                    }
                }
                holder.mHint.setCompoundDrawables(
                    drawables[0],
                    drawables[1], drawables[2], drawables[3]
                )
            }
        }
    }

    /**
     * 获取图片宽高
     */
    private fun getImageResize(context: Context): Int {
        if (mImageResize == 0) {
            val spanCount = (recyclerView.layoutManager as? GridLayoutManager)?.spanCount ?: 3
            val screenWidth = context.resources.displayMetrics.widthPixels
            val availableWidth =
                screenWidth - context.resources.getDimensionPixelSize(R.dimen.media_grid_spacing) * (spanCount - 1)
            mImageResize = availableWidth / spanCount
            mImageResize = (mImageResize * SelectionSpec.thumbnailScale).toInt()
        }
        return mImageResize
    }

    private fun setCheckStatus(item: Item, mediaGrid: MediaGrid) {
        if (SelectionSpec.countable) {
            //可多选
            //当前选中的位置
            val checkedNum = selectedCollection.checkedNumOf(item)
            if (checkedNum > 0) {
                mediaGrid.setCheckEnabled(true)
                mediaGrid.setCheckedNum(checkedNum)
            } else {
                //是否达到最大可选择数量
                if (selectedCollection.maxSelectableReached()) {
                    mediaGrid.setCheckEnabled(false)
                    mediaGrid.setCheckedNum(CheckView.UNCHECKED)
                } else {
                    mediaGrid.setCheckEnabled(true)
                    mediaGrid.setCheckedNum(checkedNum)
                }
            }
        } else {
            //单选
            val selected = selectedCollection.isSelected(item)
            if (selected) {
                mediaGrid.setCheckEnabled(true)
                mediaGrid.setChecked(true)
            } else {
                //是否达到最大可选择数量
                if (selectedCollection.maxSelectableReached()) {
                    mediaGrid.setCheckEnabled(false)
                    mediaGrid.setChecked(false)
                } else {
                    mediaGrid.setCheckEnabled(true)
                    mediaGrid.setChecked(false)
                }
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is MediaViewHolder){
            holder.mMediaGrid.clear()
        }
        super.onViewRecycled(holder)
    }

    override fun getItemViewType(position: Int, cursor: Cursor?): Int {
        return cursor?.let {
            if (Item.valueOf(cursor).isCapture()) VIEW_TYPE_CAPTURE else VIEW_TYPE_MEDIA
        } ?: VIEW_TYPE_CAPTURE
    }

    override fun onThumbnailClicked(
        thumbnail: ImageView, item: Item, holder: RecyclerView.ViewHolder
    ) {
        if (SelectionSpec.showPreview) {
            //显示预览
            mOnMediaClickListener?.onMediaClick(null, item, holder.adapterPosition)
        } else {
            updateSelectedItem(item, holder)
        }
    }

    /**
     * 更新选择列表
     */
    private fun updateSelectedItem(item: Item, holder: RecyclerView.ViewHolder) {
        if (SelectionSpec.countable) {
            //多选
            //所在列表的位置
            val checkedNum = selectedCollection.checkedNumOf(item)
            //没有就添加，有就移除
            if (checkedNum == CheckView.UNCHECKED) {
                if (assertAddSelection(holder.itemView.context, item)) {
                    selectedCollection.add(item)
                    notifyCheckStateChanged()
                }
            } else {
                selectedCollection.remove(item)
                notifyCheckStateChanged()
            }
        } else {
            //单选
            //没有就添加，有就移除
            if (selectedCollection.isSelected(item)) {
                selectedCollection.remove(item)
                notifyCheckStateChanged()
            } else {
                if (assertAddSelection(holder.itemView.context, item)) {
                    selectedCollection.add(item)
                    notifyCheckStateChanged()
                }
            }
        }
    }

    /**
     * 是否满足添加条件
     */
    private fun assertAddSelection(context: Context, item: Item): Boolean {
        val cause: IncapableCause? = selectedCollection.isAcceptable(item)
        IncapableCause.handleCause(context, cause)
        return cause == null
    }

    private fun notifyCheckStateChanged() {
        notifyDataSetChanged()
        mCheckStateListener?.onUpdate()
    }

    override fun onCheckViewClicked(
        checkView: CheckView, item: Item, holder: RecyclerView.ViewHolder
    ) {
        updateSelectedItem(item, holder)
    }

    fun registerCheckStateListener(listener: CheckStateListener) {
        mCheckStateListener = listener
    }

    fun unregisterCheckStateListener() {
        mCheckStateListener = null
    }

    fun registerOnMediaClickListener(listener: OnMediaClickListener) {
        mOnMediaClickListener = listener
    }

    fun unregisterOnMediaClickListener() {
        mOnMediaClickListener = null
    }

    fun refreshSelection() {
        val layoutManager = recyclerView.layoutManager as GridLayoutManager
        val first = layoutManager.findFirstVisibleItemPosition()
        val last = layoutManager.findLastVisibleItemPosition()
        if (first == -1 || last == -1) {
            return
        }
        val cursor = getCursor()
        cursor?.let {
            for (i in first..last) {
                val holder = recyclerView.findViewHolderForAdapterPosition(first)
                if (holder is MediaViewHolder) {
                    if (cursor.moveToPosition(i)) {
                        setCheckStatus(Item.valueOf(cursor), holder.mMediaGrid)
                    }
                }
            }
        }
    }

    /**
     * 媒体按钮布局
     */
    private class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mMediaGrid: MediaGrid = itemView as MediaGrid
    }

    /**
     * 拍照按钮布局
     */
    private class CaptureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mHint: TextView = itemView.findViewById(R.id.hint)
    }

    interface CheckStateListener {
        fun onUpdate()
    }

    interface OnMediaClickListener {
        fun onMediaClick(album: Album?, item: Item?, adapterPosition: Int)
    }

    interface OnPhotoCapture {
        fun capture()
    }
}