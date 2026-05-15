package com.boxcontairner.domain.model

data class User(
    val uid: String = "",
    val nick: String = "",
    val role: String = Role.OPERATOR.name,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val roleEnum: Role get() = Role.fromStringOrDefault(role)
}
