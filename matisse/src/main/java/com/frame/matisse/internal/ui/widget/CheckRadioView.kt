package com.frame.matisse.internal.ui.widget

import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import com.lsy.matisse.R
import com.frame.matisse.internal.utils.Platform

/**
 * Title :
 * Author: Lsy
 * Date: 2020/12/23 11:54 AM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class CheckRadioView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var mSelectedColor = 0
    private var mUnSelectUdColor = 0

    init {
        mSelectedColor = ResourcesCompat.getColor(
            resources, R.color.zhihu_item_checkCircle_backgroundColor, getContext().theme
        )
        mUnSelectUdColor = ResourcesCompat.getColor(
            resources, R.color.zhihu_check_original_radio_disable, getContext().theme
        )
        setChecked(false)
    }

    fun setChecked(enable: Boolean) {
        if (enable) {
            setImageResource(R.drawable.ic_preview_radio_on)
            setColor(mSelectedColor)
        } else {
            setImageResource(R.drawable.ic_preview_radio_off)
            setColor(mUnSelectUdColor)
        }
    }

    fun setColor(color: Int) {
        if (Platform.beforeAndroidTen()){
            drawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        } else {
            drawable?.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_IN)
        }
    }
}