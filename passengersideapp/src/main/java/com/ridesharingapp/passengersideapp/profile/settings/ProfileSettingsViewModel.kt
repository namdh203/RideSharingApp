package com.ridesharingapp.passengersideapp.profile.settings

import android.net.Uri
import com.ridesharingapp.common.ServiceResult
import com.ridesharingapp.common.domain.GrabLamUser
import com.ridesharingapp.common.domain.UserType
import com.ridesharingapp.common.services.UserService
import com.ridesharingapp.common.uicommon.ToastMessages
import com.ridesharingapp.common.usecases.GetUser
import com.ridesharingapp.common.usecases.LogOutUser
import com.ridesharingapp.common.usecases.UpdateUserAvatar
import com.ridesharingapp.passengersideapp.navigation.LoginKey
import com.ridesharingapp.passengersideapp.navigation.PassengerDashboardKey
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.History
import com.zhuinden.simplestack.ScopedServices
import com.zhuinden.simplestack.StateChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ProfileSettingsViewModel(
    private val backstack: Backstack,
    private val updateUserAvatar: UpdateUserAvatar,
    private val logUserOut: LogOutUser,
    private val userService: UserService,
    private val getUser: GetUser
) : ScopedServices.Activated, CoroutineScope {
    internal var toastHandler: ((ToastMessages) -> Unit)? = null

    private val _userModel = MutableStateFlow<GrabLamUser?>(null)
    val userModel: StateFlow<GrabLamUser?> get() = _userModel
    fun handleLogOut() = launch(Dispatchers.Main) {
        logUserOut.logout()
        sendToLogin()
    }

    fun isUserRegistered(): Boolean {
        return false
    }

    fun getUser() = launch(Dispatchers.Main) {
        val getUser = getUser.getUser()
        when (getUser) {
            is ServiceResult.Failure -> {
                toastHandler?.invoke(ToastMessages.GENERIC_ERROR)
                sendToLogin()
            }
            is ServiceResult.Value -> {
                if (getUser.value == null) sendToLogin()
                else _userModel.value = getUser.value
            }
        }
    }

    private fun sendToLogin() {
        backstack.setHistory(
            History.of(LoginKey()),
            StateChange.REPLACE
        )
    }

    override fun onServiceActive() {
        getUser()
    }

    override fun onServiceInactive() {
        canceller.cancel()
        toastHandler = null
    }

    fun handleThumbnailUpdate(imageUri: Uri?) = launch(Dispatchers.Main) {
        if (imageUri != null) {
            val updateAttempt =
                updateUserAvatar.updateAvatar(_userModel.value!!, imageUri.toString())

            when (updateAttempt) {
                is ServiceResult.Failure -> toastHandler?.invoke(ToastMessages.SERVICE_ERROR)

                is ServiceResult.Value -> {
                    _userModel.value = _userModel.value!!.copy(
                        avatarPhotoUrl = updateAttempt.value
                    )
                    toastHandler?.invoke(ToastMessages.UPDATE_SUCCESSFUL)
                }
            }
        } else {
            toastHandler?.invoke(ToastMessages.GENERIC_ERROR)
        }
    }

    private suspend fun updateUser(user: GrabLamUser) {
        val updateAttempt = userService.updateUser(user)

        when (updateAttempt) {
            is ServiceResult.Failure -> toastHandler?.invoke(ToastMessages.SERVICE_ERROR)
            is ServiceResult.Value -> {
                if (updateAttempt.value == null) sendToLogin()
                else _userModel.value = updateAttempt.value
            }
        }
    }

    fun handleToggleUserType() = launch(Dispatchers.Main) {
        val oldModel = _userModel.value!!
        val newType = flipType(oldModel.type)

        updateUser(oldModel.copy(type = newType))
    }

    private fun flipType(oldType: String): String {
        return if (oldType == UserType.PASSENGER.value) UserType.DRIVER.value
        else UserType.PASSENGER.value
    }

    fun handleBackPress() {
        backstack.setHistory(
            History.of(PassengerDashboardKey()),
            //Direction of navigation which is used for animation
            StateChange.BACKWARD
        )
    }

    private val canceller = Job()

    override val coroutineContext: CoroutineContext
        get() = canceller + Dispatchers.Main
}