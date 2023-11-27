package com.ridesharingapp.common.services

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.ridesharingapp.common.ServiceResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebasePhotoService(
    val storage: FirebaseStorage,
    val context: Context
) : PhotoService {
    override suspend fun attemptUserAvatarUpdate(url: String): ServiceResult<String> =
        withContext(Dispatchers.IO) {
            val bitmap = Glide.with(context).asBitmap().load(url).submit().get()
            val baos = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val imagesRootRef = storage.reference.child("images")
            val imageRef = imagesRootRef.child(url)
            val uploadTask = imageRef.putBytes(data).await()

            if (!uploadTask.task.isSuccessful) {
                ServiceResult.Failure(
                    uploadTask.task.exception
                        ?: Exception("Upload image task failed in Firebase")
                )
            } else {
                try {
                    val getUrlTask = awaitResult(imageRef.downloadUrl)
                    ServiceResult.Value(getUrlTask.toString())
                } catch (exception: Exception) {
                    ServiceResult.Failure(exception)
                }
            }
        }

    private suspend fun <T> awaitResult(task: Task<T>): T = suspendCoroutine { continuation ->
        task.addOnCompleteListener {
            if (task.isSuccessful) continuation.resume(task.result!!)
            else continuation.resumeWithException(task.exception!!)
        }
    }
}