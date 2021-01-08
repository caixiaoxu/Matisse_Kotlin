package com.frame.matisse.internal.utils

import android.os.Build

/**
 * @author Lsy
 */
object Platform {
    /**
     *  4.0(api 14)及以上的版本
     */
    fun afterICS(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH

    /**
     * 4.4(api 19)及以上的版本
     */
    fun afterKitKat(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

    /**
     * 5.0(api 21)之前的版本
     */
    fun beforeLollipop(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP

    /**
     * 10(api 29)之前的版本
     */
    fun beforeAndroidTen(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

}