package com.frame.matisse.internal.entity

/**
 * Title :
 * Author: Lsy
 * Date: 2020/12/22 3:16 PM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class CaptureStrategy(
    val isPublic: Boolean,
    val authority: String,
    val directory: String? = null
)