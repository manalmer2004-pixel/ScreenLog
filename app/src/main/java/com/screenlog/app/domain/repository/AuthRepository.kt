package com.screenlog.app.domain.repository

import com.screenlog.app.core.common.Resource
import com.screenlog.app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun login(email: String, javaPassword: String): Resource<User>
    suspend fun register(displayName: String, email: String, javaPassword: String, homeCountry: String): Resource<User>
    suspend fun logout(): Resource<Unit>
    suspend fun getProfile(userId: String): Resource<User>
    suspend fun updatePassword(newPassword: String): Resource<Unit>
}
