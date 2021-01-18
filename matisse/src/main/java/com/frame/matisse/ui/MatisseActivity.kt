package com.frame.matisse.ui

import android.content.Intent
import android.database.Cursor
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.frame.matisse.R
import com.frame.matisse.func.internal.entity.Album
import com.frame.matisse.func.internal.entity.Item
import com.frame.matisse.func.internal.entity.SelectionSpec
import com.frame.matisse.func.internal.model.AlbumCollection
import com.frame.matisse.func.internal.model.SelectedItemCollection
import com.frame.matisse.func.internal.ui.IncapableDialog
import com.frame.matisse.func.internal.utils.*
import com.frame.matisse.internal.ui.adapter.AlbumMediaAdapter
import com.frame.matisse.internal.ui.adapter.AlbumsAdapter
import com.frame.matisse.internal.ui.widget.AlbumsSpinner
import com.frame.matisse.internal.ui.widget.CheckRadioView
import com.frame.matisse.internal.utils.*
import java.util.*

/**
 * Title : 主Activity
 * Author: Lsy
 * Date: 2020/12/22 10:05 AM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
open class MatisseActivity : AppCompatActivity(), View.OnClickListener,
    AdapterView.OnItemSelectedListener, AlbumCollection.AlbumCallbacks,
    MediaSelectionFragment.SelectionProvider, AlbumMediaAdapter.CheckStateListener,
    AlbumMediaAdapter.OnMediaClickListener, AlbumMediaAdapter.OnPhotoCapture {

    companion object {
        //选择的图片Uri
        const val EXTRA_RESULT_SELECTION = "extra_result_selection"

        //选择的图片路径
        const val EXTRA_RESULT_SELECTION_PATH = "extra_result_selection_path"

        //原图
        const val EXTRA_RESULT_ORIGINAL_ENABLE = "extra_result_original_enable"

        //预览请求码
        private const val REQUEST_CODE_PREVIEW = 23

        //拍照请求码
        private const val REQUEST_CODE_CAPTURE = 24

        //原图按钮保存状态
        const val CHECK_STATE = "checkState"
    }

    //相册专辑集合
    private val mAlbumCollection: AlbumCollection = AlbumCollection()

    //媒体存储（拍照文件保存）
    private var mMediaStoreCompat: MediaStoreCompat? = null

    //相册内容图片集合
    private val mSelectedCollection: SelectedItemCollection = SelectedItemCollection(this)

    //相册专辑弹窗
    private lateinit var mAlbumsSpinner: AlbumsSpinner

    //相册专辑适配器
    private lateinit var mAlbumsAdapter: AlbumsAdapter

    private var toolbar: Toolbar? = null

    //预览按钮
    private var mButtonPreview: TextView? = null

    //应用按钮
    private var mButtonApply: TextView? = null

    //对应相册专辑替换内容
    private var mContainer: View? = null

    //空界面
    private var mEmptyView: View? = null

    //原图按钮
    private var mOriginalLayout: LinearLayout? = null
    private var mOriginal: CheckRadioView? = null
    private var mOriginalEnable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // 设置样式在super.onCreate()前
        setTheme(SelectionSpec.themeId)
        super.onCreate(savedInstanceState)
        //保证选择规范数据已重置
        if (!SelectionSpec.hasInited) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }
        setContentView(R.layout.activity_matisse)
        //调整方向
        if (SelectionSpec.needOrientationRestriction()) {
            requestedOrientation = SelectionSpec.orientation
        }
        //是否需要拍照
        if (SelectionSpec.capture) {
            if (SelectionSpec.captureStrategy == null) throw RuntimeException("Don't forget to set CaptureStrategy.")
            mMediaStoreCompat =
                MediaStoreCompat(this, mCaptureStrategy = SelectionSpec.captureStrategy!!)
        }
        //初始化标题栏
        initToolbar()

        mContainer = findViewById(R.id.container)
        mEmptyView = findViewById(R.id.empty_view)

        //初始化底部工具栏
        initBottomToolbar(savedInstanceState)

        mAlbumsAdapter = AlbumsAdapter(this, null, false)
        mAlbumsSpinner = AlbumsSpinner(this)
        mAlbumsSpinner.setOnItemSelectedListener(this)
        mAlbumsSpinner.setSelectedTextView((findViewById<View>(R.id.selected_album) as TextView))
        mAlbumsSpinner.setPopupAnchorView(findViewById(R.id.toolbar))
        mAlbumsSpinner.setAdapter(mAlbumsAdapter)

        //相册子内容集合
        mSelectedCollection.onCreate(savedInstanceState)
        //相册目录集合
        mAlbumCollection.onCreate(this, this)
        mAlbumCollection.onRestoreInstanceState(savedInstanceState)
        mAlbumCollection.loadAlbums()
    }

    /**
     * 初始化标题栏
     */
    private fun initToolbar() {
        toolbar = findViewById(R.id.toolbar)
        //标题栏
        setSupportActionBar(toolbar)
        //不显示标题
        supportActionBar?.setDisplayShowTitleEnabled(false)
        //显示返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //修改图标颜色
        val navigationIcon = toolbar?.navigationIcon
        val ta = theme.obtainStyledAttributes(intArrayOf(R.attr.album_element_color))
        val color = ta.getColor(0, 0)
        ta.recycle()
        if (Platform.beforeAndroidTen()) {
            navigationIcon?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        } else {
            navigationIcon?.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_IN)
        }
    }

    /**
     * 底部工具栏
     */
    private fun initBottomToolbar(savedInstanceState: Bundle?) {
        //预览按钮
        mButtonPreview = findViewById<View>(preBtnId()) as TextView
        mButtonPreview?.setOnClickListener(this)
        //确定按钮
        mButtonApply = findViewById<View>(applyBtnId()) as TextView
        mButtonApply?.setOnClickListener(this)
        //原图按钮
        mOriginalLayout = findViewById(R.id.originalLayout)
        mOriginal = findViewById(R.id.original)
        mOriginalLayout?.setOnClickListener(this)
        //原图按钮状态还原
        if (savedInstanceState != null) {
            mOriginalEnable = savedInstanceState.getBoolean(CHECK_STATE)
        }

        updateBottomToolbar()
    }

    /**
     * 预览按钮Id
     */
    protected fun preBtnId(): Int = R.id.button_preview

    /**
     * 应用按钮Id
     */
    protected fun applyBtnId(): Int = R.id.button_apply


    /**
     * 更新底部状态栏
     */
    private fun updateBottomToolbar() {
        val selectedCount = mSelectedCollection.count()
        //按钮显示
        if (selectedCount == 0) {
            mButtonPreview?.isEnabled = false
            mButtonApply?.isEnabled = false
            mButtonApply?.text = getString(R.string.button_apply_default)
        } else if (selectedCount == 1 && SelectionSpec.singleSelectionModeEnabled()) {
            mButtonPreview?.isEnabled = true
            mButtonApply?.isEnabled = true
            mButtonApply?.setText(R.string.button_apply_default)
        } else {
            mButtonPreview?.isEnabled = true
            mButtonApply?.isEnabled = true
            mButtonApply?.text = getString(R.string.button_apply, selectedCount)
        }
        //是否显示原图
        if (SelectionSpec.originalable) {
            mOriginalLayout?.visibility = View.VISIBLE
            updateOriginalState()
        } else {
            mOriginalLayout?.visibility = View.INVISIBLE
        }
    }

    /**
     * 更新原图按钮状态
     */
    private fun updateOriginalState() {
        mOriginal?.setChecked(mOriginalEnable)
        if (countOverMaxSize() > 0) {
            if (mOriginalEnable) {
                val incapableDialog: IncapableDialog = IncapableDialog.newInstance(
                    "", getString(R.string.error_over_original_size, SelectionSpec.originalMaxSize)
                )
                incapableDialog.show(supportFragmentManager, IncapableDialog::class.java.name)
                mOriginal?.setChecked(false)
                mOriginalEnable = false
            }
        }
    }

    /**
     * 是否有超过最大大小
     */
    private fun countOverMaxSize(): Int {
        var count = 0
        for (item in mSelectedCollection.asList()) {
            if (item.isImage()) {
                val size: Float = PhotoMetadataUtils.getSizeInMB(item.size)
                if (size > SelectionSpec.originalMaxSize) {
                    count++
                }
            }
        }
        return count
    }

    /**
     * 存储状态数据
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mSelectedCollection.onSaveInstanceState(outState)
        mAlbumCollection.onSaveInstanceState(outState)
        outState.putBoolean("checkState", mOriginalEnable)
    }

    override fun onDestroy() {
        SelectionSpec.imageEngine?.clear(this)
        super.onDestroy()
        mAlbumCollection.onDestroy()
        SelectionSpec.onCheckedListener = null
        SelectionSpec.onSelectedListener = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //返回事件
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 返回方法
     */
    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        if (requestCode == REQUEST_CODE_PREVIEW) {
            //预览回调
            data?.let {
                val resultBundle = data.getBundleExtra(BasePreviewActivity.EXTRA_RESULT_BUNDLE)
                //选择的列表返回
                val selected =
                    resultBundle?.getParcelableArrayList<Item>(SelectedItemCollection.STATE_SELECTION)
                //是否原图
                mOriginalEnable =
                    data.getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, false)
                //选择的类型(图片还是视频)
                val collectionType = resultBundle?.getInt(
                    SelectedItemCollection.STATE_COLLECTION_TYPE,
                    SelectedItemCollection.COLLECTION_UNDEFINED
                )

                if (null != resultBundle && null != selected && null != collectionType) {
                    if (data.getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_APPLY, false)) {
                        val result = Intent()
                        val selectedUris = ArrayList<Uri>()
                        val selectedPaths = ArrayList<String?>()
                        for (item in selected) {
                            selectedUris.add(item.getContentUri())
                            selectedPaths.add(PathUtils.getPathFromUri(this, item.getContentUri()))
                        }
                        result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, selectedUris)
                        result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, selectedPaths)
                        result.putExtra(EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable)
                        setResult(RESULT_OK, result)
                        finish()
                    } else {
                        //重置选择
                        mSelectedCollection.overwrite(selected, collectionType)
                        //获取子fragment
                        val mediaSelectionFragment = supportFragmentManager.findFragmentByTag(
                            MediaSelectionFragment::class.java.simpleName
                        )
                        //刷新
                        if (mediaSelectionFragment is MediaSelectionFragment) {
                            mediaSelectionFragment.refreshMediaGrid()
                        }
                        //刷新底部工具栏
                        updateBottomToolbar()
                    }
                }
            }
        } else if (requestCode == REQUEST_CODE_CAPTURE) {
            //拍照数据回调
            mMediaStoreCompat?.let {
                val contentUri = it.getCurrentPhotoUri()
                val path = it.getCurrentPhotoPath()
                val selected = arrayListOf(contentUri)
                val selectedPath = arrayListOf(path)
                val result = Intent()
                result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, selected)
                result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, selectedPath)
                setResult(RESULT_OK, result)
                //刷新媒体库
                SingleMediaScanner(this.applicationContext, path ?: "") {
                    Log.i("SingleMediaScanner", "scan finish!")
                }
                finish()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            mButtonPreview -> {
                //预览
                val intent = Intent(this, SelectedPreviewActivity::class.java)
                //传递已选择的数据和类型
                intent.putExtra(
                    BasePreviewActivity.EXTRA_DEFAULT_BUNDLE,
                    mSelectedCollection.getDataWithBundle()
                )
                //传递是否原图
                intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable)
                startActivityForResult(intent, REQUEST_CODE_PREVIEW)
            }
            mButtonApply -> {
                //确定
                val result = Intent()
                val selectedUris = mSelectedCollection.asListOfUri() as ArrayList<Uri>
                result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, selectedUris)
                val selectedPaths = mSelectedCollection.asListOfString() as ArrayList<String?>
                result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, selectedPaths)
                result.putExtra(EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable)
                setResult(RESULT_OK, result)
                finish()
            }
            mOriginalLayout -> {
                //原图
                val count = countOverMaxSize()
                if (count > 0) {
                    val incapableDialog = IncapableDialog.newInstance(
                        "",
                        getString(
                            R.string.error_over_original_count, count, SelectionSpec.originalMaxSize
                        )
                    )
                    incapableDialog.show(supportFragmentManager, IncapableDialog::class.java.name)
                    return
                }
                mOriginalEnable = !mOriginalEnable
                mOriginal?.setChecked(mOriginalEnable)
                SelectionSpec.onCheckedListener?.onCheck(mOriginalEnable)
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        //设置当前位置
        mAlbumCollection.mCurrentSelection = position
        //移动光标到当前位置
        mAlbumsAdapter.cursor.moveToPosition(position)
        //取出当前位置的值
        val album: Album = Album.valueOf(mAlbumsAdapter.cursor)
        if (album.isAll() && SelectionSpec.capture) {
            //如果相册专辑中没有图片，但是显示拍照
            album.addCaptureCount()
        }
        onAlbumSelected(album)
    }

    /**
     * 相册专辑切换
     */
    private fun onAlbumSelected(album: Album) {
        if (album.isAll() && album.isEmpty()) {
            //子内容列表数据为空
            mContainer?.visibility = View.GONE
            mEmptyView?.visibility = View.VISIBLE
        } else {
            //不为空
            mContainer?.visibility = View.VISIBLE
            mEmptyView?.visibility = View.GONE
            //加载子内容
            val fragment: Fragment = MediaSelectionFragment.newInstance(album)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, fragment, MediaSelectionFragment::class.java.simpleName)
                .commitAllowingStateLoss()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onAlbumLoad(cursor: Cursor?) {
        mAlbumsAdapter.swapCursor(cursor)
        // select default album.
        cursor?.let {
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                cursor.moveToPosition(mAlbumCollection.mCurrentSelection)
                mAlbumsSpinner.setSelection(
                    this@MatisseActivity,
                    mAlbumCollection.mCurrentSelection
                )
                val album = Album.valueOf(cursor)
                if (album.isAll() && SelectionSpec.capture) {
                    album.addCaptureCount()
                }
                onAlbumSelected(album)
            }
        }
    }

    override fun onAlbumReset() {
        mAlbumsAdapter.swapCursor(null)
    }

    /**
     * 获取选择的相册专辑内容媒体集合
     */
    override fun provideSelectedItemCollection(): SelectedItemCollection {
        return mSelectedCollection
    }

    override fun onUpdate() {
        // notify bottom toolbar that check state changed.
        updateBottomToolbar()
        SelectionSpec.onSelectedListener?.onSelected(
            mSelectedCollection.asListOfUri(), mSelectedCollection.asListOfString()
        )
    }

    override fun onMediaClick(album: Album?, item: Item?, adapterPosition: Int) {
        //媒体文件点击，跳转到预览界面
        val intent = Intent(this, AlbumPreviewActivity::class.java)
        intent.putExtra(AlbumPreviewActivity.EXTRA_ALBUM, album)
        intent.putExtra(AlbumPreviewActivity.EXTRA_ITEM, item)
        intent.putExtra(
            BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, mSelectedCollection.getDataWithBundle()
        )
        intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable)
        startActivityForResult(intent, REQUEST_CODE_PREVIEW)
    }

    override fun capture() {
        //拍照
        mMediaStoreCompat?.dispatchCaptureIntent(this, REQUEST_CODE_CAPTURE)
    }
}