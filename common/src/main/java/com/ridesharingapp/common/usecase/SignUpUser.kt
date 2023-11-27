package com.ridesharingapp.common.usecase

import com.ridesharingapp.common.ServiceResult
import com.ridesharingapp.common.domain.GrabLamUser
import com.ridesharingapp.common.services.AuthenticationService
import com.ridesharingapp.common.services.SignUpResult
import com.ridesharingapp.common.services.UserService

class SignUpUser(
    val authService: AuthenticationService,
    val userService: UserService
) {

    suspend fun signUpUser(email: String, password: String, username: String): ServiceResult<SignUpResult> {
        val authAttempt = authService.signUp(email, password)

        return if (authAttempt is ServiceResult.Value) {
            when (authAttempt.value) {
                is SignUpResult.Success -> updateUserDetails(
                    username,
                    authAttempt.value.uid
                )
                else -> authAttempt
            }
        } else authAttempt
    }

    private suspend fun updateUserDetails(
        username: String,
        uid: String
    ): ServiceResult<SignUpResult> {
        return userService.initializeNewUser(
            GrabLamUser(
                userId = uid,
                username = username
            )
        ).let { updateResult ->
            when (updateResult) {
                is ServiceResult.Failure -> ServiceResult.Failure(updateResult.exception)
                is ServiceResult.Value -> ServiceResult.Value(SignUpResult.Success(uid))
            }
        }
    }
}