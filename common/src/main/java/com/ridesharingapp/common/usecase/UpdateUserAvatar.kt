package com.ridesharingapp.common.usecase

import com.ridesharingapp.common.ServiceResult
import com.ridesharingapp.common.domain.GrabLamUser
import com.ridesharingapp.common.services.PhotoService
import com.ridesharingapp.common.services.UserService


class UpdateUserAvatar(
    val photoService: PhotoService,
    val userService: UserService
) {
    suspend fun updateAvatar(user: GrabLamUser, uri: String): ServiceResult<String> {
        val updateAvatar = photoService.attemptUserAvatarUpdate(uri)
        return when (updateAvatar) {
            is ServiceResult.Failure -> updateAvatar
            is ServiceResult.Value -> updateUserPhoto(user, updateAvatar.value)
        }
    }

    private suspend fun updateUserPhoto(
        user: GrabLamUser,
        newUrl: String
    ): ServiceResult<String> {
        return userService.updateUser(
            user.copy(
                avatarPhotoUrl = newUrl
            )
        ).let { updateResult ->
            when (updateResult) {
                is ServiceResult.Failure -> ServiceResult.Failure(updateResult.exception)
                is ServiceResult.Value -> ServiceResult.Value(newUrl)
            }
        }
    }
}