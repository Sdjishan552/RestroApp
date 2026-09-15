package com.companyapp.models

data class Employee(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val joinedAt: Long = System.currentTimeMillis(),
    val permissions: Permissions = Permissions()
)
