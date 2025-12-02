package com.nkds.hosikoouma.nouma.data.repository

import com.nkds.hosikoouma.nouma.data.local.User
import com.nkds.hosikoouma.nouma.data.local.UserDao

class UserRepository(private val userDao: UserDao) {

    suspend fun getUserById(userId: Int): User? {
        return userDao.getUserById(userId)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }
}
