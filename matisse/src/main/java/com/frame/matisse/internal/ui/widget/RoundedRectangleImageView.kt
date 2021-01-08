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
package com.frame.matisse.internal.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class RoundedRectangleImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    private var mRadius = 0f// dp
    private var mRoundedRectPath: Path? = null
    private var mRectF: RectF? = null

    init {
        val density = context.resources.displayMetrics.density
        mRadius = 2.0f * density
        mRoundedRectPath = Path()
        mRectF = RectF()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mRectF!![0.0f, 0.0f, measuredWidth.toFloat()] = measuredHeight.toFloat()
        mRoundedRectPath!!.addRoundRect(mRectF!!, mRadius, mRadius, Path.Direction.CW)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.clipPath(mRoundedRectPath!!)
        super.onDraw(canvas)
    }
}