package com.frame.matisse.internal.model

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.frame.matisse.internal.loader.AlbumLoader
import java.lang.ref.WeakReference

/**
 * Title : 相册目录集合
 * Author: Lsy
 * Date: 2020/12/22 5:00 PM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class AlbumCollection : LoaderManager.LoaderCallbacks<Cursor> {
    companion object {
        private const val LOADER_ID = 1
        private const val STATE_CURRENT_SELECTION = "state_current_selection"
    }

    private var mContext: WeakReference<Context>? = null
    private var mLoaderManager: LoaderManager? = null
    private var mCallbacks: AlbumCallbacks? = null
    var mCurrentSelection = 0 //选择的当前位置
    private var mLoadFinished = false

    /**---------实现父类三个方法:Start---------**/

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val context = mContext!!.get()
        mLoadFinished = false
        return AlbumLoader.newInstance(context!!)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        mContext?.get() ?: return
        if (!mLoadFinished) {
            mLoadFinished = true
            mCallbacks?.onAlbumLoad(data)
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        mContext?.get() ?: return
        mCallbacks?.onAlbumReset()
    }

    /**---------实现父类三个方法:End---------**/

    /**---------生命周期四个方法:Start---------**/

    /**
     * 创建时
     */
    fun onCreate(activity: FragmentActivity, callbacks: AlbumCallbacks) {
        mContext = WeakReference(activity)
        mLoaderManager = LoaderManager.getInstance(activity)
        mCallbacks = callbacks
    }

    /**
     * 保存数据
     */
    fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_CURRENT_SELECTION, mCurrentSelection)
    }

    /**
     * 恢复数据
     */
    fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            mCurrentSelection = savedInstanceState.getInt(STATE_CURRENT_SELECTION)
        }
    }

    /**
     * 销毁
     */
    fun onDestroy() {
        mLoaderManager?.destroyLoader(LOADER_ID)
        mCallbacks = null
    }

    /**---------生命周期四个方法:End---------**/

    /**
     * 加载相册专辑
     */
    open fun loadAlbums() {
        mLoaderManager?.initLoader(LOADER_ID, null, this)
    }

    interface AlbumCallbacks {
        /**
         * 加载完成
         */
        fun onAlbumLoad(cursor: Cursor?)

        /**
         * 加载重置
         */
        fun onAlbumReset()
    }

}