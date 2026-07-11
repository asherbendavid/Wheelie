package cvc.dashingdog.wheelie

import android.content.Context

object PendingTaskStore {
    private const val PREFS_NAME = "wheelie_prefs"
    private const val KEY_PENDING_TASK_ID = "pending_task_id"
    private const val KEY_PENDING_TASK_TEXT = "pending_task_text"

    fun save(context: Context, taskId: Long, taskText: String) {
        prefs(context).edit()
            .putLong(KEY_PENDING_TASK_ID, taskId)
            .putString(KEY_PENDING_TASK_TEXT, taskText)
            .apply()
    }

    fun clear(context: Context) {
        prefs(context).edit()
            .remove(KEY_PENDING_TASK_ID)
            .remove(KEY_PENDING_TASK_TEXT)
            .apply()
    }

    fun getPending(context: Context): Pair<Long, String>? {
        val id = prefs(context).getLong(KEY_PENDING_TASK_ID, -1L)
        val text = prefs(context).getString(KEY_PENDING_TASK_TEXT, null)
        return if (id != -1L && text != null) id to text else null
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}