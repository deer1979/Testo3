package com.containerpro.model

// ── Session ───────────────────────────────────────────────
data class ContainerSession(
    val containerId : String,
    val modelName   : String,
    val technicianId: String,
    val timestamp   : Long = System.currentTimeMillis(),
)

// ── Relay ─────────────────────────────────────────────────
/**
 * Single relay coil state.
 * [address] matches ML5 coil register (0x0001–0x000C).
 * [group] used to group relays on the UI.
 * [isDanger] = true → requires double confirmation before toggling.
 */
data class RelayInfo(
    val address  : Int,
    val name     : String,
    val group    : String,
    val isActive : Boolean = false,
    val isDanger : Boolean = false,
)

// ── Electrical ────────────────────────────────────────────
data class ElectricalData(
    val voltageL1   : Float = 0f,
    val voltageL2   : Float = 0f,
    val voltageL3   : Float = 0f,
    val currentComp : Float = 0f,
    val currentFans : Float = 0f,
    val powerKw     : Float = 0f,
    val frequencyHz : Float = 0f,
    val powerFactor : Float = 0f,
)

// ── Temperature ───────────────────────────────────────────
data class TemperatureData(
    val supplyAir : Float = 0f,
    val returnAir : Float = 0f,
    val setpoint  : Float = -18f,
    val evapCoil  : Float = 0f,
    val condCoil  : Float = 0f,
    val ambient   : Float = 0f,
    val discharge : Float = 0f,
    val suction   : Float = 0f,
)

// ── Alarm ─────────────────────────────────────────────────
data class Alarm(
    val code    : String,
    val message : String,
    val severity: String,
    val time    : String,
)
