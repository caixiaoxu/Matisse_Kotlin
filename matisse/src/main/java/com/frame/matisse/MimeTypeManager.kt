/*
 * Copyright (C) 2014 nohana, Inc.
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.frame.matisse

import androidx.collection.ArraySet
import java.util.*

/**
 * MIME Type enumeration to restrict selectable media on the selection activity. Matisse only supports images and
 * videos.
 *
 *
 * Good example of mime types Android supports:
 * https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/media/java/android/media/MediaFile.java
 */
object MimeTypeManager {
    /**
     * 封装扩展名参数
     */
    fun arraySetOf(vararg suffixes: String): Set<String> = ArraySet(listOf(*suffixes))

    /**
     * 所有类型
     */
    fun ofAll(): Set<MimeType> = EnumSet.allOf(MimeType::class.java)

    /**
     * 指定类型
     */
    fun of(type: MimeType, vararg rest: MimeType): Set<MimeType> = EnumSet.of(type, *rest)

    /**
     * 图片类型
     */
    fun ofImage(): Set<MimeType> = EnumSet.of(
        MimeType.JPEG, MimeType.PNG,
        MimeType.GIF, MimeType.BMP, MimeType.WEBP
    )

    /**
     * 图片类型
     * @param onlyGif 仅gif
     */
    fun ofImage(onlyGif: Boolean): Set<MimeType> = EnumSet.of(MimeType.GIF)

    /**
     * gif类型
     */
    fun ofGif(): Set<MimeType> = ofImage(true)

    /**
     * 视频类型
     */
    fun ofVideo(): Set<MimeType> =
        EnumSet.of(
            MimeType.MPEG,
            MimeType.MP4, MimeType.QUICKTIME, MimeType.THREEGPP,
            MimeType.THREEGPP2, MimeType.MKV, MimeType.WEBM, MimeType.TS, MimeType.AVI
        )

    /**
     * 是否是图片类型
     * @param mimeType 类型名
     */
    fun isImage(mimeType: String?): Boolean =
        mimeType?.let { mimeType.startsWith("image") } ?: false

    /**
     * 是否是视频类型
     * @param mimeType 类型名
     */
    fun isVideo(mimeType: String?): Boolean =
        mimeType?.let { mimeType.startsWith("video") } ?: false


    /**
     * 是否是gif类型
     * @param mimeType 类型名
     */
    fun isGif(mimeType: String?): Boolean =
        mimeType?.let { mimeType == MimeType.GIF.toString() } ?: false
}