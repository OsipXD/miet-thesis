package ru.endlesscode.miet.orioks.presentation.auth.view

import android.support.annotation.StringRes
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType


@StateStrategyType(SkipStrategy::class)
interface AuthView : MvpView {

    fun showLoginError(
            @StringRes
            messageId: Int
    )

    fun showPasswordError(
            @StringRes
            messageId: Int
    )

    @StateStrategyType(SingleStateStrategy::class)
    fun toggleLoginButton(enabled: Boolean)
}
