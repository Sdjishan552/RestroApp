package com.companyapp.models

data class Permissions(
    val stock: Boolean = false,
    val orders: Boolean = false,
    val finance: Boolean = false,
    val products: Boolean = false,
    val reports: Boolean = false
)
