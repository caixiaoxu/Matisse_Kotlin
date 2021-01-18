/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.frame.matisse.func.engine.impl

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.frame.matisse.func.engine.ImageEngine
import com.squareup.picasso.Picasso

/**
 * [ImageEngine] implementation using Picasso.
 */
class PicassoEngine : ImageEngine {
    override fun loadThumbnail(
        context: Context, resize: Int, placeholder: Drawable?, imageView: ImageView, uri: Uri?
    ) {
        Picasso.get().load(uri).apply {
            placeholder?.let { placeholder(placeholder) }
        }.resize(resize, resize).centerCrop().into(imageView)
    }

    override fun loadGifThumbnail(
        context: Context, resize: Int, placeholder: Drawable?, imageView: ImageView, uri: Uri?
    ) {
        loadThumbnail(context, resize, placeholder, imageView, uri)
    }

    override fun loadImage(
        context: Context, resizeX: Int, resizeY: Int,
        imageView: ImageView, uri: Uri?
    ) {
        Picasso.get().load(uri).resize(resizeX, resizeY).priority(Picasso.Priority.HIGH)
            .centerInside().into(imageView)
    }

    override fun loadGifImage(
        context: Context, resizeX: Int, resizeY: Int,
        imageView: ImageView, uri: Uri?
    ) {
        loadImage(context, resizeX, resizeY, imageView, uri)
    }

    override fun supportAnimatedGif(): Boolean {
        return false
    }

    override fun clear(context: Context, imageView: ImageView?) {
        imageView?.let {
            Picasso.get().cancelRequest(imageView)
        }
    }
}