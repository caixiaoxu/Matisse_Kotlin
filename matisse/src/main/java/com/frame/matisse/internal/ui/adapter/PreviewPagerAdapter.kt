package com.frame.matisse.internal.ui.adapter

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.frame.matisse.func.internal.entity.Item
import com.frame.matisse.ui.PreviewItemFragment
import java.util.*

/**
 * Title : 预览界面Adapter
 * Author: Lsy
 * Date: 2020/12/23 2:10 PM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class PreviewPagerAdapter(
    manager: FragmentManager,
    private val listener: OnPrimaryItemSetListener?
) : FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val mItems: ArrayList<Item> = ArrayList<Item>()

    override fun getCount(): Int = mItems.size

    override fun getItem(position: Int): Fragment =
        PreviewItemFragment.newInstance(mItems[position])

    override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
        super.setPrimaryItem(container, position, obj)
        listener?.onPrimaryItemSet(position)
    }

    fun getMediaItem(position: Int): Item = mItems[position]

    fun addAll(items: List<Item>) {
        mItems.addAll(items)
    }

    interface OnPrimaryItemSetListener {
        fun onPrimaryItemSet(position: Int)
    }
}