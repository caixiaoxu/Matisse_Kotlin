package com.frame.matisse

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import androidx.annotation.IntDef
import androidx.annotation.RequiresApi
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import com.frame.matisse.func.MimeType
import com.frame.matisse.func.engine.ImageEngine
import com.frame.matisse.func.filter.Filter
import com.frame.matisse.func.internal.entity.CaptureStrategy
import com.frame.matisse.func.internal.entity.SelectionSpec
import com.frame.matisse.listener.OnCheckedListener
import com.frame.matisse.listener.OnSelectedListener
import com.frame.matisse.ui.MatisseActivity

/**
 * Title : 选择创建者
 * Author: Lsy
 * Date: 2020/12/22 4:16 PM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */

/**
 * Fluent API for building media select specification.
 */
@SuppressWarnings("unused")
class SelectionCreator(
    private val mMatisse: Matisse, mimeTypes: Set<MimeType>, mediaTypeExclusive: Boolean
) {
    private val mSelectionSpec: SelectionSpec = SelectionSpec.getCleanInstance().apply {
        this.mimeTypeSet = mimeTypes
        this.mediaTypeExclusive = mediaTypeExclusive
        this.orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @IntDef(
        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED,
        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
        ActivityInfo.SCREEN_ORIENTATION_USER,
        ActivityInfo.SCREEN_ORIENTATION_BEHIND,
        ActivityInfo.SCREEN_ORIENTATION_SENSOR,
        ActivityInfo.SCREEN_ORIENTATION_NOSENSOR,
        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE,
        ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT,
        ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
        ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
        ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR,
        ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE,
        ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT,
        ActivityInfo.SCREEN_ORIENTATION_FULL_USER,
        ActivityInfo.SCREEN_ORIENTATION_LOCKED
    )
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    internal annotation class ScreenOrientation

    /**
     * 如果选择的媒体只是图像或视频，是否只显示一种媒体类型。
     * Whether to show only one media type if choosing medias are only images or videos.
     *
     * @param showSingleMediaType whether to show only one media type, either images or videos.
     * @return [SelectionCreator] for fluent API.
     * @see SelectionSpec.onlyShowImages
     * @see SelectionSpec.onlyShowVideos
     */
    fun showSingleMediaType(showSingleMediaType: Boolean): SelectionCreator {
        mSelectionSpec.showSingleMediaType = showSingleMediaType
        return this
    }

    /**
     * 样式
     * Theme for media selecting Activity.
     *
     *
     * There are two built-in themes:
     * 1. com.zhihu.matisse.R.style.Matisse_Zhihu;
     * 2. com.zhihu.matisse.R.style.Matisse_Dracula
     * you can define a custom theme derived from the above ones or other themes.
     *
     * @param themeId theme resource id. Default value is com.zhihu.matisse.R.style.Matisse_Zhihu.
     * @return [SelectionCreator] for fluent API.
     */
    fun theme(@StyleRes themeId: Int): SelectionCreator {
        mSelectionSpec.themeId = themeId
        return this
    }

    /**
     * 当用户选择媒体时，显示自动增加的数字或复选标记。
     * Show a auto-increased number or a check mark when user select media.
     *
     * @param countable true for a auto-increased number from 1, false for a check mark. Default
     * value is false.
     * @return [SelectionCreator] for fluent API.
     */
    fun countable(countable: Boolean): SelectionCreator {
        mSelectionSpec.countable = countable
        return this
    }

    /**
     * 最大的选择数
     * Maximum selectable count.
     *
     * @param maxSelectable Maximum selectable count. Default value is 1.
     * @return [SelectionCreator] for fluent API.
     */
    fun maxSelectable(maxSelectable: Int): SelectionCreator {
        require(maxSelectable >= 1) { "maxSelectable must be greater than or equal to one" }
        check(!(mSelectionSpec.maxImageSelectable > 0 || mSelectionSpec.maxVideoSelectable > 0)) { "already set maxImageSelectable and maxVideoSelectable" }
        mSelectionSpec.maxSelectable = maxSelectable
        return this
    }

    /**
     * 只有当[SelectionSpec.mediaTypeExclusive] 为true时，你可以设置图片和视频类型的最大数
     * Only useful when [SelectionSpec.mediaTypeExclusive] set true and you want to set different maximum
     * selectable files for image and video media types.
     *
     * @param maxImageSelectable Maximum selectable count for image.
     * @param maxVideoSelectable Maximum selectable count for video.
     * @return [SelectionCreator] for fluent API.
     */
    fun maxSelectablePerMediaType(
        maxImageSelectable: Int, maxVideoSelectable: Int
    ): SelectionCreator {
        require(!(maxImageSelectable < 1 || maxVideoSelectable < 1)) { "max selectable must be greater than or equal to one" }
        mSelectionSpec.maxSelectable = -1
        mSelectionSpec.maxImageSelectable = maxImageSelectable
        mSelectionSpec.maxVideoSelectable = maxVideoSelectable
        return this
    }

    /**
     * 添加过滤器来过滤每个选择的项目。
     * Add filter to filter each selecting item.
     *
     * @param filter [Filter]
     * @return [SelectionCreator] for fluent API.
     */
    fun addFilter(filter: Filter?): SelectionCreator {
        if (mSelectionSpec.filters.isNullOrEmpty()) {
            mSelectionSpec.filters = arrayListOf()
        }
        requireNotNull(filter) { "filter cannot be null" }
        mSelectionSpec.filters.add(filter)
        return this
    }

    /**
     * 是否显示拍照按钮。
     * Determines whether the photo capturing is enabled or not on the media grid view.
     *
     *
     * If this value is set true, photo capturing entry will appear only on All Media's page.
     *
     * @param enable Whether to enable capturing or not. Default value is false;
     * @return [SelectionCreator] for fluent API.
     */
    fun capture(enable: Boolean): SelectionCreator {
        mSelectionSpec.capture = enable
        return this
    }

    /**
     * 显示原始照片检查选项。让用户选择后决定是否使用原始照片
     * Show a original photo check options.Let users decide whether use original photo after select
     *
     * @param enable Whether to enable original photo or not
     * @return [SelectionCreator] for fluent API.
     */
    fun originalEnable(enable: Boolean): SelectionCreator {
        mSelectionSpec.originalable = enable
        return this
    }

    /**
     * 确定当用户点击图片时，是否在预览模式下隐藏顶部和底部工具栏
     * Determines Whether to hide top and bottom toolbar in PreView mode ,when user tap the picture
     * @param enable
     * @return [SelectionCreator] for fluent API.
     */
    fun autoHideToolbarOnSingleTap(enable: Boolean): SelectionCreator {
        mSelectionSpec.autoHideToobar = enable
        return this
    }

    /**
     * 最大原始大小，单位是MB。仅当{link@originalEnable}设置为true时有用
     * Maximum original size,the unit is MB. Only useful when {link@originalEnable} set true
     *
     * @param size Maximum original size. Default value is Integer.MAX_VALUE
     * @return [SelectionCreator] for fluent API.
     */
    fun maxOriginalSize(size: Int): SelectionCreator {
        mSelectionSpec.originalMaxSize = size
        return this
    }

    /**
     * 为该位置提供捕获策略，以保存包括内部和外部的照片
     * Capture strategy provided for the location to save photos including internal and external
     * storage and also a authority for [androidx.core.content.FileProvider].
     *
     * @param captureStrategy [CaptureStrategy], needed only when capturing is enabled.
     * @return [SelectionCreator] for fluent API.
     */
    fun captureStrategy(captureStrategy: CaptureStrategy?): SelectionCreator {
        mSelectionSpec.captureStrategy = captureStrategy
        return this
    }

    /**
     * 设置此activity所需的方向。
     * Set the desired orientation of this activity.
     *
     * @param orientation An orientation constant as used in [ScreenOrientation].
     * Default value is [android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT].
     * @return [SelectionCreator] for fluent API.
     * @see Activity.setRequestedOrientation
     */
    fun restrictOrientation(@ScreenOrientation orientation: Int): SelectionCreator {
        mSelectionSpec.orientation = orientation
        return this
    }

    /**
     * 设置媒体网格的固定跨度计数。对于不同的屏幕方向也是一样的。
     * Set a fixed span count for the media grid. Same for different screen orientations.
     *
     *
     * This will be ignored when [.gridExpectedSize] is set.
     *
     * @param spanCount Requested span count.
     * @return [SelectionCreator] for fluent API.
     */
    fun spanCount(spanCount: Int): SelectionCreator {
        require(spanCount >= 1) { "spanCount cannot be less than 1" }
        mSelectionSpec.spanCount = spanCount
        return this
    }

    /**
     * 设置媒体网格的预期大小，以适应不同的屏幕大小。这并不一定会被应用，因为媒体网格应该填满视图容器。测量的媒体网格的大小将尽可能接近这个值。
     * Set expected size for media grid to adapt to different screen sizes. This won't necessarily
     * be applied cause the media grid should fill the view container. The measured media grid's
     * size will be as close to this value as possible.
     *
     * @param size Expected media grid size in pixel.
     * @return [SelectionCreator] for fluent API.
     */
    fun gridExpectedSize(size: Int): SelectionCreator {
        mSelectionSpec.gridExpectedSize = size
        return this
    }

    /**
     * 图片缩略图的比例与视图的大小比较。它应该是一个浮点值(0.0，1.0)。
     * Photo thumbnail's scale compared to the View's size. It should be a float value in (0.0,
     * 1.0].
     *
     * @param scale Thumbnail's scale in (0.0, 1.0]. Default value is 0.5.
     * @return [SelectionCreator] for fluent API.
     */
    fun thumbnailScale(scale: Float): SelectionCreator {
        require(!(scale <= 0f || scale > 1f)) { "Thumbnail scale must be between (0.0, 1.0]" }
        mSelectionSpec.thumbnailScale = scale
        return this
    }

    /**
     * 提供一个图像引擎。
     * Provide an image engine.
     *
     *
     * There are two built-in image engines:
     * 1. [com.lsy.matisse.engine.impl.GlideEngine]
     * 2. [com.lsy.matisse.engine.impl.PicassoEngine]
     * And you can implement your own image engine.
     *
     * @param imageEngine [ImageEngine]
     * @return [SelectionCreator] for fluent API.
     */
    fun imageEngine(imageEngine: ImageEngine?): SelectionCreator {
        mSelectionSpec.imageEngine = imageEngine
        return this
    }

    /**
     * 当用户选择或取消选择时，立即设置回调监听器。
     * Set listener for callback immediately when user select or unselect something.
     *
     *
     * It's a redundant API with [Matisse.obtainResult],
     * we only suggest you to use this API when you need to do something immediately.
     *
     * @param listener [OnSelectedListener]
     * @return [SelectionCreator] for fluent API.
     */
    fun setOnSelectedListener(listener: OnSelectedListener?): SelectionCreator {
        mSelectionSpec.onSelectedListener = listener
        return this
    }

    /**
     * 当用户选中或取消选中原始时，立即设置回调监听器。
     * Set listener for callback immediately when user check or uncheck original.
     *
     * @param listener [OnSelectedListener]
     * @return [SelectionCreator] for fluent API.
     */
    fun setOnCheckedListener(listener: OnCheckedListener?): SelectionCreator {
        mSelectionSpec.onCheckedListener = listener
        return this
    }

    /**
     * 是否显示预览
     */
    fun showPreview(showPreview: Boolean): SelectionCreator {
        mSelectionSpec.showPreview = showPreview
        return this
    }

    /**
     * 开始选择媒体并等待结果。
     * Start to select media and wait for result.
     *
     * @param requestCode Identity of the request Activity or Fragment.
     */
    fun forResult(requestCode: Int) {
        if (0 == mSelectionSpec.themeId){
            mSelectionSpec.themeId = R.style.Matisse_Zhihu
        }

        val activity: Activity = mMatisse.getActivity() ?: return
        val intent = Intent(activity, MatisseActivity::class.java)
        val fragment: Fragment? = mMatisse.getFragment()
        if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode)
        } else {
            activity.startActivityForResult(intent, requestCode)
        }
    }
}