package com.frame.matisse.func.internal.model

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.frame.matisse.func.internal.entity.Album
import com.frame.matisse.func.internal.loader.AlbumMediaLoader
import java.lang.ref.WeakReference

/**
 * Title : 媒体内容集合
 * Author: Lsy
 * Date: 2020/12/23 3:11 PM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class AlbumMediaCollection : LoaderManager.LoaderCallbacks<Cursor> {

    companion object {
        private const val LOADER_ID = 2
        private const val ARGS_ALBUM = "args_album"
        private const val ARGS_ENABLE_CAPTURE = "args_enable_capture"
    }

    private var mContext: WeakReference<Context>? = null
    private var mLoaderManager: LoaderManager? = null
    private var mCallbacks: AlbumMediaCallbacks? = null

    fun onCreate(context: FragmentActivity, callbacks: AlbumMediaCallbacks) {
        mContext = WeakReference(context)
        mLoaderManager = LoaderManager.getInstance(context)
        mCallbacks = callbacks
    }

    fun onDestroy() {
        mLoaderManager?.destroyLoader(LOADER_ID)
        mCallbacks = null
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val context = mContext!!.get()
        val album: Album? = args?.getParcelable(ARGS_ALBUM)

        return AlbumMediaLoader.newInstance(
            context!!, album!!,
            album.isAll() && args.getBoolean(ARGS_ENABLE_CAPTURE, false)
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        mContext?.get() ?: return
        mCallbacks?.onAlbumMediaLoad(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        mContext?.get() ?: return
        mCallbacks?.onAlbumMediaReset()
    }

    fun load(target: Album?) {
        load(target, false)
    }

    fun load(target: Album?, enableCapture: Boolean) {
        val args = Bundle()
        args.putParcelable(ARGS_ALBUM, target)
        args.putBoolean(ARGS_ENABLE_CAPTURE, enableCapture)
        mLoaderManager?.initLoader(LOADER_ID, args, this)
    }

    interface AlbumMediaCallbacks {
        fun onAlbumMediaLoad(cursor: Cursor?)
        fun onAlbumMediaReset()
    }
}