package com.nkds.hosikoouma.nouma.features.contacts

data class Contact(
    val id: Long,
    val name: String,
    val phoneNumber: String? = null
)
