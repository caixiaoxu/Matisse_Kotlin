package com.lsy.matisse_kotlin

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import coil.clear
import coil.load
import com.frame.matisse.func.engine.ImageEngine

/**
 * Title : Coil 图片加载引擎
 * Author: Lsy
 * Date: 1/4/21 5:28 PM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class CoilEngine : ImageEngine {
    override fun loadThumbnail(
        context: Context,
        resize: Int,
        placeholder: Drawable?,
        imageView: ImageView,
        uri: Uri?
    ) {
        imageView.load(uri) {
            crossfade(true)
            placeholder(placeholder)
            error(placeholder)
            size(resize, resize)
        }
    }

    override fun loadGifThumbnail(
        context: Context,
        resize: Int,
        placeholder: Drawable?,
        imageView: ImageView,
        uri: Uri?
    ) {
        imageView.load(uri) {
            crossfade(true)
            placeholder(placeholder)
            error(placeholder)
            size(resize, resize)
        }
    }

    override fun loadImage(
        context: Context,
        resizeX: Int,
        resizeY: Int,
        imageView: ImageView,
        uri: Uri?
    ) {
        imageView.load(uri) {
            crossfade(true)
            size(resizeX, resizeY)
        }
    }

    override fun loadGifImage(
        context: Context,
        resizeX: Int,
        resizeY: Int,
        imageView: ImageView,
        uri: Uri?
    ) {
        imageView.load(uri) {
            crossfade(true)
            size(resizeX, resizeY)
        }
    }

    override fun supportAnimatedGif(): Boolean = true

    override fun clear(context: Context, imageView: ImageView?) {
        imageView?.clear()
    }
}