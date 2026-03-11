package com.containerpro.model

data class ContainerSession(
    val containerId : String,
    val modelName   : String,
    val technicianId: String,
    val timestamp   : Long = System.currentTimeMillis(),
)

data class RelayState(
    val address: Int,
    val name   : String,
    val enabled: Boolean,
    val danger : Boolean = false,
)

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

data class TemperatureData(
    val supplyAir   : Float = 0f,
    val returnAir   : Float = 0f,
    val setpoint    : Float = 0f,
    val evapCoil    : Float = 0f,
    val condCoil    : Float = 0f,
    val ambient     : Float = 0f,
    val discharge   : Float = 0f,
    val suction     : Float = 0f,
)

data class Alarm(
    val code    : String,
    val message : String,
    val severity: String,
    val time    : String,
)
