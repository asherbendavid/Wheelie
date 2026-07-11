package cvc.dashingdog.wheelie

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ResultDialogFragment : DialogFragment() {

    private var taskId: Long = -1L
    private var taskText: String = ""

    // Called after Completed (task deleted) or Skip (task left alone).
    // Host uses this to refresh the wheel either way.
    var onResolved: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        taskId = requireArguments().getLong(ARG_TASK_ID)
        taskText = requireArguments().getString(ARG_TASK_TEXT, "")

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(taskText)
            .setMessage("Completed?")
            .setPositiveButton("Completed") { _, _ ->
                completeTask()
            }
            .setNegativeButton("Skip") { _, _ ->
                onResolved?.invoke()
            }
            .create()

        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK && event.action == android.view.KeyEvent.ACTION_UP) {
                requireActivity().finish()
                true
            } else {
                false
            }
        }

        return dialog
    }

    private fun completeTask() {
        val dao = AppDatabase.getInstance(requireContext()).taskDao()
        lifecycleScope.launch {
            dao.deleteById(taskId)
            onResolved?.invoke()
        }
    }

    companion object {
        const val TAG = "ResultDialogFragment"
        private const val ARG_TASK_ID = "task_id"
        private const val ARG_TASK_TEXT = "task_text"

        fun newInstance(taskId: Long, taskText: String): ResultDialogFragment {
            return ResultDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_TASK_ID, taskId)
                    putString(ARG_TASK_TEXT, taskText)
                }
            }
        }
    }
}