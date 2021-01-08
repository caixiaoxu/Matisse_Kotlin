package com.frame.matisse.internal.model

import android.content.Context
import android.content.res.Resources.NotFoundException
import android.net.Uri
import android.os.Bundle
import com.lsy.matisse.R
import com.frame.matisse.internal.entity.IncapableCause
import com.frame.matisse.internal.entity.Item
import com.frame.matisse.internal.entity.SelectionSpec
import com.frame.matisse.internal.utils.PathUtils
import com.frame.matisse.internal.utils.PhotoMetadataUtils
import com.frame.matisse.internal.ui.widget.CheckView
import java.util.*

/**
 * Title : 选择的相册媒体集合
 * Author: Lsy
 * Date: 2020/12/22 6:27 PM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class SelectedItemCollection(private val mContext: Context) {
    companion object {
        const val STATE_SELECTION = "state_selection"
        const val STATE_COLLECTION_TYPE = "state_collection_type"

        /**
         * Empty collection
         */
        const val COLLECTION_UNDEFINED = 0x00

        /**
         * Collection only with images
         */
        const val COLLECTION_IMAGE = 0x01

        /**
         * Collection only with videos
         */
        const val COLLECTION_VIDEO = 0x01 shl 1

        /**
         * Collection with images and videos.
         */
        const val COLLECTION_MIXED = COLLECTION_IMAGE or COLLECTION_VIDEO
    }

    //选择的集合
    private var mItems: MutableSet<Item> = mutableSetOf()
    //选择的类型
    private var mCollectionType = COLLECTION_UNDEFINED

    /**********跟随生命周期走的恢复数据:Start**********/
    fun onCreate(bundle: Bundle?) {
        bundle?.let {
            val saved: List<Item> = bundle.getParcelableArrayList(STATE_SELECTION) ?: listOf()
            mItems = LinkedHashSet(saved)
            mCollectionType = bundle.getInt(STATE_COLLECTION_TYPE, COLLECTION_UNDEFINED)
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(STATE_SELECTION, ArrayList(mItems))
        outState.putInt(STATE_COLLECTION_TYPE, mCollectionType)
    }
    /**********跟随生命周期走的恢复数据:End**********/

    /**
     * 打包选择列表和类型成Bundle
     */
    fun getDataWithBundle(): Bundle {
        val bundle = Bundle()
        bundle.putParcelableArrayList(STATE_SELECTION, ArrayList(mItems))
        bundle.putInt(STATE_COLLECTION_TYPE, mCollectionType)
        return bundle
    }

    /**
     * 设置默认选择中的列表
     */
    fun setDefaultSelection(uris: List<Item>) {
        mItems.addAll(uris)
    }

    /**
     * 添加到选择列表
     */
    fun add(item: Item): Boolean {
        require(!typeConflict(item)) { "Can't select images and videos at the same time." }
        val added = mItems.add(item)
        if (added) {
            if (mCollectionType == COLLECTION_UNDEFINED) {
                if (item.isImage()) {
                    mCollectionType = COLLECTION_IMAGE
                } else if (item.isVideo()) {
                    mCollectionType = COLLECTION_VIDEO
                }
            } else if (mCollectionType == COLLECTION_IMAGE) {
                if (item.isVideo()) {
                    mCollectionType = COLLECTION_MIXED
                }
            } else if (mCollectionType == COLLECTION_VIDEO) {
                if (item.isImage()) {
                    mCollectionType = COLLECTION_MIXED
                }
            }
        }
        return added
    }

    /**
     * 从选择列表中移除
     */
    fun remove(item: Item): Boolean {
        val removed = mItems.remove(item)
        if (removed) {
            if (mItems.size == 0) {
                mCollectionType = COLLECTION_UNDEFINED
            } else {
                if (mCollectionType == COLLECTION_MIXED) {
                    refineCollectionType()
                }
            }
        }
        return removed
    }

    /**
     * 覆盖选择列表和类型
     */
    fun overwrite(items: ArrayList<Item>, collectionType: Int) {
        mCollectionType = if (items.size == 0) {
            COLLECTION_UNDEFINED
        } else {
            collectionType
        }
        mItems.clear()
        mItems.addAll(items)
    }

    /**
     * 获取选择列表
     */
    fun asList(): List<Item> {
        return ArrayList(mItems)
    }

    /**
     * 获取选择Uri列表
     */
    fun asListOfUri(): List<Uri> {
        val uris = ArrayList<Uri>()
        for (item in mItems) {
            uris.add(item.getContentUri())
        }
        return uris
    }

    /**
     * 获取选择路径列表
     */
    fun asListOfString(): List<String?> {
        val paths = ArrayList<String?>()
        for (item in mItems) {
            paths.add(PathUtils.getPathFromUri(mContext, item.getContentUri()))
        }
        return paths
    }

    /**
     * 判空
     */
    fun isEmpty(): Boolean {
        return mItems.isEmpty()
    }

    /**
     * 是否选择
     */
    fun isSelected(item: Item?): Boolean {
        return mItems.contains(item)
    }

    /**
     * 是否可选(1、是否超过最大可选数量，2、是否满足选择条件)
     */
    fun isAcceptable(item: Item): IncapableCause? {
        if (maxSelectableReached()) {
            val maxSelectable = currentMaxSelectable()
            val cause: String
            cause = try {
                mContext.getString(R.string.error_over_count, maxSelectable)
            } catch (e: NotFoundException) {
                mContext.getString(R.string.error_over_count, maxSelectable)
            } catch (e: NoClassDefFoundError) {
                mContext.getString(R.string.error_over_count, maxSelectable)
            }
            return IncapableCause(message = cause)
        } else if (typeConflict(item)) {
            return IncapableCause(message = mContext.getString(R.string.error_type_conflict))
        }
        return PhotoMetadataUtils.isAcceptable(mContext, item)
    }

    /**
     * 当前选择列表是否已经到最大值
     */
    fun maxSelectableReached(): Boolean = mItems.size == currentMaxSelectable()

    /**
     * 最大可选择数量
     */
    private fun currentMaxSelectable(): Int {
        return when {
            SelectionSpec.maxSelectable > 0 -> SelectionSpec.maxSelectable
            mCollectionType == COLLECTION_IMAGE -> SelectionSpec.maxImageSelectable
            mCollectionType == COLLECTION_VIDEO -> SelectionSpec.maxVideoSelectable
            else -> SelectionSpec.maxSelectable
        }
    }

    /**
     * 获取选择类型
     */
    fun getCollectionType(): Int {
        return mCollectionType
    }

    /**
     * 切换选择类型
     */
    private fun refineCollectionType() {
        var hasImage = false
        var hasVideo = false
        for (i in mItems) {
            if (i.isImage() && !hasImage) hasImage = true
            if (i.isVideo() && !hasVideo) hasVideo = true
        }
        if (hasImage && hasVideo) {
            mCollectionType = COLLECTION_MIXED
        } else if (hasImage) {
            mCollectionType = COLLECTION_IMAGE
        } else if (hasVideo) {
            mCollectionType = COLLECTION_VIDEO
        }
    }

    /**
     * 确定是否存在冲突媒体类型。用户只能同时选择图片和视频
     * Determine whether there will be conflict media types. A user can only select images and videos at the same time
     * while [SelectionSpec.mediaTypeExclusive] is set to false.
     */
    private fun typeConflict(item: Item): Boolean {
        return (SelectionSpec.mediaTypeExclusive
                && (item.isImage() && (mCollectionType == COLLECTION_VIDEO || mCollectionType == COLLECTION_MIXED)
                || item.isVideo() && (mCollectionType == COLLECTION_IMAGE || mCollectionType == COLLECTION_MIXED)))
    }

    /**
     * 选择列表数量
     */
    fun count(): Int {
        return mItems.size
    }

    /**
     * 在选择列表中的位置
     */
    fun checkedNumOf(item: Item?): Int {
        val index = ArrayList(mItems).indexOf(item)
        return if (index == -1) CheckView.UNCHECKED else index + 1
    }
}