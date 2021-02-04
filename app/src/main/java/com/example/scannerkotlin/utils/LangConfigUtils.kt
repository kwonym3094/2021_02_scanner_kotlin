package com.example.scannerkotlin.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.view.ViewGroup
import android.widget.TextView
import java.util.*

/**
 * Created by ymKwon on 2021-02-04 오후 4:08.
 */
object LangConfigUtils {
    /**
     * @author kwonym
     * @version 1.0.0
     * @since 2021-01-11 오후 2:20
     *
     * 언어 설정 메소드
     * - 언어 변경 전 만들어진 액티비티는 변경된 언어가 바로 적용되지 않음 -> 액티비티를 다시 시작해야함
     * - 액티비티를 다시 시작하거나 다시 그리는 경우는 개발자에게 (성능 혹은 다양한 변수 때문에) 부담이 됨
     * - 해당 문제를 해결하기 위해 액티비티를 재시작 하지 않고 액티비티 안의 view를 찾아 바뀐 언어로 이름을 바꿔주는 메소드 정의
     * - 레이아웃 xml파일은 반드시 다음 형식으로 작성
     *
     * ```````````
     * android:text="@string/text1" << 이런형태로 존재하는 태그를 찾아서,
     * android:tag="text1" << values 내부에 있는 string name 값을 값만 넣는 방식.
     * ```````````
     *
     * - 사용방법
     * setLocale(~activity.this, "ko")
     * setLocale(~activity.this, "en") // 1st variable: 해당 액티비티, 2nd var: 언어(ex. "ko", "en")
     * !! 변경전에 만들어진 액티비티만 적용하면 됨 (이후는 자동으로 바뀐 언어로 적용됨) !!
     *
     * 출처: https://www.androidpub.com/2556461
     */
    @Throws(Exception::class)
    fun setRefreshViewGroup(context: Context, root: ViewGroup) {
        for (i in 0 until root.childCount) {
            val child = root.getChildAt(i)
            if (child is TextView) {
                if (child.getTag() != null) {
                    if (child.text != null && child.text.toString().length > 0) {
                        val stringId = getResourceId(context, child.getTag())
                        child.setText(stringId)
                        // Log.i(TAG, "getText:" + ((TextView)
                        // child).getText());
                    }
                    if (child.hint != null && child.hint.toString().length > 0) {
                        val hintId = getResourceId(context, child.getTag())
                        child.setHint(hintId)

                        // Log.i(TAG, "getHint:" + ((TextView)
                        // child).getHint());
                    }
                }
            } else if (child is ViewGroup) setRefreshViewGroup(context, child)
        }
    }

    fun getResourceId(context: Context, tag: Any?): Int {
        return context.resources.getIdentifier(tag as String?, "string", context.packageName)
    }

    fun setLocale(activity: Activity, character: String?) {
        val locale = Locale(character)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
        val vg = activity.window.decorView.rootView as ViewGroup
        try {
            setRefreshViewGroup(activity, vg)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}