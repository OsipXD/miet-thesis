package ru.endlesscode.miet.orioks.presentation.common.fragment

import android.os.Bundle
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.MvpAppCompatFragment


abstract class BaseFragment : MvpAppCompatFragment() {

    @get:LayoutRes
    protected abstract val layoutId: Int

    @get:IdRes
    protected open val containerId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutId, container, false)

    open fun onBackPressed(): Boolean {
        return onBackPressedInChild() || onBackPressedHere()
    }

    open fun onBackPressedInChild(): Boolean {
        if (containerId != 0) {
            val fragment = childFragmentManager.findFragmentById(containerId) as? BaseFragment
            if (fragment?.onBackPressed() == true) return true
        }

        return false
    }

    open fun onBackPressedHere(): Boolean = false

    fun snackbar(
        view: View,
        @StringRes
        messageId: Int,
        duration: Int = Snackbar.LENGTH_LONG,
        @StringRes
        actionTextId: Int = 0,
        action: () -> Unit = {}
    ) {
        Snackbar.make(view, messageId, duration)
            .apply {
                if (actionTextId != 0) {
                    setAction(actionTextId) { action.invoke() }
                }
            }
            .show()
    }
}
