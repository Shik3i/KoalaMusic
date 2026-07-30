package net.koalastuff.music.core.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QueueIndexPolicyTest {
    @Test
    fun `play next inserts after current item`() {
        assertEquals(
            3,
            QueueIndexPolicy.insertionIndexAfterCurrent(itemCount = 5, currentIndex = 2)
        )
    }

    @Test
    fun `play next appends when current item is last`() {
        assertEquals(
            5,
            QueueIndexPolicy.insertionIndexAfterCurrent(itemCount = 5, currentIndex = 4)
        )
    }

    @Test
    fun `valid index excludes empty and upper bound`() {
        assertFalse(QueueIndexPolicy.validIndex(itemCount = 0, index = 0))
        assertTrue(QueueIndexPolicy.validIndex(itemCount = 3, index = 2))
        assertFalse(QueueIndexPolicy.validIndex(itemCount = 3, index = 3))
    }

    @Test
    fun `move rejects unchanged or out of range positions`() {
        assertNull(QueueIndexPolicy.move(itemCount = 3, fromIndex = 1, toIndex = 1))
        assertNull(QueueIndexPolicy.move(itemCount = 3, fromIndex = -1, toIndex = 1))
        assertNull(QueueIndexPolicy.move(itemCount = 3, fromIndex = 1, toIndex = 3))
    }

    @Test
    fun `move returns validated positions`() {
        assertEquals(2 to 0, QueueIndexPolicy.move(itemCount = 3, fromIndex = 2, toIndex = 0))
    }
}
