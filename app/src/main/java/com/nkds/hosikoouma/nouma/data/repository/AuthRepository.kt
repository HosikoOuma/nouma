package com.nkds.hosikoouma.nouma.data.repository

import com.nkds.hosikoouma.nouma.data.local.User
import com.nkds.hosikoouma.nouma.data.local.UserDao

class AuthRepository(private val userDao: UserDao) {

    suspend fun registerUser(user: User): Long {
        return userDao.insertUser(user)
    }

    suspend fun findUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }

    suspend fun findUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }
}
