package com.ridesharingapp.common.services

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.ridesharingapp.common.ServiceResult
import com.ridesharingapp.common.domain.GrabLamUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseAuthService(
    val auth: FirebaseAuth
) : AuthenticationService {
    override suspend fun signUp(
        email: String,
        password: String
    ): ServiceResult<SignUpResult> = withContext(Dispatchers.IO) {
        try {
            val authAttempt = auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{
                if (!it.isSuccessful)
                    Log.w("FirebaseAuthService", "createUserWithEmailAndPassword failed", it.exception)
            }.await()
            if (authAttempt.user != null) ServiceResult.Value(
                SignUpResult.Success(authAttempt.user!!.uid)
            )
            else ServiceResult.Failure(Exception("Null user"))
        } catch (exception: Exception) {
            when (exception) {
                is FirebaseAuthWeakPasswordException -> ServiceResult.Value(SignUpResult.InvalidCredentials)
                is FirebaseAuthInvalidCredentialsException -> ServiceResult.Value(SignUpResult.InvalidCredentials)
                is FirebaseAuthUserCollisionException -> ServiceResult.Value(SignUpResult.AlreadySignedUp)
                else -> ServiceResult.Failure(exception)
            }
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): ServiceResult<LogInResult> = withContext(Dispatchers.IO) {
        try {
            val authAttempt = auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{
                if (!it.isSuccessful)
                    Log.w("FirebaseAuthService", "SignInWithEmailAndPassword failed", it.exception)
            }.await()
            if (authAttempt.user != null) ServiceResult.Value(
                LogInResult.Success(
                    GrabLamUser(
                        userId = authAttempt.user!!.uid
                    )
                )
            )
            else {
                ServiceResult.Failure(Exception("Null user"))
            }
        } catch (exception: Exception) {
            when (exception) {
                is FirebaseAuthInvalidUserException -> ServiceResult.Value(LogInResult.InvalidCredentials)
                is FirebaseAuthInvalidCredentialsException -> ServiceResult.Value(LogInResult.InvalidCredentials)
                else -> ServiceResult.Failure(exception)
            }
        }
    }

    override fun logout(): ServiceResult<Unit> {
        auth.signOut()
        return ServiceResult.Value(Unit)
    }

    override suspend fun getSession(): ServiceResult<GrabLamUser?> {
//        logout()
//        return ServiceResult.Value(null)
        val firebaseUser = auth.currentUser
        return if (firebaseUser == null) ServiceResult.Value(null)
        else ServiceResult.Value(
            GrabLamUser(
                userId = firebaseUser.uid
            )
        )
    }
}