package com.example.slipi1

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class Powiadomienia : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var schedulePrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_powiadomienia)
        supportActionBar?.hide()

        prefs = getSharedPreferences("NotificationPrefs", MODE_PRIVATE)
        schedulePrefs = getSharedPreferences("SchedulePrefs", MODE_PRIVATE)

        val sennikSwitch = findViewById<Switch>(R.id.sennikReminderSwitch)
        val sleepSwitch = findViewById<Switch>(R.id.sleepReminderSwitch)
        val missingSwitch = findViewById<Switch>(R.id.missingReminderSwitch)

        val sennikTime = findViewById<TextView>(R.id.sennikReminderTime)
        val sleepTime = findViewById<TextView>(R.id.sleepReminderTime)

        val saveButton = findViewById<Button>(R.id.saveButton)
        val backArrow = findViewById<ImageView>(R.id.backArrow)

        // Załaduj stany przełączników
        sennikSwitch.isChecked = prefs.getBoolean("wakeReminder", true)
        sleepSwitch.isChecked = prefs.getBoolean("sleepReminder", true)
        missingSwitch.isChecked = prefs.getBoolean("missingReminder", true)

        // Wyświetl godziny na podstawie harmonogramu
        sennikTime.text = "(15 minut po pobudce – ${obliczGodzine("+15", wakeTime = true)})"
        sleepTime.text = "(1 godzina przed snem – ${obliczGodzine("-60", wakeTime = false)})"

        saveButton.setOnClickListener {
            prefs.edit().apply {
                putBoolean("wakeReminder", sennikSwitch.isChecked)
                putBoolean("sleepReminder", sleepSwitch.isChecked)
                putBoolean("missingReminder", missingSwitch.isChecked)
                apply()
            }

            Toast.makeText(this, "Zapisano zmiany i odświeżono godziny", Toast.LENGTH_SHORT).show()
            recreate()
        }

        backArrow.setOnClickListener {
            finish()
        }
    }

    private fun obliczGodzine(offset: String, wakeTime: Boolean): String {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        val baseTime = if (wakeTime)
            schedulePrefs.getString("wake_time", "07:00")
        else
            schedulePrefs.getString("sleep_time", "22:00")

        return try {
            val calendar = Calendar.getInstance()
            calendar.time = format.parse(baseTime!!) ?: Date()

            val offsetMinutes = when (offset.first()) {
                '+' -> offset.drop(1).toInt()
                '-' -> -offset.drop(1).toInt()
                else -> 0
            }

            calendar.add(Calendar.MINUTE, offsetMinutes)
            format.format(calendar.time)
        } catch (e: Exception) {
            "--:--"
        }
    }
}
