package com.frame.matisse.internal.ui.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.frame.matisse.R
import com.frame.matisse.func.internal.entity.Item
import com.frame.matisse.func.internal.entity.SelectionSpec

/**
 * Title : 媒体列表Item控件
 * Author: Lsy
 * Date: 2020/12/23 3:52 PM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class MediaGrid @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SquareFrameLayout(context, attrs, defStyleAttr), View.OnClickListener {
    private var mThumbnail: ImageView
    private var mCheckView: CheckView
    private var mGifTag: ImageView
    private var mVideoDuration: TextView
    private var mMedia: Item? = null
    private var mPreBindInfo: PreBindInfo? = null
    private var mListener: OnMediaGridClickListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.media_grid_content, this, true)
        mThumbnail = findViewById(R.id.media_thumbnail)
        mCheckView = findViewById(R.id.check_view)
        mGifTag = findViewById(R.id.gif)
        mVideoDuration = findViewById(R.id.video_duration)
        mThumbnail.setOnClickListener(this)
        mCheckView.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (mListener != null && null != mMedia && null != mPreBindInfo) {
            if (v === mThumbnail) {
                mListener!!.onThumbnailClicked(mThumbnail, mMedia!!, mPreBindInfo!!.mViewHolder)
            } else if (v === mCheckView) {
                mListener!!.onCheckViewClicked(mCheckView, mMedia!!, mPreBindInfo!!.mViewHolder)
            }
        }
    }

    /**
     * 预显示数据
     */
    fun preBindMedia(info: PreBindInfo?) {
        mPreBindInfo = info
    }

    /**
     * 绑定媒体数据
     */
    fun bindMedia(item: Item?) {
        mMedia = item
        setGifTag()
        initCheckView()
        setImage()
        setVideoDuration()
    }

    fun getMedia(): Item? {
        return mMedia
    }

    /**
     * 显示/隐藏Gif标签
     */
    private fun setGifTag() {
        mGifTag.visibility = if (mMedia?.isGif() == true) View.VISIBLE else View.GONE
    }

    /**
     * 是否可多选
     */
    private fun initCheckView() {
        mCheckView.setCountable(mPreBindInfo?.mCheckViewCountable == true)
    }

    /**
     * 是否可选中
     */
    fun setCheckEnabled(enabled: Boolean) {
        mCheckView.isEnabled = enabled
    }

    /**
     * 选择编号
     */
    fun setCheckedNum(checkedNum: Int) {
        mCheckView.setCheckedNum(checkedNum)
    }

    /**
     * 选中
     */
    fun setChecked(checked: Boolean) {
        mCheckView.setChecked(checked)
    }

    /**
     * 加载图片
     */
    private fun setImage() {
        mMedia?.let {
            mThumbnail.tag = it.uri
            if (it.isGif()) {
                SelectionSpec.imageEngine?.loadGifThumbnail(
                    context, mPreBindInfo!!.mResize,
                    mPreBindInfo!!.mPlaceholder, mThumbnail, it.getContentUri()
                )
            } else {
                SelectionSpec.imageEngine?.loadThumbnail(
                    context, mPreBindInfo!!.mResize,
                    mPreBindInfo!!.mPlaceholder, mThumbnail, it.getContentUri()
                )
            }
        }
    }

    /**
     * 清空加载，减少内存
     */
    public fun clear() {
        mThumbnail.setImageDrawable(null)
        SelectionSpec.imageEngine?.clear(context, mThumbnail)
    }

    /**
     * 显示视频时长
     */
    private fun setVideoDuration() {
        mMedia?.let {
            if (it.isVideo()) {
                mVideoDuration.visibility = VISIBLE
                mVideoDuration.text = DateUtils.formatElapsedTime(it.duration / 1000)
            } else {
                mVideoDuration.visibility = GONE
            }
        }
    }

    fun setOnMediaGridClickListener(listener: OnMediaGridClickListener?) {
        mListener = listener
    }

    fun removeOnMediaGridClickListener() {
        mListener = null
    }

    interface OnMediaGridClickListener {
        fun onThumbnailClicked(thumbnail: ImageView, item: Item, holder: RecyclerView.ViewHolder)
        fun onCheckViewClicked(checkView: CheckView, item: Item, holder: RecyclerView.ViewHolder)
    }

    class PreBindInfo(
        var mResize: Int, var mPlaceholder: Drawable?, var mCheckViewCountable: Boolean,
        var mViewHolder: RecyclerView.ViewHolder
    )
}