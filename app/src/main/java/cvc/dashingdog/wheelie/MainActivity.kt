package cvc.dashingdog.wheelie

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.google.android.material.floatingactionbutton.FloatingActionButton

//import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private var currentSlots: List<TaskSlot> = emptyList()
    private lateinit var wheelView: WheelView
    private lateinit var spinButton: Button
    private lateinit var addTaskFab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        wheelView = findViewById(R.id.wheelView)
        spinButton = findViewById<Button>(R.id.spinButton)
        addTaskFab = findViewById<FloatingActionButton>(R.id.addTaskFab)

        refreshWheelFromDatabase()

        spinButton.visibility = if (currentSlots.isEmpty()) View.GONE else View.VISIBLE
        spinButton.setOnClickListener {
            if (currentSlots.size == 1) {
                showResultDialog(currentSlots[0])
            } else {
                startSpin()
            }
        }

        addTaskFab.setOnClickListener {
            val dialog = AddTaskDialogFragment()
            dialog.onTaskAdded = { refreshWheelFromDatabase() }
            dialog.show(supportFragmentManager, AddTaskDialogFragment.TAG)
        }

        val pending = PendingTaskStore.getPending(this)
        if (pending != null) {
            val (taskId, taskText) = pending
            val dialog = ResultDialogFragment.newInstance(taskId, taskText)
            dialog.onResolved = {
                PendingTaskStore.clear(this)
                refreshWheelFromDatabase()
            }
            dialog.show(supportFragmentManager, ResultDialogFragment.TAG)
        }
    }

    private var isSpinning = false

    private fun refreshWheelFromDatabase() {
        lifecycleScope.launch {
            val allTasks = AppDatabase.getInstance(this@MainActivity).taskDao().getAll()
            currentSlots = buildSlots(allTasks)
            wheelView.setSlots(currentSlots)
            spinButton.visibility = if (currentSlots.isEmpty()) View.GONE else View.VISIBLE
        }
    }
    private fun startSpin() {
        if (isSpinning || currentSlots.isEmpty()) return
        isSpinning = true
        addTaskFab.isEnabled = false

        val slotCount = currentSlots.size
        val sweepPerSlot = 360f / slotCount

        // Random full rotations (5-8) plus a random final resting angle
        val fullRotations = (5..8).random()
        val randomOffset = (0 until 360).random().toFloat()
        val targetRotation = wheelView.rotationDegrees + fullRotations * 360f + randomOffset

        val animator = android.animation.ValueAnimator.ofFloat(wheelView.rotationDegrees, targetRotation)
        animator.duration = 4000L
        animator.interpolator = android.view.animation.DecelerateInterpolator(2.5f)

        animator.addUpdateListener { anim ->
            wheelView.rotationDegrees = anim.animatedValue as Float
        }

        animator.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                isSpinning = false
                addTaskFab.isEnabled = true
                val landedIndex = calculateLandedIndex(wheelView.rotationDegrees, sweepPerSlot, slotCount)
                val landedSlot = currentSlots[landedIndex]
                //Toast.makeText(this@MainActivity, "Landed on: ${landedSlot.text}", Toast.LENGTH_SHORT).show()
                showResultDialog(landedSlot)
            }
        })

        animator.start()
    }

    private fun calculateLandedIndex(finalRotation: Float, sweepPerSlot: Float, slotCount: Int): Int {
        // The pointer is fixed at the top (0°). As the wheel rotates clockwise,
        // the slot now under the pointer is the one whose un-rotated position
        // equals (360 - normalizedRotation), since the wheel moved, not the pointer.
        val normalized = ((finalRotation % 360f) + 360f) % 360f
        val angleUnderPointer = ((270f - normalized) % 360f + 360f) % 360f
        val index = (angleUnderPointer / sweepPerSlot).toInt()
        return index.coerceIn(0, slotCount - 1)
    }

    private fun showResultDialog(slot: TaskSlot) {
        PendingTaskStore.save(this, slot.taskId, slot.text)
        val dialog = ResultDialogFragment.newInstance(slot.taskId, slot.text)
        dialog.onResolved = {
            PendingTaskStore.clear(this)
            refreshWheelFromDatabase()
        }
        dialog.show(supportFragmentManager, ResultDialogFragment.TAG)
    }
}