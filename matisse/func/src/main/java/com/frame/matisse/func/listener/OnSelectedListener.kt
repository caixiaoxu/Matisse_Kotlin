package com.frame.matisse.listener

import android.net.Uri

/**
 * Title :
 * Author: Lsy
 * Date: 2020/12/22 3:21 PM
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
interface OnSelectedListener {
    /**
     * @param uriList the selected item [Uri] list.
     * @param pathList the selected item file path list.
     */
    fun onSelected(uriList: List<Uri>, pathList: List<String?>)
}