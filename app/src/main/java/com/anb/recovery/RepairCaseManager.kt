package com.anb.recovery

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RepairCaseManager {
    fun createLocalSummary(consent: Boolean, report: DeviceReport, notes: String): String {
        require(ConsentManager.mayCreateRepairSummary(consent)) {
            "Customer consent is required."
        }
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        return listOf(
            "ANB Android Recovery - Repair Summary",
            "Date: $timestamp",
            "Manufacturer: ${report.manufacturer}",
            "Model: ${report.model}",
            "Android: ${report.androidVersion} (SDK ${report.sdk})",
            "Device code: ${report.deviceCode}",
            "",
            "Technician notes:",
            notes.ifBlank { "No notes provided." },
            "",
            "Consent: confirmed by technician",
            "Generated locally; no data uploaded."
        ).joinToString("\n")
    }
}
