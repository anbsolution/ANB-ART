package com.anb.recovery

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConsentManagerTest {
    @Test fun summaryRequiresConsent() {
        assertFalse(ConsentManager.mayCreateRepairSummary(false))
        assertTrue(ConsentManager.mayCreateRepairSummary(true))
    }
}
