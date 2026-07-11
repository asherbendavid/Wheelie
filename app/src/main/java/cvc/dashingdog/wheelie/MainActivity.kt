package cvc.dashingdog.wheelie

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private var currentSlots: List<TaskSlot> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val rawTasks = listOf(
            Task(1, "Mow the lawn"),
            Task(2, "Pack away shopping"),
            Task(3, "Fix loose db board cover"),
            //Task(4, "Feed the chickens"),
            //Task(5, "Walk the dog"),
        )
        currentSlots = buildSlots(rawTasks)
        currentSlots = emptyList()

        val wheelView = findViewById<WheelView>(R.id.wheelView)
        wheelView.setSlots(currentSlots)

        // inside onCreate, after wheelView.setSlots(...)
        val spinButton = findViewById<Button>(R.id.spinButton)
        spinButton.visibility = if (currentSlots.isEmpty()) View.GONE else View.VISIBLE
        spinButton.setOnClickListener {
            if (currentSlots.isEmpty()) return@setOnClickListener
            val landedIndex = Random.nextInt(currentSlots.size)
            val landedSlot = currentSlots[landedIndex]
            Toast.makeText(this, "Landed on: ${landedSlot.text}", Toast.LENGTH_SHORT).show()
        }
    }
}