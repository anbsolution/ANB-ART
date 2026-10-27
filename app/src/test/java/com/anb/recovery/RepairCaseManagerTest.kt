package com.anb.recovery

import org.junit.Assert.assertTrue
import org.junit.Test

class RepairCaseManagerTest {
    private val report = DeviceReport(
        manufacturer = "ExampleMaker",
        model = "ExampleModel",
        androidVersion = "14",
        sdk = 34,
        deviceCode = "example_device"
    )

    @Test(expected = IllegalArgumentException::class)
    fun summaryCreationRejectsMissingConsent() {
        RepairCaseManager.createLocalSummary(false, report, "Test note")
    }

    @Test
    fun summaryContainsDeviceDetailsAndLocalOnlyNotice() {
        val summary = RepairCaseManager.createLocalSummary(true, report, "Diagnostic check")

        assertTrue(summary.contains("ANB Android Recovery - Repair Summary"))
        assertTrue(summary.contains("Manufacturer: ExampleMaker"))
        assertTrue(summary.contains("Model: ExampleModel"))
        assertTrue(summary.contains("Android: 14 (SDK 34)"))
        assertTrue(summary.contains("Device code: example_device"))
        assertTrue(summary.contains("Diagnostic check"))
        assertTrue(summary.contains("Consent: confirmed by technician"))
        assertTrue(summary.contains("Generated locally; no data uploaded."))
    }

    @Test
    fun multilineTechnicianNotesArePreserved() {
        val summary = RepairCaseManager.createLocalSummary(true, report, "Battery check\nCharging test")
        assertTrue(summary.contains("Battery check\nCharging test"))
    }

    @Test
    fun sharingRequiresConsentAndExactGeneratedNotes() {
        assertTrue(RepairCaseManager.canShareSummary("Battery check", "Battery check", true))
        assertTrue(!RepairCaseManager.canShareSummary("Battery check", "Charging test", true))
        assertTrue(!RepairCaseManager.canShareSummary("Battery check", "Battery check", false))
        assertTrue(!RepairCaseManager.canShareSummary(null, "Battery check", true))
    }

    @Test
    fun shareReviewMessageIncludesExactReportAndPrivacyReminder() {
        val review = RepairCaseManager.createShareReviewMessage("Sample repair report")
        assertTrue(review.contains("Sample repair report"))
        assertTrue(review.contains("device details and technician notes"))
        assertTrue(review.contains("authorization to share"))
    }

    @Test
    fun reviewedReportMustRemainExact() {
        assertTrue(RepairCaseManager.reviewedReportMatches("Report A", "Report A"))
        assertTrue(!RepairCaseManager.reviewedReportMatches("Report A", "Report B"))
    }

    @Test
    fun sharingRequiresExplicitReviewAcknowledgement() {
        assertTrue(RepairCaseManager.mayProceedAfterReviewAcknowledgement(true))
        assertTrue(!RepairCaseManager.mayProceedAfterReviewAcknowledgement(false))
    }

    @Test
    fun reviewedReportSharingRequiresAllConditions() {
        assertTrue(RepairCaseManager.mayShareReviewedReport(
            true, "notes", "notes", "report", "report", true
        ))
        assertTrue(!RepairCaseManager.mayShareReviewedReport(
            false, "notes", "notes", "report", "report", true
        ))
        assertTrue(!RepairCaseManager.mayShareReviewedReport(
            true, "notes", "changed notes", "report", "report", true
        ))
        assertTrue(!RepairCaseManager.mayShareReviewedReport(
            true, "notes", "notes", "report", "changed report", true
        ))
        assertTrue(!RepairCaseManager.mayShareReviewedReport(
            true, "notes", "notes", "report", "report", false
        ))
    }

    @Test
    fun blankNotesUseSafePlaceholder() {
        val summary = RepairCaseManager.createLocalSummary(true, report, "  ")

        assertTrue(summary.contains("No notes provided."))
    }
}
