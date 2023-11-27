package com.ridesharingapp.common.services

import com.ridesharingapp.common.ServiceResult
import com.ridesharingapp.common.domain.GrabLamUser

interface AuthenticationService {
    /**
     * @return uid if sign up is successful
     */
    suspend fun signUp(email: String, password: String): ServiceResult<SignUpResult>
    suspend fun login(email: String, password: String): ServiceResult<LogInResult>

    fun logout(): ServiceResult<Unit>

    /**
     * @return true if a user session is active, else null
     */
    suspend fun getSession(): ServiceResult<GrabLamUser?>
}

sealed interface SignUpResult {
    data class Success(val uid: String) : SignUpResult
    object AlreadySignedUp : SignUpResult
    object InvalidCredentials : SignUpResult
}

sealed interface LogInResult {
    data class Success(val user: GrabLamUser) : LogInResult
    object InvalidCredentials : LogInResult
}