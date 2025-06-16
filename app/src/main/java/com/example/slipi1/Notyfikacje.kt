package com.example.slipi1

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.text.SimpleDateFormat
import java.util.*

class Notyfikacje : BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("notification_type") ?: return
        val message = when (type) {
            "sleep" -> "Pora się wyciszyć – przygotuj się do snu 💤"
            "wake" -> "Czas wypełnić sennik – zapisz, jak Ci się spało!"
            "missing" -> "Nie wypełniłeś sennika od 3 dni 😴 Zacznij znowu monitorować swój sen!"
            else -> return
        }

        val notification = NotificationCompat.Builder(context, "SLIPI_CHANNEL")
            .setSmallIcon(R.drawable.bear_moon)
            .setContentTitle("SLIPI")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(Random().nextInt(), notification)
    }
}
