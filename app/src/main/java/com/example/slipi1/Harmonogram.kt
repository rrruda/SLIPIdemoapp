package com.example.slipi1

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class Harmonogram : AppCompatActivity() {

    private lateinit var timeWake: EditText
    private lateinit var timeSleep: EditText
    private lateinit var saveButton: Button
    private lateinit var backArrow: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_harmonogram)
        supportActionBar?.hide()

        timeWake = findViewById(R.id.wakeUpTime)
        timeSleep = findViewById(R.id.sleepTime)
        saveButton = findViewById(R.id.saveButton)
        backArrow = findViewById(R.id.backArrow)

        val prefs = getSharedPreferences("SchedulePrefs", MODE_PRIVATE)
        timeWake.setText(prefs.getString("wake_time", ""))
        timeSleep.setText(prefs.getString("sleep_time", ""))

        saveButton.setOnClickListener {
            val wake = timeWake.text.toString()
            val sleep = timeSleep.text.toString()

            if (wake.isBlank() || sleep.isBlank()) {
                Toast.makeText(this, "Podaj oba czasy", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Zapis do SharedPreferences
            prefs.edit().apply {
                putString("wake_time", wake)
                putString("sleep_time", sleep)
                apply()
            }

            // Zapis do Firebase
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val ref = FirebaseDatabase.getInstance().getReference("Harmonogram").child(userId)
            ref.setValue(mapOf("wake_time" to wake, "sleep_time" to sleep))

            // Ustaw powiadomienia
            ustawPowiadomienia(sleep, wake)

            Toast.makeText(this, "Harmonogram zapisany!", Toast.LENGTH_SHORT).show()
            finish()
        }

        backArrow.setOnClickListener {
            finish()
        }
    }

    private fun ustawPowiadomienia(sleepTime: String, wakeTime: String) {
        val prefs = getSharedPreferences("NotificationPrefs", MODE_PRIVATE)
        val showSleepReminder = prefs.getBoolean("sleepReminder", true)
        val showWakeReminder = prefs.getBoolean("wakeReminder", true)
        val showMissingReminder = prefs.getBoolean("missingReminder", true)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance()

        // Wieczorne powiadomienie – godzina przed snem
        if (showSleepReminder) {
            val parsedSleep = timeFormat.parse(sleepTime)
            parsedSleep?.let {
                calendar.time = it
                calendar.add(Calendar.HOUR_OF_DAY, -1)
                planujAlarm(alarmManager, calendar.clone() as Calendar, "sleep", 101)
            }
        }

        // Poranne powiadomienie – 15 minut po pobudce
        if (showWakeReminder) {
            val parsedWake = timeFormat.parse(wakeTime)
            parsedWake?.let {
                calendar.time = it
                calendar.add(Calendar.MINUTE, 15)
                planujAlarm(alarmManager, calendar.clone() as Calendar, "wake", 102)
            }
        }

        // Powiadomienie o braku wpisów – codziennie o 9:00
        if (showMissingReminder) {
            calendar.set(Calendar.HOUR_OF_DAY, 9)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            planujAlarm(alarmManager, calendar.clone() as Calendar, "missing", 103, true)
        }
    }

    private fun planujAlarm(
        alarmManager: AlarmManager,
        time: Calendar,
        type: String,
        requestCode: Int,
        powtarzalne: Boolean = false
    ) {
        val intent = Intent(this, Notyfikacje::class.java).apply {
            action = when (type) {
                "sleep" -> "REMINDER_SLEEP"
                "wake" -> "REMINDER_FILL"
                "missing" -> "REMINDER_INACTIVE"
                else -> null
            }
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Jeśli czas już minął dziś, ustaw na jutro
        if (time.timeInMillis <= System.currentTimeMillis()) {
            time.add(Calendar.DAY_OF_YEAR, 1)
        }

        if (powtarzalne) {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                time.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                time.timeInMillis,
                pendingIntent
            )
        }
    }
}
