package com.frame.matisse.ui

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lsy.matisse.R
import com.frame.matisse.internal.entity.Album
import com.frame.matisse.internal.entity.Item
import com.frame.matisse.internal.entity.SelectionSpec
import com.frame.matisse.internal.model.AlbumMediaCollection
import com.frame.matisse.internal.model.SelectedItemCollection
import com.frame.matisse.internal.ui.adapter.AlbumMediaAdapter
import com.frame.matisse.internal.ui.widget.MediaGridInset
import com.frame.matisse.internal.utils.UIUtils


/**
 * Title : 对应的相册fragment界面
 * Author: Lsy
 * Date: 2020/12/23 3:09 PM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class MediaSelectionFragment : Fragment(), AlbumMediaAdapter.CheckStateListener,
    AlbumMediaAdapter.OnMediaClickListener, AlbumMediaCollection.AlbumMediaCallbacks {

    companion object {
        const val EXTRA_ALBUM = "extra_album"

        fun newInstance(album: Album): MediaSelectionFragment {
            val fragment = MediaSelectionFragment()
            val args = Bundle()
            args.putParcelable(EXTRA_ALBUM, album)
            fragment.arguments = args
            return fragment
        }
    }

    //媒体文件集合
    private val mAlbumMediaCollection: AlbumMediaCollection = AlbumMediaCollection()

    //列表
    private lateinit var mRecyclerView: RecyclerView

    //列表适配器
    private var mAdapter: AlbumMediaAdapter? = null

    //回调接口
    private lateinit var mSelectionProvider: SelectionProvider
    private var mCheckStateListener: AlbumMediaAdapter.CheckStateListener? = null
    private var mOnMediaClickListener: AlbumMediaAdapter.OnMediaClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mSelectionProvider = if (context is SelectionProvider) {
            context
        } else {
            throw IllegalStateException("Context must implement SelectionProvider.")
        }
        if (context is AlbumMediaAdapter.CheckStateListener) {
            mCheckStateListener = context
        }
        if (context is AlbumMediaAdapter.OnMediaClickListener) {
            mOnMediaClickListener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_media_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRecyclerView = view.findViewById<View>(R.id.recyclerview) as RecyclerView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val album: Album? = arguments?.getParcelable(EXTRA_ALBUM)
        initRecyclerView()
        mAlbumMediaCollection.onCreate(activity!!, this)
        mAlbumMediaCollection.load(album, SelectionSpec.capture)
    }

    /**
     * 初始化列表
     */
    private fun initRecyclerView(){
        mAdapter = AlbumMediaAdapter(
            context!!, mSelectionProvider.provideSelectedItemCollection(), mRecyclerView
        )
        mAdapter?.registerCheckStateListener(this)
        mAdapter?.registerOnMediaClickListener(this)
        mRecyclerView.setHasFixedSize(true)
        val spanCount: Int = if (SelectionSpec.gridExpectedSize > 0) {
            UIUtils.spanCount(context!!, SelectionSpec.gridExpectedSize)
        } else {
            SelectionSpec.spanCount
        }
        mRecyclerView.layoutManager = GridLayoutManager(context, spanCount)
        val spacing = resources.getDimensionPixelSize(R.dimen.media_grid_spacing)
        mRecyclerView.addItemDecoration(MediaGridInset(spanCount, spacing, false))
        mRecyclerView.adapter = mAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mAlbumMediaCollection.onDestroy()
    }

    fun refreshMediaGrid() {
        mAdapter?.notifyDataSetChanged()
    }

    fun refreshSelection() {
        mAdapter?.refreshSelection()
    }

    override fun onUpdate() {
        // notify outer Activity that check state changed
        mCheckStateListener?.onUpdate()
    }

    override fun onMediaClick(album: Album?, item: Item?, adapterPosition: Int) {
        mOnMediaClickListener?.onMediaClick(
            arguments?.getParcelable<Parcelable>(EXTRA_ALBUM) as Album?, item, adapterPosition
        )
    }

    override fun onAlbumMediaLoad(cursor: Cursor?) {
        mAdapter?.swapCursor(cursor)
    }

    override fun onAlbumMediaReset() {
        mAdapter?.swapCursor(null)
    }

    interface SelectionProvider {
        fun provideSelectedItemCollection(): SelectedItemCollection
    }
}