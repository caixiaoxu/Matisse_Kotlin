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
package com.lsy.matisse_kotlin

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lsy.matisse.Matisse
import com.lsy.matisse.MimeType
import com.lsy.matisse.MimeTypeManager
import com.lsy.matisse.engine.impl.GlideEngine
import com.lsy.matisse.engine.impl.PicassoEngine
import com.lsy.matisse.filter.Filter
import com.lsy.matisse.internal.entity.CaptureStrategy
import com.lsy.matisse.listener.OnCheckedListener
import com.lsy.matisse.listener.OnSelectedListener
import com.tbruyelle.rxpermissions2.RxPermissions
import java.lang.String
import java.lang.Throwable
import kotlin.Int
import kotlin.also

class MainActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val REQUEST_CODE_CHOOSE = 23
    }

    private var mAdapter: UriAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.zhihu).setOnClickListener(this)
        findViewById<View>(R.id.dracula).setOnClickListener(this)
        findViewById<View>(R.id.only_gif).setOnClickListener(this)
        val recyclerView = findViewById<View>(R.id.recyclerview) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = UriAdapter().also {
            mAdapter = it
        }
    }

    // <editor-fold defaultstate="collapsed" desc="onClick">
    @SuppressLint("CheckResult")
    override fun onClick(v: View?) {
        v?.let {
            val rxPermissions = RxPermissions(this)
            rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe({ aBoolean ->
                    if (aBoolean) {
                        startAction(v)
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            R.string.permission_request_denied,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }, { it.printStackTrace() })
        }
    }

    // </editor-fold>
    private fun startAction(v: View) {
        when (v.id) {
            R.id.zhihu -> Matisse.from(this@MainActivity)
                .choose(MimeTypeManager.ofImage(), false)
                .countable(true)
                .capture(true)
                .captureStrategy(
                    CaptureStrategy(true, "com.lsy.matisse.sample.fileprovider", "test")
                )
                .maxSelectable(9)
                .addFilter(GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .gridExpectedSize(
                    resources.getDimensionPixelSize(R.dimen.grid_expected_size)
                )
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .thumbnailScale(0.6f)
                .imageEngine(GlideEngine())
                .setOnSelectedListener(object : OnSelectedListener {

                    override fun onSelected(uriList: List<Uri>, pathList: List<kotlin.String?>) {
                        Log.e("onSelected", "onSelected: pathList=$pathList")
                    }
                })
                .showSingleMediaType(true)
                .originalEnable(true)
                .maxOriginalSize(10)
                .autoHideToolbarOnSingleTap(true)
                .setOnCheckedListener(object : OnCheckedListener {
                    override fun onCheck(isChecked: Boolean) {
                        Log.e("isChecked", "onCheck: isChecked=$isChecked")
                    }
                })
                .forResult(REQUEST_CODE_CHOOSE)
            R.id.dracula -> Matisse.from(this@MainActivity)
                .choose(MimeTypeManager.ofImage())
                .theme(R.style.Matisse_Dracula)
                .countable(false)
                .addFilter(GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .maxSelectable(9)
                .thumbnailScale(0.6f)
                .originalEnable(true)
                .maxOriginalSize(10)
                .imageEngine(PicassoEngine())
                .forResult(REQUEST_CODE_CHOOSE)
            R.id.only_gif -> Matisse.from(this@MainActivity)
                .choose(MimeTypeManager.ofImage())
                .countable(true)
                .maxSelectable(9)
                .addFilter(GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .gridExpectedSize(
                    resources.getDimensionPixelSize(R.dimen.grid_expected_size)
                )
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .thumbnailScale(0.6f)
                .imageEngine(CoilEngine())
                .showSingleMediaType(true)
                .originalEnable(true)
                .maxOriginalSize(10)
                .autoHideToolbarOnSingleTap(true)
                .forResult(REQUEST_CODE_CHOOSE)
            else -> {
            }
        }
        mAdapter!!.setData(null, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK && null != data) {
            mAdapter?.setData(Matisse.obtainResult(data), Matisse.obtainPathResult(data))
            Log.e("OnActivityResult ", String.valueOf(Matisse.obtainOriginalState(data)))
        }
    }
}