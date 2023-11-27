package com.ridesharingapp.common.services

import com.ridesharingapp.common.ServiceResult
import com.ridesharingapp.common.domain.GrabLamUser

interface UserService {
    suspend fun getUserById(userId: String): ServiceResult<GrabLamUser?>

    suspend fun updateUser(user: GrabLamUser): ServiceResult<GrabLamUser?>

    suspend fun initializeNewUser(user: GrabLamUser): ServiceResult<GrabLamUser?>

    suspend fun logOutUser()
}