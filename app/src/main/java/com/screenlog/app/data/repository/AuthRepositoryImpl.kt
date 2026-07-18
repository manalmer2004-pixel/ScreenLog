package com.screenlog.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.screenlog.app.core.common.Resource
import com.screenlog.app.data.local.dao.UserDao
import com.screenlog.app.data.mapper.toDomain
import com.screenlog.app.data.mapper.toEntity
import com.screenlog.app.data.remote.firebase.FirebaseAuthDataSource
import com.screenlog.app.data.remote.firebase.FirestoreDataSource
import com.screenlog.app.domain.model.User
import com.screenlog.app.domain.repository.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource,
    private val firestoreDataSource: FirestoreDataSource,
    private val userDao: UserDao
) : AuthRepository {

    override val currentUser: Flow<User?> = authDataSource.currentUserStateFlow.flatMapLatest { firebaseUser ->
        if (firebaseUser == null) {
            flowOf<User?>(null)
        } else {
            userDao.getUserByIdFlow(firebaseUser.uid).map { entity ->
                entity?.toDomain() ?: User(
                    userId = firebaseUser.uid,
                    displayName = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    homeCountry = "KE",
                    photoUrl = firebaseUser.photoUrl?.toString(),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    isModerator = false
                )
            }
        }
    }

    override suspend fun login(email: String, javaPassword: String): Resource<User> {
        return try {
            val firebaseUser = authDataSource.signInWithEmail(email, javaPassword)
            val profile = firestoreDataSource.getUserProfile(firebaseUser.uid)
            userDao.insertUser(profile.toEntity())
            Resource.Success(profile)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Authentication failed")
        }
    }

    override suspend fun register(
        displayName: String,
        email: String,
        javaPassword: String,
        homeCountry: String
    ): Resource<User> {
        return try {
            val firebaseUser = authDataSource.signUpWithEmail(email, javaPassword)
            val newUser = User(
                userId = firebaseUser.uid,
                displayName = displayName,
                email = email,
                homeCountry = homeCountry,
                photoUrl = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isModerator = false
            )
            firestoreDataSource.saveUserProfile(newUser)
            userDao.insertUser(newUser.toEntity())
            Resource.Success(newUser)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration failed")
        }
    }

    override suspend fun logout(): Resource<Unit> {
        return try {
            authDataSource.signOut()
            userDao.clearUsers()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Signout failed")
        }
    }

    override suspend fun getProfile(userId: String): Resource<User> {
        return try {
            val user = firestoreDataSource.getUserProfile(userId)
            userDao.insertUser(user.toEntity())
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch profile")
        }
    }

    override suspend fun updatePassword(newPassword: String): Resource<Unit> {
        return try {
            authDataSource.updatePassword(newPassword)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update password")
        }
    }
}
