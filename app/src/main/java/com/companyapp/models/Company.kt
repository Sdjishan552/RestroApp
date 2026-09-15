package com.companyapp.models

data class Company(
    val id: String = "",
    val name: String = "",
    val ownerId: String = "",
    val ownerEmail: String = "",
    val shareCode: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
