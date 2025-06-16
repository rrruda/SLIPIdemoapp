package com.example.slipi1

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class DodanieEdycjaWpisu : AppCompatActivity() {

    private lateinit var timeFrom: EditText
    private lateinit var timeTo: EditText
    private lateinit var wakeups: EditText
    private lateinit var naps: EditText
    private lateinit var quality: SeekBar
    private lateinit var alkohol: CheckBox
    private lateinit var kofeina: CheckBox
    private lateinit var telefon: CheckBox
    private lateinit var posilek: CheckBox
    private lateinit var aktywnosc: CheckBox
    private lateinit var wyciszenie: CheckBox
    private lateinit var dataTextView: TextView
    private lateinit var chooseDateButton: Button

    private lateinit var wybranaData: String
    private var czyTrybEdycji = false

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dodanie_wpisu)
        supportActionBar?.hide()

        findViewById<ImageView>(R.id.backArrow)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        timeFrom = findViewById(R.id.timeFrom)
        timeTo = findViewById(R.id.timeTo)
        wakeups = findViewById(R.id.wakeups)
        naps = findViewById(R.id.naps)
        quality = findViewById(R.id.sleepQualitySeekBar)
        alkohol = findViewById(R.id.checkbox_alkohol)
        kofeina = findViewById(R.id.checkbox_kofeina)
        telefon = findViewById(R.id.checkbox_telefon)
        posilek = findViewById(R.id.checkbox_posilek)
        aktywnosc = findViewById(R.id.checkbox_aktywnosc)
        wyciszenie = findViewById(R.id.checkbox_wyciszenie)
        dataTextView = findViewById(R.id.dataTextView)
        chooseDateButton = findViewById(R.id.datePickerButton)

        val tryb = intent.getStringExtra("tryb") ?: "dodawanie"
        val dataZIntentu = intent.getStringExtra("data")
        czyTrybEdycji = tryb == "edycja"
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        wybranaData = if (czyTrybEdycji && dataZIntentu != null) {
            dataZIntentu
        } else {
            sdf.format(Date()) // tylko dziś w trybie dodawania
        }

        dataTextView.text = "Data: $wybranaData"

        // Widoczność przycisku daty
        if (czyTrybEdycji) {
            chooseDateButton.visibility = Button.VISIBLE
        } else {
            chooseDateButton.visibility = Button.GONE
        }

        // Obsługa przycisku WYBIERZ DATĘ
        chooseDateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                wybranaData = String.format("%04d-%02d-%02d", year, month + 1, day)
                dataTextView.text = "Data: $wybranaData"

                // Wyczyść dane z pól
                timeFrom.setText("")
                timeTo.setText("")
                wakeups.setText("")
                naps.setText("")
                quality.progress = 2
                alkohol.isChecked = false
                kofeina.isChecked = false
                telefon.isChecked = false
                posilek.isChecked = false
                aktywnosc.isChecked = false
                wyciszenie.isChecked = false

                zaladujDaneZFirestore()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Wczytaj dane, jeśli tryb edycji
        if (czyTrybEdycji) {
            zaladujDaneZFirestore()
        }

        // Przycisk ZAPISZ
        val layout = findViewById<LinearLayout>(R.id.factorsLayout).parent as LinearLayout
        val saveButton = Button(this).apply {
            text = "Zapisz"
            setBackgroundColor(Color.parseColor("#00BCD4"))
            setTextColor(Color.WHITE)
        }

        saveButton.setOnClickListener {
            val czasOd = normalizujGodzine(timeFrom.text.toString())
            val czasDo = normalizujGodzine(timeTo.text.toString())

            if (!isPoprawnyFormat(czasOd) || !isPoprawnyFormat(czasDo)) {
                Toast.makeText(this, "Nieprawidłowy format godziny (np. 22:30)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val wpis = hashMapOf(
                "data" to wybranaData,
                "czasOd" to czasOd,
                "czasDo" to czasDo,
                "pobudki" to (wakeups.text.toString().toIntOrNull() ?: 0),
                "drzemki" to (naps.text.toString().toIntOrNull() ?: 0),
                "jakosc" to (quality.progress + 1),
                "alkohol" to alkohol.isChecked,
                "kofeina" to kofeina.isChecked,
                "telefon" to telefon.isChecked,
                "posilek" to posilek.isChecked,
                "aktywnosc" to aktywnosc.isChecked,
                "wyciszenie" to wyciszenie.isChecked,
                "timestamp" to System.currentTimeMillis()
            )

            val userId = auth.currentUser?.uid ?: return@setOnClickListener

            firestore.collection("WpisySnu")
                .document(userId)
                .collection("Dni")
                .document(wybranaData)
                .set(wpis)
                .addOnSuccessListener {
                    Toast.makeText(this, "Zapisano wpis!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Błąd zapisu: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        layout.addView(saveButton)
    }

    private fun zaladujDaneZFirestore() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("WpisySnu")
            .document(userId)
            .collection("Dni")
            .document(wybranaData)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    timeFrom.setText(document.getString("czasOd") ?: "")
                    timeTo.setText(document.getString("czasDo") ?: "")
                    wakeups.setText((document.getLong("pobudki") ?: 0).toString())
                    naps.setText((document.getLong("drzemki") ?: 0).toString())
                    quality.progress = ((document.getLong("jakosc") ?: 1).toInt() - 1).coerceIn(0, 4)

                    alkohol.isChecked = document.getBoolean("alkohol") ?: false
                    kofeina.isChecked = document.getBoolean("kofeina") ?: false
                    telefon.isChecked = document.getBoolean("telefon") ?: false
                    posilek.isChecked = document.getBoolean("posilek") ?: false
                    aktywnosc.isChecked = document.getBoolean("aktywnosc") ?: false
                    wyciszenie.isChecked = document.getBoolean("wyciszenie") ?: false
                }
            }
    }

    private fun normalizujGodzine(input: String): String {
        val trimmed = input.trim()
        return when {
            trimmed.matches(Regex("^\\d{1,2}$")) -> "$trimmed:00"
            trimmed.matches(Regex("^\\d{1,2}\\.$")) -> trimmed.dropLast(1) + ":00"
            trimmed.matches(Regex("^\\d{1,2}[.](\\d{1,2})$")) -> trimmed.replace(".", ":")
            trimmed.matches(Regex("^\\d{1,2}:[0-5]\\d$")) -> trimmed
            else -> trimmed
        }
    }

    private fun isPoprawnyFormat(godzina: String): Boolean {
        return godzina.matches(Regex("^([01]?\\d|2[0-3]):[0-5]\\d$"))
    }
}

//test
