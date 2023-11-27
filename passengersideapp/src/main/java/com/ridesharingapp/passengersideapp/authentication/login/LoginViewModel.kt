package com.ridesharingapp.passengersideapp.authentication.login

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ridesharingapp.common.ServiceResult
import com.ridesharingapp.passengersideapp.navigation.PassengerDashboardKey
import com.ridesharingapp.passengersideapp.navigation.SignUpKey
import com.ridesharingapp.common.services.FirebaseAuthService
import com.ridesharingapp.common.services.LogInResult
import com.ridesharingapp.common.uicommon.ToastMessages
import com.ridesharingapp.common.usecases.LogInUser
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.History
import com.zhuinden.simplestack.ScopedServices
import com.zhuinden.simplestack.StateChange
import io.getstream.chat.android.client.ChatClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class LoginViewModel(
    private val backstack: Backstack,
    private val login: LogInUser,
    private val authService: FirebaseAuthService,
    private val client: ChatClient
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

    var clearingPrevLogin by mutableStateOf(true)
        private set

    var loginInProcess by mutableStateOf(false)
        private set

    fun handleLogin() = launch(Dispatchers.Main) {
        loginInProcess = true
        val loginAttempt = login.login(email, password)
        loginInProcess = false
        when (loginAttempt) {
            is ServiceResult.Failure -> {
                Log.w("LoginViewModel", "login failed", loginAttempt.exception)
                toastHandler?.invoke(ToastMessages.SERVICE_ERROR)
            }
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
            History.of(PassengerDashboardKey()),
            //Direction of navigation which is used for animation
            StateChange.FORWARD
        )
    }

    fun goToSignup() {
        backstack.goTo(SignUpKey())
    }

    // Logout the current user (if exists)
    override fun onServiceActive() {
        Log.d("LoginViewModel", "onServiceActive")
        clearingPrevLogin = true
        val fireAuthUser = authService.auth.currentUser
        if (fireAuthUser != null) {
            Log.d("LoginViewModel", "onServiceActive:firebaseUser ${fireAuthUser.email}")
            authService.logout()
            val user = client.getCurrentUser()
            if (user != null) {
                Log.d("LoginViewModel", "onServiceActive:chatClientUser ${user.name}")
                client.disconnect(flushPersistence = true).enqueue {
                    clearingPrevLogin = false
                    if (it.isError) {
                        Log.w(
                            "LoginViewModel",
                            it.error().message ?: "Error logging out",
                            it.error().cause
                        )
                    } else {
                        Log.d("LoginViewModel", "clearing old user:disconnect chat client:success")
                    }
                }
            } else {
                clearingPrevLogin = false
            }
        } else {
            clearingPrevLogin = false
        }
    }

    override fun onServiceInactive() {
        canceller.cancel()
        toastHandler = null
    }

    private val canceller = Job()

    override val coroutineContext: CoroutineContext
        get() = canceller + Dispatchers.Main
}