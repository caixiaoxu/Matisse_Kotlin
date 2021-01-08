package com.frame.matisse.filter

import android.content.Context
import com.frame.matisse.MimeType
import com.frame.matisse.internal.entity.IncapableCause
import com.frame.matisse.internal.entity.Item

/**
 * Title :
 * Author: Lsy
 * Date: 2020/12/22 3:13 PM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
abstract class Filter {
    companion object {

        // Convenient constant for a minimum value
        const val MIN = 0

        // Convenient constant for a maximum value
        const val MAX = Int.MAX_VALUE

        // Convenient constant for 1024
        const val K = 1024
    }

    // Against what mime types this filter applies
    abstract fun constraintTypes(): Set<MimeType>

    /**
     * Invoked for filtering each item
     *
     * @return null if selectable, {@link IncapableCause} if not selectable.
     */
    abstract fun filter(context: Context, item: Item?): IncapableCause?

    // Whether an {@link Item} need filtering
    open fun needFiltering(context: Context, item: Item): Boolean {
        for (type in constraintTypes()) {
            if (type.checkType(context, item.getContentUri())) {
                return true
            }
        }
        return false
    }
}