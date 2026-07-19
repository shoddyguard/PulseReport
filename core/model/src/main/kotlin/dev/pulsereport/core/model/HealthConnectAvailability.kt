package dev.pulsereport.core.model

/** Whether Health Connect can be used on this device. */
enum class HealthConnectAvailability {
    AVAILABLE,
    UPDATE_REQUIRED,
    NOT_AVAILABLE,
}
