package com.anb.recovery

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RepairCaseManager {
    /** Sharing is allowed only when consent remains checked and notes match the generated report. */
    fun canShareSummary(generatedNotes: String?, currentNotes: String, consentConfirmed: Boolean): Boolean =
        consentConfirmed && generatedNotes != null && generatedNotes == currentNotes

    fun createShareReviewMessage(summary: String): String =
        "Review the exact report below before sharing. It may contain device details and technician notes.\n\n" +
            summary +
            "\n\nContinue only if you have authorization to share this information."

    /** True only if the reviewed report remains unchanged. */
    fun reviewedReportMatches(reviewedReport: String, currentReport: String): Boolean =
        reviewedReport == currentReport

    /** Requires explicit acknowledgement of the report review. */
    fun mayProceedAfterReviewAcknowledgement(acknowledged: Boolean): Boolean =
        acknowledged

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
