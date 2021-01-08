package com.frame.matisse

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import com.frame.matisse.ui.MatisseActivity
import java.lang.ref.WeakReference

/**
 * Title : 启动主控制
 * Author: Lsy
 * Date: 2020/12/22 9:49 AM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class Matisse(activity: Activity? = null, fragment: Fragment? = null) {

    private val mContext: WeakReference<Activity> = WeakReference(activity)
    private val mFragment: WeakReference<Fragment> = WeakReference(fragment)

    private constructor(fragment: Fragment) : this(fragment.activity, fragment)

    /**
     * MIME types the selection constrains on.
     *
     *
     * Types not included in the set will still be shown in the grid but can't be chosen.
     *
     * @param mimeTypes MIME types set user can choose from.
     * @return [SelectionCreator] to build select specifications.
     * @see MimeType
     *
     * @see SelectionCreator
     */
    fun choose(mimeTypes: Set<MimeType>): SelectionCreator = this.choose(mimeTypes, true)

    /**
     * MIME types the selection constrains on.
     *
     *
     * Types not included in the set will still be shown in the grid but can't be chosen.
     *
     * @param mimeTypes          MIME types set user can choose from.
     * @param mediaTypeExclusive Whether can choose images and videos at the same time during one single choosing
     * process. true corresponds to not being able to choose images and videos at the same
     * time, and false corresponds to being able to do this.
     * @return [SelectionCreator] to build select specifications.
     * @see MimeType
     *
     * @see SelectionCreator
     */
    fun choose(mimeTypes: Set<MimeType>, mediaTypeExclusive: Boolean): SelectionCreator =
        SelectionCreator(this, mimeTypes, mediaTypeExclusive)

    fun getActivity(): Activity? = mContext.get()

    fun getFragment(): Fragment? = mFragment.get()

    companion object {
        /**
         * activity实例化
         * Start Matisse from an Activity.
         * <p>
         * This Activity's {@link Activity#onActivityResult(int, int, Intent)} will be called when user
         * finishes selecting.
         *
         * @param activity Activity instance.
         * @return Matisse instance.
         */
        fun from(activity: Activity): Matisse = Matisse(activity)

        /**
         * fragment实例化
         * Start Matisse from a Fragment.
         * <p>
         * This Fragment's {@link Fragment#onActivityResult(int, int, Intent)} will be called when user
         * finishes selecting.
         *
         * @param fragment Fragment instance.
         * @return Matisse instance.
         */
        fun from(fragment: Fragment): Matisse = Matisse(fragment)

        /**
         * 获得用户选择的媒体Uri列表
         * Obtain user selected media' {@link Uri} list in the starting Activity or Fragment.
         *
         * @param data Intent passed by {@link Activity#onActivityResult(int, int, Intent)} or
         *             {@link Fragment#onActivityResult(int, int, Intent)}.
         * @return User selected media' {@link Uri} list.
         */
        fun obtainResult(data: Intent): List<Uri>? =
            data.getParcelableArrayListExtra(MatisseActivity.EXTRA_RESULT_SELECTION)

        /**
         * 获得用户选择的媒体路径列表
         * Obtain user selected media path list in the starting Activity or Fragment.
         *
         * @param data Intent passed by {@link Activity#onActivityResult(int, int, Intent)} or
         *             {@link Fragment#onActivityResult(int, int, Intent)}.
         * @return User selected media path list.
         */
        fun obtainPathResult(data: Intent): List<String?>? =
            data.getStringArrayListExtra(MatisseActivity.EXTRA_RESULT_SELECTION_PATH)

        /**
         * 获取用户是否决定使用原始选定媒体的状态
         * Obtain state whether user decide to use selected media in original
         *
         * @param data Intent passed by {@link Activity#onActivityResult(int, int, Intent)} or
         *             {@link Fragment#onActivityResult(int, int, Intent)}.
         * @return Whether use original photo
         */
        fun obtainOriginalState(data: Intent): Boolean =
            data.getBooleanExtra(MatisseActivity.EXTRA_RESULT_ORIGINAL_ENABLE, false)
    }
}