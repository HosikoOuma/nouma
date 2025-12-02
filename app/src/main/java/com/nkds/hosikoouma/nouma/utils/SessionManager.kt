package com.nkds.hosikoouma.nouma.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Управляет сессией пользователя, сохраняя состояние входа в SharedPreferences.
 */
class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences("NoumaAppPrefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_LOGGED_IN = "user_logged_in"
        const val LOGGED_IN_USER_ID = "logged_in_user_id"
    }

    /**
     * Сохраняет состояние входа пользователя.
     */
    fun setLoggedIn(isLoggedIn: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean(USER_LOGGED_IN, isLoggedIn)
        editor.apply()
    }

    /**
     * Проверяет, вошел ли пользователь в систему.
     * @return true, если пользователь вошел, иначе false.
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(USER_LOGGED_IN, false)
    }

    /**
     * Сохраняет ID вошедшего пользователя.
     */
    fun saveUserId(userId: Int) {
        val editor = prefs.edit()
        editor.putInt(LOGGED_IN_USER_ID, userId)
        editor.apply()
    }

    /**
     * Получает ID вошедшего пользователя.
     * @return ID пользователя или -1, если не найден.
     */
    fun getUserId(): Int {
        return prefs.getInt(LOGGED_IN_USER_ID, -1)
    }
}
