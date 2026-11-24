package com.example.myfirstapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val repository = Repository()

    private lateinit var seekBar: SeekBar
    private lateinit var valueText: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var firstButton: Button
    private lateinit var secondButton: Button
    private lateinit var cancelButton: Button

    private var selectedValue = 5
    private var activeJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        seekBar = findViewById(R.id.seekBar)
        valueText = findViewById(R.id.valueText)
        progressBar = findViewById(R.id.progressBar)

        firstButton = findViewById(R.id.firstButton)
        secondButton = findViewById(R.id.secondButton)
        cancelButton = findViewById(R.id.cancelButton)

        valueText.text = "Выбранное значение: $selectedValue"
        cancelButton.isEnabled = false

        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    selectedValue = progress
                    valueText.text = "Выбранное значение: $progress"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            }
        )

        firstButton.setOnClickListener {
            startWork {
                coroutineScope {
                    repeat(3) {
                        launch {
                            repository.doWork(selectedValue)
                        }.join()
                    }
                }
            }
        }

        secondButton.setOnClickListener {
            startWork {
                coroutineScope {
                    val jobs = List(3) {
                        launch {
                            repository.doWork(selectedValue)
                        }
                    }
                    jobs.joinAll()
                }
            }
        }

        cancelButton.setOnClickListener {
            activeJob?.cancel()
        }
    }

    private fun startWork(block: suspend () -> Unit) {
        if (activeJob?.isActive == true) return

        setRunningState(true)

        activeJob = lifecycleScope.launch {
            try {
                block()
            } finally {
                setRunningState(false)
                activeJob = null
            }
        }
    }

    private fun setRunningState(isRunning: Boolean) {
        progressBar.visibility = if (isRunning) View.VISIBLE else View.GONE
        firstButton.isEnabled = !isRunning
        secondButton.isEnabled = !isRunning
        cancelButton.isEnabled = isRunning
    }
}
