package com.frame.matisse.ui

import android.os.Bundle
import com.frame.matisse.func.internal.entity.Item
import com.frame.matisse.func.internal.entity.SelectionSpec
import com.frame.matisse.func.internal.model.SelectedItemCollection

/**
 * Title :
 * Author: Lsy
 * Date: 2020/12/23 3:05 PM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class SelectedPreviewActivity : BasePreviewActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!SelectionSpec.hasInited) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }
        val bundle = intent.getBundleExtra(EXTRA_DEFAULT_BUNDLE)
        val selected: List<Item> =
            bundle?.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION) ?: listOf()
        mAdapter?.addAll(selected)
        mAdapter?.notifyDataSetChanged()
        if (SelectionSpec.countable) {
            mCheckView.setCheckedNum(1)
        } else {
            mCheckView.setChecked(true)
        }
        mPreviousPos = 0
        updateSize(selected[0])
    }
}