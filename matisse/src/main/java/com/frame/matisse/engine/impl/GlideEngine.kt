package com.frame.matisse.engine.impl

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.frame.matisse.engine.ImageEngine

/**
 * Title : Glide加载引擎
 * Author: Lsy
 * Date: 2020/12/22 3:23 PM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class GlideEngine : ImageEngine {
    override fun loadThumbnail(
        context: Context, resize: Int, placeholder: Drawable?,
        imageView: ImageView, uri: Uri?
    ) {
        Glide.with(context)
            .asDrawable() // some .jpeg files are actually gif
            .load(uri)
            .apply(
                RequestOptions()
                    .override(resize, resize)
                    .placeholder(placeholder)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .centerCrop()
            )
            .into(MyTarget(imageView, imageView.tag))
    }

    override fun loadGifThumbnail(
        context: Context, resize: Int, placeholder: Drawable?,
        imageView: ImageView, uri: Uri?
    ) {
        Glide.with(context)
            .asDrawable() // some .jpeg files are actually gif
            .load(uri)
            .apply(
                RequestOptions()
                    .override(resize, resize)
                    .placeholder(placeholder)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .centerCrop()
            )
            .into(MyTarget(imageView))
    }

    override fun loadImage(
        context: Context, resizeX: Int, resizeY: Int,
        imageView: ImageView, uri: Uri?
    ) {
        Glide.with(context)
            .load(uri)
            .apply(
                RequestOptions()
                    .override(resizeX, resizeY)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .priority(Priority.HIGH)
                    .fitCenter()
            )
            .into(imageView)
    }

    override fun loadGifImage(
        context: Context, resizeX: Int, resizeY: Int,
        imageView: ImageView, uri: Uri?
    ) {
        Glide.with(context)
            .asGif()
            .load(uri)
            .apply(
                RequestOptions()
                    .override(resizeX, resizeY)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .priority(Priority.HIGH)
                    .fitCenter()
            )
            .into(imageView)
    }

    override fun supportAnimatedGif(): Boolean = true

    override fun clear(context: Context, imageView: ImageView?) {
        if (null != imageView) {
            Glide.with(context).clear(imageView)
        } else {
            Glide.get(context).clearMemory()
        }
    }

    private class MyTarget(val imageView: ImageView, val tag: Any? = null) :
        CustomTarget<Drawable>() {
        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
            if (null != tag && tag != imageView.tag) {
                return
            }

            imageView.setImageDrawable(resource)
        }

        override fun onLoadCleared(placeholder: Drawable?) {
        }
    }
}