package com.ridesharingapp.common.services

import com.ridesharingapp.common.ServiceResult

interface PhotoService {
    suspend fun attemptUserAvatarUpdate(url: String): ServiceResult<String>
}