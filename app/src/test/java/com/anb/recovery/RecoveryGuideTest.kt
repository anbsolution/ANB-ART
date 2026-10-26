package com.anb.recovery

import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryGuideTest {
    @Test
    fun guideIncludesOwnershipAndOfficialRecoverySafeguards() {
        val guide = RecoveryGuide.steps.joinToString(" ")

        assertTrue(guide.contains("ownership"))
        assertTrue(guide.contains("official recovery"))
        assertTrue(guide.contains("Factory Reset Protection"))
        assertTrue(guide.contains("do not promise universal unlocking"))
    }
}
