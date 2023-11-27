package com.ridesharingapp.common.usecase

import com.ridesharingapp.common.ServiceResult
import com.ridesharingapp.common.services.AuthenticationService
import com.ridesharingapp.common.services.UserService


class LogOutUser(
    val authService: AuthenticationService,
    val userService: UserService
) {

    suspend fun logout(): ServiceResult<Unit> {
        authService.logout()
        userService.logOutUser()

        return ServiceResult.Value(Unit)
    }
}