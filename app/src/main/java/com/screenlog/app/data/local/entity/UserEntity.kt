package com.screenlog.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val displayName: String,
    val email: String,
    val homeCountry: String,
    val photoUrl: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isModerator: Boolean
)
