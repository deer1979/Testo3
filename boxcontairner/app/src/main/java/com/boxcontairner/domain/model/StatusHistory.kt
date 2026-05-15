package com.boxcontairner.domain.model

data class StatusHistory(
    val id: String = "",
    val containerId: String = "",
    val action: String = HistoryAction.STATUS_CHANGE.name,
    val previousStatus: String = "",
    val newStatus: String = "",
    val changedBy: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    val actionEnum: HistoryAction get() = HistoryAction.fromStringOrDefault(action)
}
