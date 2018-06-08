package ru.endlesscode.miet.orioks.presentation.auth.presenter

import com.arellomobile.mvp.InjectViewState
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import ru.endlesscode.miet.orioks.R
import ru.endlesscode.miet.orioks.internal.Screens
import ru.endlesscode.miet.orioks.presentation.auth.view.AuthView
import ru.endlesscode.miet.orioks.presentation.common.presenter.BasePresenter
import ru.endlesscode.miet.orioks.util.Validator
import ru.endlesscode.miet.orioks.util.Validator.Companion.BLANK
import ru.endlesscode.miet.orioks.util.Validator.Companion.NO_ERROR
import ru.endlesscode.miet.orioks.util.Validator.Companion.TOO_SHORT
import ru.endlesscode.miet.orioks.util.Validator.Companion.WRONG_LENGTH
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
class AuthPresenter @Inject constructor(
        private val router: Router,
        private val validator: Validator,
        private val authUseCase: AuthUseCase
) : BasePresenter<AuthView>() {

    fun setFieldsValues(loginValues: Observable<CharSequence>, passwordValues: Observable<CharSequence>) {
        safeSubscribe {
            Observable.combineLatest(
                    loginValues.map(this::checkLogin),
                    passwordValues.map(this::checkPassword),
                    BiFunction({ loginIsValid: Boolean, passIsValid: Boolean -> loginIsValid && passIsValid })
            ).subscribe(viewState::toggleLoginButton)
        }
    }

    private fun checkLogin(login: CharSequence): Boolean {
        val status = validator.validate(login) {
            notBlank()
            lengthExact(7)
        }

        when (status) {
            BLANK -> viewState.showLoginError(R.string.error_empty_field)
            WRONG_LENGTH -> viewState.showLoginError(R.string.error_wrong_login_length)
            else -> viewState.showLoginError(0)
        }

        return status == NO_ERROR
    }

    private fun checkPassword(password: CharSequence): Boolean {
        val status = validator.validate(password) {
            notBlank()
            minimalLength(3)
        }

        when (status) {
            BLANK -> viewState.showPasswordError(R.string.error_empty_field)
            TOO_SHORT -> viewState.showPasswordError(R.string.error_password_too_short)
            else -> viewState.showPasswordError(0)
        }

        return status == NO_ERROR
    }

    fun onLogInClicked() {
        authUseCase.auth().subscribe(
                onSuccess = this::goToNextScreen,
                onError = viewState::showError;
        )
    }

    private fun goToNextScreen() {
        router.newRootScreen(Screens.MAIN_MENU)
    }
}
