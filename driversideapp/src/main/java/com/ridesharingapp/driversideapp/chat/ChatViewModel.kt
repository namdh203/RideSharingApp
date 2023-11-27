package com.ridesharingapp.driversideapp.chat

import com.ridesharingapp.common.ServiceResult
import com.ridesharingapp.common.uicommon.ToastMessages
import com.ridesharingapp.common.usecase.GetUser
import com.ridesharingapp.driversideapp.navigation.DriverHomeKey
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


class ChatViewModel(
    private val backstack: Backstack,
    private val getUser: GetUser
) : ScopedServices.Activated, CoroutineScope {
    internal var toastHandler: ((ToastMessages) -> Unit)? = null

    fun handleBackButton() = launch(Dispatchers.Main) {
        val user = getUser.getUser()

        if (user is ServiceResult.Value && user.value != null) {
            sendToDashboard()
        } else {
            toastHandler?.invoke(ToastMessages.GENERIC_ERROR)
            backstack.setHistory(
                History.of(SplashKey()),
                StateChange.FORWARD
            )
        }
    }

    private fun sendToDashboard() {
        backstack.setHistory(
            History.of(DriverHomeKey()),
            //Direction of navigation which is used for animation
            StateChange.FORWARD
        )
    }

    private val canceller = Job()

    override val coroutineContext: CoroutineContext
        get() = canceller + Dispatchers.Main

    override fun onServiceActive() {
        Unit
    }

    override fun onServiceInactive() {
        canceller.cancel()
        toastHandler = null
    }
}