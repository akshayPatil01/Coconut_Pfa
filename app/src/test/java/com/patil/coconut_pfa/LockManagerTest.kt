package com.patil.coconut_pfa

import com.patil.coconut_pfa.data.local.security.LockManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LockManagerTest {

    @Test
    fun testExponentialLockoutCalculation() {
        // 1 to 4 attempts: 0s wait
        assertEquals(0, LockManager.calculateWaitSeconds(1))
        assertEquals(0, LockManager.calculateWaitSeconds(4))

        // 5th attempt: 30s
        assertEquals(30, LockManager.calculateWaitSeconds(5))

        // 6th attempt: 60s
        assertEquals(60, LockManager.calculateWaitSeconds(6))

        // 7th attempt: 120s
        assertEquals(120, LockManager.calculateWaitSeconds(7))

        // 8th attempt: 240s
        assertEquals(240, LockManager.calculateWaitSeconds(8))
    }

    @Test
    fun testLockAndUnlock() {
        LockManager.lock()
        assertTrue(LockManager.isLocked.value)

        LockManager.unlock()
        assertFalse(LockManager.isLocked.value)
        assertEquals(0, LockManager.wrongAttempts.value)
        assertEquals(0, LockManager.lockoutRemainingSeconds.value)
    }
}
