package cvc.dashingdog.wheelie

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class QuickAddActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dialog = AddTaskDialogFragment()
        dialog.onTaskAdded = { finish() }
        dialog.show(supportFragmentManager, AddTaskDialogFragment.TAG)
    }
}