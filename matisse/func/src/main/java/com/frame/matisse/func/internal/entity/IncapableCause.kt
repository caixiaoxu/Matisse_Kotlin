package com.frame.matisse.func.internal.entity

import android.content.Context
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.fragment.app.FragmentActivity
import com.frame.matisse.func.internal.ui.IncapableDialog
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Title : 异常提示方式
 * Author: Lsy
 * Date: 2020/12/22 1:52 PM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class IncapableCause {

    companion object {
        const val TOAST = 0x00
        const val DIALOG = 0x01
        const val NONE = 0x02

        fun handleCause(context: Context, cause: IncapableCause?) {
            cause?.let {
                when (cause.mForm) {
                    NONE -> {
                    }
                    DIALOG -> {
                        val incapableDialog: IncapableDialog =
                            IncapableDialog.newInstance(cause.mTitle, cause.mMessage)
                        incapableDialog.show(
                            (context as FragmentActivity).supportFragmentManager,
                            IncapableDialog::class.java.name
                        )
                    }
                    TOAST -> Toast.makeText(context, cause.mMessage, Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(context, cause.mMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(TOAST, DIALOG, NONE)
    annotation class Form

    private var mForm = TOAST
    private var mTitle: String? = null
    private var mMessage: String? = null

    constructor(@Form form: Int = TOAST, title: String = "", message: String) {
        this.mForm = form
        this.mTitle = title
        this.mMessage = message
    }

}