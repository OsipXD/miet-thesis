package ru.endlesscode.miet.orioks.presentation.auth.fragment

import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.screen_auth.*
import ru.endlesscode.miet.orioks.R
import ru.endlesscode.miet.orioks.internal.di.DI
import ru.endlesscode.miet.orioks.presentation.auth.presenter.AuthPresenter
import ru.endlesscode.miet.orioks.presentation.auth.view.AuthView
import ru.endlesscode.miet.orioks.presentation.common.fragment.BaseFragment
import ru.endlesscode.miet.orioks.util.asObservable
import javax.inject.Inject


class AuthFragment : BaseFragment(), AuthView {

    companion object {
        fun newInstance(): AuthFragment = AuthFragment()
    }

    override val layoutId = R.layout.screen_auth

    @Inject
    @InjectPresenter
    internal lateinit var presenter: AuthPresenter

    @ProvidePresenter
    fun providePresenter(): AuthPresenter = presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        DI.main.provideComponent().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerListeners()
    }

    override fun showLoginError(messageId: Int) {
        student_number_layout.error = if (messageId == 0) "" else getString(messageId)
    }

    override fun showPasswordError(messageId: Int) {
        password_layout.error = if (messageId == 0) "" else getString(messageId)
    }

    override fun toggleLoginButton(enabled: Boolean) {
        log_in_button.isEnabled = enabled
    }

    private fun registerListeners() {
        presenter.setFieldsValues(student_number_text_view.asObservable(), password_text_view.asObservable())
        log_in_button.setOnClickListener { presenter.onLogInClicked() }
    }
}
