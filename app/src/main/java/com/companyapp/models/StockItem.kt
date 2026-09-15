package com.companyapp.models

data class StockItem(
    val id: String = "",
    val name: String = "",
    val quantity: Double = 0.0,
    val unit: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val minQuantity: Double = 5.0,
    val lastUpdated: Long = System.currentTimeMillis()
)
