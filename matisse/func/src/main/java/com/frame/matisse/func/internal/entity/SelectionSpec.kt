package com.frame.matisse.func.internal.entity

import android.content.pm.ActivityInfo
import androidx.annotation.StyleRes
import com.frame.matisse.func.MimeType
import com.frame.matisse.func.MimeTypeManager
import com.frame.matisse.func.engine.ImageEngine
import com.frame.matisse.func.engine.impl.GlideEngine
import com.frame.matisse.func.filter.Filter
import com.frame.matisse.listener.OnCheckedListener
import com.frame.matisse.listener.OnSelectedListener

/**
 * Title : 选择规范
 * Author: Lsy
 * Date: 2020/12/22 3:11 PM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
object SelectionSpec {
    var mimeTypeSet: Set<MimeType> = setOf() //媒体扩展集合
    var mediaTypeExclusive = false
    var showSingleMediaType = false//只能显示显示视频或者图片媒体类型

    @StyleRes
    var themeId = 0//样式
    var orientation = 0//方向
    var countable = false//是否支持多选
    var maxSelectable = 0//最大可选数
    var maxImageSelectable = 0//最大可选图片数
    var maxVideoSelectable = 0//最大可选视频数
    var filters: MutableList<Filter> = mutableListOf() //过滤条件
    var capture = false//是否需要拍照
    var captureStrategy: CaptureStrategy? = null//拍照策略
    var spanCount = 0//行数
    var gridExpectedSize = 0
    var thumbnailScale = 0f//缩略图比例
    var imageEngine: ImageEngine? = null//图片加载引擎
    var hasInited = false
    var onSelectedListener: OnSelectedListener? = null//选择监听
    var originalable = false//是否显示原图
    var autoHideToobar = false//自动隐藏工具栏
    var originalMaxSize = 0//最大原始大小
    var onCheckedListener: OnCheckedListener? = null//选中监听
    var showPreview = false//显示预览

    fun getCleanInstance(): SelectionSpec {
        reset()
        return this
    }

    private fun reset() {
        mimeTypeSet = setOf()
        mediaTypeExclusive = true
        showSingleMediaType = false
        themeId = 0
        orientation = 0
        countable = false
        maxSelectable = 1
        maxImageSelectable = 0
        maxVideoSelectable = 0
        filters = mutableListOf()
        capture = false
        captureStrategy = null
        spanCount = 3
        gridExpectedSize = 0
        thumbnailScale = 0.5f
        imageEngine = GlideEngine()
        hasInited = true
        originalable = false
        autoHideToobar = false
        originalMaxSize = Int.MAX_VALUE
        showPreview = true
    }

    /**
     * 单选
     */
    fun singleSelectionModeEnabled(): Boolean =
        !countable && (maxSelectable == 1 || maxImageSelectable == 1 && maxVideoSelectable == 1)

    /**
     * 需要取向限制
     */
    fun needOrientationRestriction(): Boolean =
        orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    /**
     * 仅显示图片
     */
    fun onlyShowImages(): Boolean =
        showSingleMediaType && MimeTypeManager.ofImage().containsAll(mimeTypeSet)

    /**
     * 仅显示视频
     */
    fun onlyShowVideos(): Boolean =
        showSingleMediaType && MimeTypeManager.ofVideo().containsAll(mimeTypeSet)

    /**
     * 仅显示gif
     */
    fun onlyShowGif(): Boolean = showSingleMediaType && MimeTypeManager.ofGif() == mimeTypeSet
}