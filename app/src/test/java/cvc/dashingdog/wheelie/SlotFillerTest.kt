package cvc.dashingdog.wheelie

import org.junit.Assert.assertEquals
import org.junit.Test

class SlotFillerTest {

    private fun task(id: Long, text: String) = Task(id = id, text = text)

    @Test
    fun `single task returns one slot`() {
        val result = buildSlots(listOf(task(1, "A")))
        assertEquals(1, result.size)
    }

    @Test
    fun `two tasks duplicate to four slots`() {
        val result = buildSlots(listOf(task(1, "A"), task(2, "B")))
        assertEquals(4, result.size)
        assertEquals(listOf("A", "B", "A", "B"), result.map { it.text })
    }

    @Test
    fun `three tasks duplicate to six slots ABCABC`() {
        val result = buildSlots(listOf(task(1, "A"), task(2, "B"), task(3, "C")))
        assertEquals(6, result.size)
        assertEquals(listOf("A", "B", "C", "A", "B", "C"), result.map { it.text })
    }

    @Test
    fun `four tasks no duplication`() {
        val tasks = listOf(task(1, "A"), task(2, "B"), task(3, "C"), task(4, "D"))
        val result = buildSlots(tasks)
        assertEquals(4, result.size)
    }

    @Test
    fun `twenty tasks no duplication`() {
        val tasks = (1..20).map { task(it.toLong(), "Task$it") }
        val result = buildSlots(tasks)
        assertEquals(20, result.size)
    }

    @Test
    fun `twenty one tasks caps at twenty and queues the rest`() {
        val tasks = (1..21).map { task(it.toLong(), "Task$it") }
        val result = buildSlots(tasks)
        assertEquals(20, result.size)
        // task 21 should not appear — it's queued, not on the wheel
        assertEquals(false, result.any { it.text == "Task21" })
    }

    @Test
    fun `empty list returns no slots`() {
        val result = buildSlots(emptyList())
        assertEquals(0, result.size)
    }
}