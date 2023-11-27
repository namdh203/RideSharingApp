package com.ridesharingapp.driversideapp.authentication.signup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ridesharingapp.common.ServiceResult
import com.ridesharingapp.common.services.SignUpResult
import com.ridesharingapp.common.uicommon.ToastMessages
import com.ridesharingapp.common.usecase.SignUpUser
import com.ridesharingapp.driversideapp.navigation.SplashKey
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.History
import com.zhuinden.simplestack.ScopedServices
import com.zhuinden.simplestack.StateChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
class SignUpViewModel(
    private val backstack: Backstack,
    private val signUp: SignUpUser,
) : ScopedServices.Activated, CoroutineScope {
    internal var toastHandler: ((ToastMessages) -> Unit)? = null

    var email by mutableStateOf("")
        private set

    fun updateEmail(input: String) {
        email = input
    }

    var name by mutableStateOf("")
        private set

    fun updateName(input: String) {
        name = input
    }

    var password by mutableStateOf("")
        private set

    fun updatePassword(input: String) {
        password = input
    }


    fun handleSignUp() = launch(Dispatchers.Main) {
        val signupAttempt = signUp.signUpUser(email, password, name)
        when (signupAttempt) {
            is ServiceResult.Failure -> toastHandler?.invoke(ToastMessages.SERVICE_ERROR)
            is ServiceResult.Value -> {
                when (signupAttempt.value) {
                    is SignUpResult.Success -> {
                        backstack.setHistory(
                            History.of(SplashKey()),
                            //Direction of navigation which is used for animation
                            StateChange.REPLACE
                        )
                    }
                    SignUpResult.InvalidCredentials -> toastHandler?.invoke(ToastMessages.INVALID_CREDENTIALS)
                    SignUpResult.AlreadySignedUp -> toastHandler?.invoke(ToastMessages.ACCOUNT_EXISTS)
                }
            }
        }
    }

    fun handleBackPress() {
        backstack.goBack()
    }
    override fun onServiceActive() = Unit

    override fun onServiceInactive() {
        canceller.cancel()
        toastHandler = null
    }

    private val canceller = Job()

    override val coroutineContext: CoroutineContext
        get() = canceller + Dispatchers.Main
}