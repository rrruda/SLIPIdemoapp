package com.example.slipi1

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EkranGlowny : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        supportActionBar?.hide()



        findViewById<Button>(R.id.addEntryButton).setOnClickListener {
            val intent = Intent(this, DodanieEdycjaWpisu::class.java)
            intent.putExtra("tryb", "dodawanie")
            startActivity(intent)
        }

        findViewById<Button>(R.id.editEntryButton).setOnClickListener {
            val intent = Intent(this, DodanieEdycjaWpisu::class.java)
            intent.putExtra("tryb", "edycja")
            startActivity(intent)
        }

        findViewById<Button>(R.id.historyButton).setOnClickListener {
            startActivity(Intent(this, HistoriaSnu::class.java))
        }

        findViewById<Button>(R.id.statisticsButton).setOnClickListener {
            startActivity(Intent(this, Statystyki::class.java))
        }

        findViewById<ImageView>(R.id.settingsIcon).setOnClickListener {
            startActivity(Intent(this, Ustawienia::class.java))
        }

        findViewById<ImageView>(R.id.backArrow).setOnClickListener {
            finish()
        }
    }

}
