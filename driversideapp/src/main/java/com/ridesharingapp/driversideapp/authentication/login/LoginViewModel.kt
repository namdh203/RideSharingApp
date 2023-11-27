package com.ridesharingapp.driversideapp.authentication.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ridesharingapp.common.ServiceResult
import com.ridesharingapp.common.services.LogInResult
import com.ridesharingapp.common.uicommon.ToastMessages
import com.ridesharingapp.common.usecase.LogInUser
import com.ridesharingapp.driversideapp.navigation.DriverHomeKey
import com.ridesharingapp.driversideapp.navigation.SignUpKey
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.History
import com.zhuinden.simplestack.ScopedServices
import com.zhuinden.simplestack.StateChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class LoginViewModel(
    private val backstack: Backstack,
    private val login: LogInUser
) : ScopedServices.Activated, CoroutineScope {
    internal var toastHandler: ((ToastMessages) -> Unit)? = null

    var email by mutableStateOf("")
        private set

    fun updateEmail(input: String) {
        email = input
    }

    var password by mutableStateOf("")
        private set

    fun updatePassword(input: String) {
        password = input
    }

    fun handleLogin() = launch(Dispatchers.Main) {
        val loginAttempt = login.login(email, password)
        when (loginAttempt) {
            is ServiceResult.Failure -> toastHandler?.invoke(ToastMessages.SERVICE_ERROR)
            is ServiceResult.Value -> {
                val result = loginAttempt.value
                when (result) {
                    is LogInResult.Success -> sendToDashboard()
                    LogInResult.InvalidCredentials -> toastHandler?.invoke(ToastMessages.INVALID_CREDENTIALS)
                }
            }
        }
    }

    private fun sendToDashboard() {
        backstack.setHistory(
            History.of(DriverHomeKey()),
                //Direction of navigation which is used for animation
            StateChange.FORWARD
        )
    }

    fun goToSignup() {
        backstack.goTo(SignUpKey())
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