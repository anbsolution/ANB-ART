package com.anb.recovery

import android.os.Build

data class DeviceReport(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val sdk: Int,
    val deviceCode: String
)

object DeviceDiagnostics {
    fun collect() = DeviceReport(
        Build.MANUFACTURER.orUnknown(),
        Build.MODEL.orUnknown(),
        Build.VERSION.RELEASE.orUnknown(),
        Build.VERSION.SDK_INT,
        Build.DEVICE.orUnknown()
    )
    private fun String?.orUnknown() = this?.takeIf { it.isNotBlank() } ?: "Unknown"
}
