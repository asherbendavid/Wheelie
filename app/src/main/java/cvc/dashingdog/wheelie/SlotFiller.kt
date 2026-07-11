package cvc.dashingdog.wheelie

data class TaskSlot(val taskId: Long, val text: String)

const val MIN_SLOTS = 4
const val MAX_SLOTS = 20

/**
 * Builds the wheel's slot list from the full task backlog.
 * - Tasks beyond MAX_SLOTS queue silently (oldest tasks shown first, FIFO).
 * - If fewer than MIN_SLOTS visible tasks exist, they are duplicated evenly
 *   (round-robin) until at least MIN_SLOTS slots are filled.
 * - Duplication is display-only: each TaskSlot still points at a real
 *   taskId, so completing one slot only removes that one task row.
 */
fun buildSlots(allTasks: List<Task>): List<TaskSlot> {
    if (allTasks.isEmpty()) return emptyList()

    val visible = allTasks.take(MAX_SLOTS)
    val n = visible.size

    if (n==1) {
        return listOf(TaskSlot(visible[0].id, visible[0].text))
    }

    val targetSlotCount = if (n >= MIN_SLOTS) {
        n
    } else {
        // smallest multiple of n that is >= MIN_SLOTS
        val repeats = (MIN_SLOTS + n - 1) / n
        n * repeats
    }

    return List(targetSlotCount) { index ->
        val task = visible[index % n]
        TaskSlot(task.id, task.text)
    }
}