package com.nkds.hosikoouma.nouma.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["email"], unique = true)
    ]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val email: String,
    val passwordHash: String,
    val isBot: Boolean = false,
    val bio: String? = null,
    val avatarUri: String? = null,
    val nickname: String? = null // Отображаемое имя (никнейм)
)
