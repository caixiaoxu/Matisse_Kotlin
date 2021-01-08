package com.frame.matisse.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.viewpager.widget.ViewPager
import com.lsy.matisse.R
import com.frame.matisse.internal.entity.IncapableCause
import com.frame.matisse.internal.entity.Item
import com.frame.matisse.internal.entity.SelectionSpec
import com.frame.matisse.internal.model.SelectedItemCollection
import com.frame.matisse.internal.utils.PhotoMetadataUtils
import com.frame.matisse.internal.utils.Platform
import com.frame.matisse.listener.OnFragmentInteractionListener
import com.frame.matisse.internal.ui.adapter.PreviewPagerAdapter
import com.frame.matisse.internal.ui.widget.CheckRadioView
import com.frame.matisse.internal.ui.widget.CheckView
import com.frame.matisse.internal.ui.widget.IncapableDialog

/**
 * Title : 预览界面基本类
 * Author: Lsy
 * Date: 2020/12/23 2:08 PM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
abstract class BasePreviewActivity : AppCompatActivity(), View.OnClickListener,
    ViewPager.OnPageChangeListener, OnFragmentInteractionListener {

    companion object {
        const val EXTRA_DEFAULT_BUNDLE = "extra_default_bundle"
        const val EXTRA_RESULT_BUNDLE = "extra_result_bundle"
        const val EXTRA_RESULT_APPLY = "extra_result_apply"
        const val EXTRA_RESULT_ORIGINAL_ENABLE = "extra_result_original_enable"
        const val CHECK_STATE = "checkState"
    }

    //选择集合
    protected val mSelectedCollection: SelectedItemCollection = SelectedItemCollection(this)

    //列表
    protected lateinit var mPager: ViewPager
    protected var mAdapter: PreviewPagerAdapter? = null

    //选中和应用按钮
    protected lateinit var mCheckView: CheckView
    protected lateinit var mButtonBack: TextView
    protected lateinit var mButtonApply: TextView
    protected lateinit var mSize: TextView

    protected var mPreviousPos = -1

    //原图按钮
    protected lateinit var mOriginalLayout: LinearLayout
    private lateinit var mOriginal: CheckRadioView
    protected var mOriginalEnable = false

    //顶部底部工具栏
    private lateinit var mBottomToolbar: FrameLayout
    private lateinit var mTopToolbar: FrameLayout
    private var mIsToolbarHide = false

    override fun onCreate(savedInstanceState: Bundle?) {
        //样式
        setTheme(SelectionSpec.themeId)
        super.onCreate(savedInstanceState)
        if (!SelectionSpec.hasInited) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }
        setContentView(R.layout.activity_media_preview)
        if (Platform.afterKitKat()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        //需要调整屏幕方向
        if (SelectionSpec.needOrientationRestriction()) {
            requestedOrientation = SelectionSpec.orientation
        }
        //按钮状态
        mOriginalEnable = if (savedInstanceState == null) {
            mSelectedCollection.onCreate(intent.getBundleExtra(EXTRA_DEFAULT_BUNDLE))
            intent.getBooleanExtra(EXTRA_RESULT_ORIGINAL_ENABLE, false)
        } else {
            mSelectedCollection.onCreate(savedInstanceState)
            savedInstanceState.getBoolean(CHECK_STATE)
        }
        //返回按钮
        mButtonBack = findViewById<View>(R.id.button_back) as TextView
        mButtonBack.setOnClickListener(this)
        //确认按钮
        mButtonApply = findViewById<View>(R.id.button_apply) as TextView
        mButtonApply.setOnClickListener(this)
        //编号
        mSize = findViewById<View>(R.id.size) as TextView

        //翻页
        mPager = findViewById<View>(R.id.pager) as ViewPager
        mPager.addOnPageChangeListener(this)
        mAdapter = PreviewPagerAdapter(supportFragmentManager, null)
        mPager.adapter = mAdapter

        //底部工具栏
        mBottomToolbar = findViewById(R.id.bottom_toolbar)
        //顶部工具栏
        mTopToolbar = findViewById(R.id.top_toolbar)
        //选中按钮
        mCheckView = findViewById<View>(R.id.check_view) as CheckView
        mCheckView.setCountable(SelectionSpec.countable)
        mCheckView.setOnClickListener(this)
        //原图按钮
        mOriginalLayout = findViewById(R.id.originalLayout)
        mOriginal = findViewById(R.id.original)
        mOriginalLayout.setOnClickListener(this)
        updateApplyButton()
    }

    /**
     * 是否满足选中条件
     */
    private fun assertAddSelection(item: Item): Boolean {
        val cause: IncapableCause? = mSelectedCollection.isAcceptable(item)
        IncapableCause.handleCause(this, cause)
        return cause == null
    }

    /**
     * 刷新应用按钮状态
     */
    private fun updateApplyButton() {
        val selectedCount = mSelectedCollection.count()
        if (selectedCount == 0) {
            mButtonApply.setText(R.string.button_apply_default)
            mButtonApply.isEnabled = false
        } else if (selectedCount == 1 && SelectionSpec.singleSelectionModeEnabled()) {
            mButtonApply.setText(R.string.button_apply_default)
            mButtonApply.isEnabled = true
        } else {
            mButtonApply.isEnabled = true
            mButtonApply.text = getString(R.string.button_apply, selectedCount)
        }
        if (SelectionSpec.originalable) {
            mOriginalLayout.visibility = View.VISIBLE
            updateOriginalState()
        } else {
            mOriginalLayout.visibility = View.GONE
        }
    }

    /**
     * 刷新原图按钮状态
     */
    private fun updateOriginalState() {
        mOriginal.setChecked(mOriginalEnable)
        if (!mOriginalEnable) {
            mOriginal.setColor(Color.WHITE)
        }
        if (countOverMaxSize() > 0) {
            if (mOriginalEnable) {
                val incapableDialog = IncapableDialog.newInstance(
                    "", getString(R.string.error_over_original_size, SelectionSpec.originalMaxSize)
                )
                incapableDialog.show(
                    supportFragmentManager,
                    IncapableDialog::class.java.name
                )
                mOriginal.setChecked(false)
                mOriginal.setColor(Color.WHITE)
                mOriginalEnable = false
            }
        }
    }

    /**
     * 是否超过最大可选择原图数
     */
    private fun countOverMaxSize(): Int {
        var count = 0
        val selectedCount = mSelectedCollection.count()
        for (i in 0 until selectedCount) {
            val item = mSelectedCollection.asList()[i]
            if (item.isImage()) {
                val size: Float = PhotoMetadataUtils.getSizeInMB(item.size)
                if (size > SelectionSpec.originalMaxSize) {
                    count++
                }
            }
        }
        return count
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mSelectedCollection.onSaveInstanceState(outState)
        outState.putBoolean("checkState", mOriginalEnable)
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        sendBackResult(false)
        super.onBackPressed()
    }

    override fun onDestroy() {
        SelectionSpec.imageEngine?.clear(this)
        super.onDestroy()
    }

    protected open fun sendBackResult(apply: Boolean) {
        val intent = Intent()
        intent.putExtra(EXTRA_RESULT_BUNDLE, mSelectedCollection.getDataWithBundle())
        intent.putExtra(EXTRA_RESULT_APPLY, apply)
        intent.putExtra(EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable)
        setResult(RESULT_OK, intent)
    }

    override fun onClick(v: View?) {
        when (v) {
            mButtonBack -> onBackPressed()
            mButtonApply -> {
                //应用
                sendBackResult(true)
                finish()
            }
            mCheckView -> {
                //选中
                val item: Item = mAdapter!!.getMediaItem(mPager.currentItem)
                //选中移除，未选中添加
                if (mSelectedCollection.isSelected(item)) {
                    mSelectedCollection.remove(item)
                    if (SelectionSpec.countable) {
                        mCheckView.setCheckedNum(CheckView.UNCHECKED)
                    } else {
                        mCheckView.setChecked(false)
                    }
                } else {
                    if (assertAddSelection(item)) {
                        mSelectedCollection.add(item)
                        if (SelectionSpec.countable) {
                            mCheckView.setCheckedNum(mSelectedCollection.checkedNumOf(item))
                        } else {
                            mCheckView.setChecked(true)
                        }
                    }
                }
                updateApplyButton()
                if (SelectionSpec.onSelectedListener != null) {
                    SelectionSpec.onSelectedListener!!.onSelected(
                        mSelectedCollection.asListOfUri(), mSelectedCollection.asListOfString()
                    )
                }
            }
            mOriginalLayout -> {
                //原图按钮
                //是否超过最大可选择的原图数量
                val count: Int = countOverMaxSize()
                if (count > 0) {
                    val incapableDialog: IncapableDialog = IncapableDialog.newInstance(
                        "",
                        getString(
                            R.string.error_over_original_count, count, SelectionSpec.originalMaxSize
                        )
                    )
                    incapableDialog.show(supportFragmentManager, IncapableDialog::class.java.name)
                    return
                }
                mOriginalEnable = !mOriginalEnable
                mOriginal.setChecked(mOriginalEnable)
                if (!mOriginalEnable) {
                    mOriginal.setColor(Color.WHITE)
                }
                if (SelectionSpec.onCheckedListener != null) {
                    SelectionSpec.onCheckedListener!!.onCheck(mOriginalEnable)
                }
            }
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        val adapter = mPager.adapter as? PreviewPagerAdapter
        if (mPreviousPos != -1 && mPreviousPos != position) {
            //重置界面
            (adapter?.instantiateItem(mPager, mPreviousPos) as? PreviewItemFragment)?.resetView()
            //获取当前位置数据
            val item = adapter?.getMediaItem(position)
            if (SelectionSpec.countable) {
                //多选
                val checkedNum = mSelectedCollection.checkedNumOf(item)
                mCheckView.setCheckedNum(checkedNum)
                if (checkedNum > 0) {
                    mCheckView.isEnabled = true
                } else {
                    mCheckView.isEnabled = !mSelectedCollection.maxSelectableReached()
                }
            } else {
                //单选
                val checked = mSelectedCollection.isSelected(item)
                mCheckView.setChecked(checked)
                if (checked) {
                    mCheckView.isEnabled = true
                } else {
                    mCheckView.isEnabled = !mSelectedCollection.maxSelectableReached()
                }
            }
            updateSize(item)
        }
        mPreviousPos = position
    }

    /**
     * 刷新数量
     */
    protected open fun updateSize(item: Item?) {
        item?.let {
            if (item.isGif()) {
                mSize.visibility = View.VISIBLE
                mSize.text = "${PhotoMetadataUtils.getSizeInMB(item.size)}M"
            } else {
                mSize.visibility = View.GONE
            }
            if (item.isVideo()) {
                mOriginalLayout.visibility = View.GONE
            } else if (SelectionSpec.originalable) {
                mOriginalLayout.visibility = View.VISIBLE
            }
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onClick() {
        //自动隐藏工具栏
        if (!SelectionSpec.autoHideToobar) {
            return
        }
        if (mIsToolbarHide) {
            mTopToolbar.animate()
                .setInterpolator(FastOutSlowInInterpolator())
                .translationYBy(mTopToolbar.measuredHeight.toFloat())
                .start()
            mBottomToolbar.animate()
                .translationYBy(-mBottomToolbar.measuredHeight.toFloat())
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
        } else {
            mTopToolbar.animate()
                .setInterpolator(FastOutSlowInInterpolator())
                .translationYBy(-mTopToolbar.measuredHeight.toFloat())
                .start()
            mBottomToolbar.animate()
                .setInterpolator(FastOutSlowInInterpolator())
                .translationYBy(mBottomToolbar.measuredHeight.toFloat())
                .start()
        }
        mIsToolbarHide = !mIsToolbarHide
    }
}