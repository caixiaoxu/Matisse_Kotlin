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
package com.frame.matisse.func

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.frame.matisse.func.MimeTypeManager.arraySetOf
import com.frame.matisse.func.internal.utils.PathUtils
import java.util.*

/**
 * MIME Type enumeration to restrict selectable media on the selection activity. Matisse only supports images and
 * videos.
 *
 *
 * Good example of mime types Android supports:
 * https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/media/java/android/media/MediaFile.java
 */
enum class MimeType(private val mineTypeName: String, private val mExtensions: Set<String>) {

    // ============== images ==============
    JPEG("image/jpeg", arraySetOf("jpg", "jpeg")),
    PNG("image/png", arraySetOf("png")),
    GIF("image/gif", arraySetOf("gif")),
    BMP("image/x-ms-bmp", arraySetOf("bmp")),
    WEBP("image/webp", arraySetOf("webp")),

    // ============== videos ==============
    MPEG("video/mpeg", arraySetOf("mpeg", "mpg")),
    MP4("video/mp4", arraySetOf("mp4", "m4v")),
    QUICKTIME("video/quicktime", arraySetOf("mov")),
    THREEGPP("video/3gpp", arraySetOf("3gp", "3gpp")),
    THREEGPP2("video/3gpp2", arraySetOf("3g2", "3gpp2")),
    MKV("video/x-matroska", arraySetOf("mkv")),
    WEBM("video/webm", arraySetOf("webm")),
    TS("video/mp2ts", arraySetOf("ts")),
    AVI("video/avi", arraySetOf("avi"));

    override fun toString(): String = mineTypeName

    /**
     * 检查媒体类型是否相同
     * @param resolver 内容解析器
     * @param uri 媒体uri
     */
    fun checkType(context: Context, uri: Uri?): Boolean = uri?.let {
        //获取扩展名
        val type = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(context.contentResolver.getType(uri))
        var path: String? = null
        // lazy load the path and prevent resolve for multiple times(惰性加载路径并多次阻止解析)
        var pathParsed = false
        //遍历扩展名
        for (extension in mExtensions) {
            //直接相同
            if (extension == type) {
                return true
            }
            // we only resolve the path for one time(我们只解决一次路径)
            if (!pathParsed) {
                //获取Uri对应的文件路径
                path = PathUtils.getPathFromUri(context, uri)
                //同一大小写
                if (!path.isNullOrEmpty()) path = path.toLowerCase(Locale.US)
                pathParsed = true
            }
            //判断路径结尾扩展名是否相同
            if (!path.isNullOrEmpty() && path.endsWith(extension)) {
                return true
            }
        }
        return false
    } ?: false
}