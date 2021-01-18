package com.frame.matisse.func.internal.ui

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.frame.matisse.func.R

/**
 * Title :
 * Author: Lsy
 * Date: 2020/12/22 2:03 PM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class IncapableDialog : DialogFragment() {
    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"

        fun newInstance(title: String?, message: String?): IncapableDialog {
            val dialog = IncapableDialog()
            val args = Bundle()
            args.putString(EXTRA_TITLE, title)
            args.putString(EXTRA_MESSAGE, message)
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments!!.getString(EXTRA_TITLE)
        val message = arguments!!.getString(EXTRA_MESSAGE)
        val builder = AlertDialog.Builder(
            activity!!
        )
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title)
        }
        if (!TextUtils.isEmpty(message)) {
            builder.setMessage(message)
        }
        builder.setPositiveButton(R.string.button_ok) { dialog, _ -> dialog.dismiss() }
        return builder.create()
    }
}