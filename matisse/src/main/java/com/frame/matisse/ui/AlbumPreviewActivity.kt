package com.frame.matisse.ui

import android.database.Cursor
import android.os.Bundle
import com.frame.matisse.internal.entity.Album
import com.frame.matisse.internal.entity.Item
import com.frame.matisse.internal.entity.SelectionSpec
import com.frame.matisse.internal.model.AlbumMediaCollection
import com.frame.matisse.internal.ui.adapter.PreviewPagerAdapter
import java.util.*

/**
 * Title :
 * Author: Lsy
 * Date: 2020/12/23 5:30 PM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class AlbumPreviewActivity : BasePreviewActivity(), AlbumMediaCollection.AlbumMediaCallbacks {
    companion object {
        const val EXTRA_ALBUM = "extra_album"
        const val EXTRA_ITEM = "extra_item"
    }

    private val mCollection: AlbumMediaCollection = AlbumMediaCollection()

    private var mIsAlreadySetPosition = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!SelectionSpec.hasInited) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }
        mCollection.onCreate(this, this)
        val album: Album? = intent.getParcelableExtra(EXTRA_ALBUM)
        mCollection.load(album)
        val item: Item? = intent.getParcelableExtra(EXTRA_ITEM)
        if (SelectionSpec.countable) {
            mCheckView.setCheckedNum(mSelectedCollection.checkedNumOf(item))
        } else {
            mCheckView.setChecked(mSelectedCollection.isSelected(item))
        }
        updateSize(item)
    }

//    override fun updateSize(item: Item?) {
//        item?.let {
//            if (item.isGif()) {
//                mSize.visibility = View.VISIBLE
//                mSize.text = "${PhotoMetadataUtils.getSizeInMB(item.size)}M"
//            } else {
//                mSize.visibility = View.GONE
//            }
//            if (item.isVideo()) {
//                mOriginalLayout.visibility = View.GONE
//            } else if (SelectionSpec.originalable) {
//                mOriginalLayout.visibility = View.VISIBLE
//            }
//        }
//    }

    override fun onAlbumMediaLoad(cursor: Cursor?) {
        cursor?.let {
            val items: MutableList<Item> = ArrayList()
            while (cursor.moveToNext()) {
                items.add(Item.valueOf(cursor))
            }
//            cursor.close()
            if (items.isEmpty()) {
                return
            }
            val adapter: PreviewPagerAdapter? = mPager.adapter as? PreviewPagerAdapter
            adapter?.addAll(items)
            adapter?.notifyDataSetChanged()
            if (!mIsAlreadySetPosition) {
                //onAlbumMediaLoad is called many times..
                mIsAlreadySetPosition = true
                val selected: Item? = intent.getParcelableExtra(EXTRA_ITEM)
                val selectedIndex = items.indexOf(selected)
                mPager.setCurrentItem(selectedIndex, false)
                mPreviousPos = selectedIndex
            }
        }
    }

    override fun onAlbumMediaReset() {
    }
}