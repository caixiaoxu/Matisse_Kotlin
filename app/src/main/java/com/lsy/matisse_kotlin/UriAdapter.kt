package com.lsy.matisse_kotlin

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Title :
 * Author: Lsy
 * Date: 12/24/20 5:13 PM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class UriAdapter : RecyclerView.Adapter<UriAdapter.UriViewHolder>() {
    private var mUris: List<Uri>? = null
    private var mPaths: List<String?>? = null
    fun setData(uris: List<Uri>?, paths: List<String?>?) {
        mUris = uris
        mPaths = paths
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UriViewHolder {
        return UriViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.uri_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: UriViewHolder, position: Int) {
        holder.mUri.text = mUris!![position].toString()
        holder.mPath.text = mPaths!![position]
        holder.mUri.alpha = if (position % 2 == 0) 1.0f else 0.54f
        holder.mPath.alpha = if (position % 2 == 0) 1.0f else 0.54f
    }

    override fun getItemCount(): Int {
        return if (mUris == null) 0 else mUris!!.size
    }

    class UriViewHolder(contentView: View) : RecyclerView.ViewHolder(contentView) {
        val mUri: TextView = contentView.findViewById<View>(R.id.uri) as TextView
        val mPath: TextView = contentView.findViewById<View>(R.id.path) as TextView
    }
}