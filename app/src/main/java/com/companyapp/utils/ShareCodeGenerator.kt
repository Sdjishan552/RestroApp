package com.companyapp.utils

object ShareCodeGenerator {
    private val CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    fun generate(length: Int = 6): String = (1..length).map { CHARS.random() }.joinToString("")
}
