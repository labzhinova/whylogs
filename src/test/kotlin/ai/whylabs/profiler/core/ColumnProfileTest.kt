package ai.whylabs.profiler.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class ColumnProfileTest {
    @Test
    fun `tracking int values`() {
        val columnProfile = ColumnProfile("test")
        for (i in (1..100)) {
            columnProfile.track(i)
        }

        val res = columnProfile.toInterpretableStatistics()

        assertEquals(res.longSummary?.count, 100)
        assertNull(res.doubleSummary)
    }

    @Test
    fun `tracking long values`() {
        val columnProfile = ColumnProfile("test")
        for (i in (1..100)) {
            columnProfile.track(i.toLong())
        }

        val res = columnProfile.toInterpretableStatistics()

        assertEquals(res.longSummary?.count, 100)
        assertNull(res.doubleSummary)
    }

    @Test
    fun `tracking double values`() {
        val columnProfile = ColumnProfile("test")
        for (i in (1..100)) {
            columnProfile.track(i.toDouble())
        }

        val res = columnProfile.toInterpretableStatistics()

        assertNull(res.longSummary)
        assertEquals(res.doubleSummary?.count, 100)

    }

    @Test
    fun `tracking float values`() {
        val columnProfile = ColumnProfile("test")
        for (i in (1..100)) {
            columnProfile.track(i.toFloat())
        }

        val res = columnProfile.toInterpretableStatistics()

        assertNull(res.longSummary)
        assertEquals(res.doubleSummary?.count, 100)
    }
}