package com.frame.matisse.internal.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Point
import android.media.ExifInterface
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import com.lsy.matisse.R
import com.frame.matisse.internal.entity.IncapableCause
import com.frame.matisse.internal.entity.Item
import com.frame.matisse.internal.entity.SelectionSpec
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

/**
 * Title : 照片元数据工具类
 * Author: Lsy
 * Date: 2020/12/22 11:16 AM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class PhotoMetadataUtils private constructor() {
    init {
        throw AssertionError("oops! the utility class is about to be instantiated...")
    }

    companion object {
        //标签
        private val TAG = PhotoMetadataUtils::class.java.simpleName

        //最大宽
        private val MAX_WIDTH = 1600

        /**
         * 像素点数
         */
        fun getPixelsCount(resolver: ContentResolver, uri: Uri): Int {
            val size: Point = getBitmapBound(resolver, uri)
            return size.x * size.y
        }

        /**
         * 图片的大小
         */
        fun getBitmapSize(uri: Uri, activity: Activity): Point {
            val resolver = activity.contentResolver
            val imageSize: Point = getBitmapBound(resolver, uri)
            var w = imageSize.x
            var h = imageSize.y
            if (shouldRotate(activity, uri)) {
                w = imageSize.y
                h = imageSize.x
            }
            if (h == 0) return Point(MAX_WIDTH, MAX_WIDTH)
            val metrics: DisplayMetrics = activity.resources.displayMetrics
            val screenWidth = metrics.widthPixels.toFloat()
            val screenHeight = metrics.heightPixels.toFloat()
            val widthScale = screenWidth / w
            val heightScale = screenHeight / h
            return if (widthScale > heightScale) {
                Point((w * widthScale).toInt(), (h * heightScale).toInt())
            } else Point((w * widthScale).toInt(), (h * heightScale).toInt())
        }

        /**
         * 获取图片的边界
         */
        fun getBitmapBound(resolver: ContentResolver, uri: Uri): Point {
            var inputStream: InputStream? = null
            return try {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                inputStream = resolver.openInputStream(uri)
                BitmapFactory.decodeStream(inputStream, null, options)
                val width = options.outWidth
                val height = options.outHeight
                Point(width, height)
            } catch (e: FileNotFoundException) {
                Point(0, 0)
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }

        /**
         * 判断选择的媒体类型是否满足条件
         */
        fun isAcceptable(context: Context, item: Item): IncapableCause? {
            if (!isSelectableType(context, item)) {
                return IncapableCause(message = context.getString(R.string.error_file_type))
            }

            for (filter in SelectionSpec.filters) {
                val incapableCause: IncapableCause? = filter.filter(context, item)
                if (incapableCause != null) {
                    return incapableCause
                }
            }
            return null
        }

        /**
         * 是否是需要选择的类型
         */
        private fun isSelectableType(context: Context?, item: Item): Boolean {
            return context?.let {
                for (type in SelectionSpec.mimeTypeSet) {
                    if (type.checkType(context, item.getContentUri())) {
                        return true
                    }
                }
                false
            } ?: false
        }

        /**
         * 是否需要旋转
         * @param resolver 内容解析器
         * @param uri 图片Uri
         */
        private fun shouldRotate(context: Context, uri: Uri): Boolean {
            val exif: ExifInterface = try {
                ExifInterfaceCompat.newInstance(PathUtils.getPathFromUri(context, uri))
            } catch (e: IOException) {
                Log.e(TAG, "could not read exif info of the image: $uri")
                return false
            }
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)
            return (orientation == ExifInterface.ORIENTATION_ROTATE_90
                    || orientation == ExifInterface.ORIENTATION_ROTATE_270)
        }

        /**
         * 转成MB大小
         */
        fun getSizeInMB(sizeInBytes: Long): Float {
            val df = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat
            df.applyPattern("0.0")
            var result = df.format((sizeInBytes.toFloat() / 1024 / 1024).toDouble())
            Log.e(TAG, "getSizeInMB: $result")
            result = result.replace(",".toRegex(), ".") // in some case , 0.0 will be 0,0
            return java.lang.Float.valueOf(result)
        }
    }
}