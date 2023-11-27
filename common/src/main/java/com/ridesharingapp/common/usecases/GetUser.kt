package com.ridesharingapp.common.usecases

import com.ridesharingapp.common.ServiceResult
import com.ridesharingapp.common.domain.GrabLamUser
import com.ridesharingapp.common.services.AuthenticationService
import com.ridesharingapp.common.services.UserService

class GetUser(
    val authService: AuthenticationService,
    val userService: UserService
) {

    suspend fun getUser(): ServiceResult<GrabLamUser?> {
        val getSession = authService.getSession()
        return when (getSession) {
            is ServiceResult.Failure -> getSession
            is ServiceResult.Value -> {
                if (getSession.value == null) getSession
                else getUserDetails(getSession.value.userId)
            }
        }
    }

    private suspend fun getUserDetails(uid: String): ServiceResult<GrabLamUser?> {
        return userService.getUserById(uid).let { getDetailsResult ->
            when (getDetailsResult) {
                is ServiceResult.Failure -> ServiceResult.Failure(getDetailsResult.exception)
                is ServiceResult.Value -> ServiceResult.Value(getDetailsResult.value)
            }
        }
    }
}