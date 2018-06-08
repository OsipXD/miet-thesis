package ru.endlesscode.miet.orioks.presentation.common.activity

import android.content.Intent
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.MvpAppCompatActivity
import ru.endlesscode.miet.orioks.R
import ru.endlesscode.miet.orioks.presentation.common.fragment.BaseFragment
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.NavigatorHolder
import javax.inject.Inject


abstract class BaseActivity : MvpAppCompatActivity() {

    val contentView: ViewGroup get() = this.findViewById(android.R.id.content)

    @Inject
    internal lateinit var navigatorHolder: NavigatorHolder

    @get:LayoutRes
    protected open val layoutId: Int = R.layout.activity_fragment_container

    @Suppress("LeakingThis")
    @get:IdRes
    protected open val containerId: Int = if (layoutId == R.layout.activity_fragment_container) R.id.container else 0

    protected open val navigator: Navigator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)
    }

    override fun onResume() {
        super.onResume()

        if (navigator != null) {
            navigatorHolder.setNavigator(navigator)
        }
    }

    override fun onPause() {
        if (navigator != null) {
            navigatorHolder.removeNavigator()
        }

        super.onPause()
    }

    override fun onBackPressed() {
        if (containerId != 0) {
            val fragment = supportFragmentManager.findFragmentById(containerId) as? BaseFragment
            if (fragment?.onBackPressed() == true) {
                return
            }
        }

        if (!onBackPressedHere()) {
            super.onBackPressed()
        }
    }

    open fun onBackPressedHere(): Boolean = false

    inline fun <reified Activity> intent(init: Intent.() -> Unit = {}): Intent {
        return Intent(this, Activity::class.java).apply(init)
    }

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
