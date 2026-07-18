package com.screenlog.app.data.mapper

import com.screenlog.app.data.local.entity.UserEntity
import com.screenlog.app.domain.model.User

fun UserEntity.toDomain(): User {
    return User(
        userId = userId,
        displayName = displayName,
        email = email,
        homeCountry = homeCountry,
        photoUrl = photoUrl,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isModerator = isModerator
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        userId = userId,
        displayName = displayName,
        email = email,
        homeCountry = homeCountry,
        photoUrl = photoUrl,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isModerator = isModerator
    )
}
