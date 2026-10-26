package com.anb.recovery

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConsentManagerTest {
    @Test
    fun consentIsRequiredBeforeCreatingASummary() {
        assertFalse(ConsentManager.mayCreateRepairSummary(false))
    }

    @Test
    fun confirmedConsentAllowsSummaryCreation() {
        assertTrue(ConsentManager.mayCreateRepairSummary(true))
    }
}
