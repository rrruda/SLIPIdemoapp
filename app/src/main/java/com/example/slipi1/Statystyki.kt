package com.example.slipi1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class Statystyki : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_okno_statystyki)
        supportActionBar?.hide()

        findViewById<ImageView>(R.id.backArrow)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        val button7dni = findViewById<Button>(R.id.button7dni)
        val button30dni = findViewById<Button>(R.id.button30dni)

        button7dni.setOnClickListener {
            startActivity(Intent(this, Statystyki7dni::class.java))
        }

        button30dni.setOnClickListener {
            startActivity(Intent(this, Statystyki30dni::class.java))
        }
    }
}
