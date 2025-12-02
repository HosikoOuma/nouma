package com.nkds.hosikoouma.nouma.features.chats

data class Chat(
    val id: String,
    val name: String,
    val lastMessage: String,
    val avatarUrl: String? = null // Пока оставим nullable для аватара
)
