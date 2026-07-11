package cvc.dashingdog.wheelie

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AddTaskDialogFragment : DialogFragment() {

    // Optional callback so a host can react after a successful insert
    // (e.g. MainActivity refreshing the wheel). Not required — Room's
    // Flow-based observeAll() would also pick this up reactively if
    // MainActivity is collecting it, but this keeps things explicit
    // and simple for now.
    var onTaskAdded: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val editText = EditText(requireContext()).apply {
            hint = "What needs doing?"
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_DONE
            isSingleLine = true
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add task")
            .setView(editText)
            .setPositiveButton("Add") { _, _ ->
                val text = editText.text.toString().trim()
                if (text.isNotEmpty()) {
                    saveTask(text)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                val text = editText.text.toString().trim()
                if (text.isNotEmpty()) {
                    saveTask(text)
                }
                dialog.dismiss()
                true
            } else {
                false
            }
        }

        dialog.window?.setSoftInputMode(
            android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        )

        return dialog
    }

    private fun saveTask(text: String) {
        val dao = AppDatabase.getInstance(requireContext()).taskDao()
        lifecycleScope.launch {
            dao.insert(Task(text = text))
            onTaskAdded?.invoke()
        }
    }

    companion object {
        const val TAG = "AddTaskDialogFragment"
    }
}