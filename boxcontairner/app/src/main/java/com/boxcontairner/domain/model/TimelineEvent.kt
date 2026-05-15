package com.boxcontairner.domain.model

data class TimelineEvent(
    val timestamp: Long = System.currentTimeMillis(),
    val event: String,
    val user: String,
    val remarks: String = ""
)
