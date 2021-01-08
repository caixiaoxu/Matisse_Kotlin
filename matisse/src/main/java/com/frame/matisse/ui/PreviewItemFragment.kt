package com.frame.matisse.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.lsy.matisse.R
import com.frame.matisse.internal.entity.Item
import com.frame.matisse.internal.entity.SelectionSpec
import com.frame.matisse.internal.utils.PhotoMetadataUtils
import com.frame.matisse.listener.OnFragmentInteractionListener
import it.sephiroth.android.library.imagezoom.ImageViewTouch
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase

/**
 * Title : 预览的item fragment界面
 * Author: Lsy
 * Date: 2020/12/23 2:18 PM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class PreviewItemFragment : Fragment() {
    companion object {
        private const val ARGS_ITEM = "args_item"

        fun newInstance(item: Item?): PreviewItemFragment {
            val fragment = PreviewItemFragment()
            val bundle = Bundle()
            bundle.putParcelable(ARGS_ITEM, item)
            fragment.arguments = bundle
            return fragment
        }
    }

    private var mListener: OnFragmentInteractionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnFragmentInteractionListener) {
            context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_preview_item, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val item: Item = arguments?.getParcelable(ARGS_ITEM) ?: return
        //视频按钮
        val videoPlayButton = view.findViewById<View>(R.id.video_play_button)
        if (item.isVideo()) {
            videoPlayButton.visibility = View.VISIBLE
            videoPlayButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(item.uri, "video/*")
                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, R.string.error_no_video_activity, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } else {
            videoPlayButton.visibility = View.GONE
        }
        //图片缩放
        val image: ImageViewTouch = view.findViewById<View>(R.id.image_view) as ImageViewTouch
        image.displayType = ImageViewTouchBase.DisplayType.FIT_TO_SCREEN
        image.setSingleTapListener { mListener?.onClick() }

        val size: Point = PhotoMetadataUtils.getBitmapSize(item.getContentUri(), activity!!)
        if (item.isGif()) {
            SelectionSpec.imageEngine?.loadGifImage(
                context!!, size.x, size.y, image, item.getContentUri()
            )
        } else {
            SelectionSpec.imageEngine?.loadImage(
                context!!, size.x, size.y, image, item.getContentUri()
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    fun resetView() {
        (view?.findViewById<View>(R.id.image_view) as ImageViewTouch).resetMatrix()
    }
}