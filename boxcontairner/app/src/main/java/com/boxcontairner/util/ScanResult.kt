package com.boxcontairner.util

sealed class ScanResult {
    data class ContainerId(val code: String) : ScanResult()
    data class ReeferInfo(val data: ReeferData) : ScanResult()
}
