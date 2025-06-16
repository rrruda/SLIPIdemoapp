package com.example.slipi1

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class Statystyki30dni : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private lateinit var firestore: FirebaseFirestore

    private lateinit var senText: TextView
    private lateinit var drzemkiText: TextView
    private lateinit var jakoscText: TextView
    private lateinit var pobudkiText: TextView
    private lateinit var alkoholText: TextView
    private lateinit var kofeinaText: TextView
    private lateinit var posilekText: TextView
    private lateinit var telefonText: TextView
    private lateinit var aktywnoscText: TextView
    private lateinit var wyciszenieText: TextView
    private lateinit var wnioskiText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statystyki_30dni)
        supportActionBar?.hide()

        findViewById<ImageView>(R.id.backArrow).setOnClickListener { finish() }

        // Widoki
        senText = findViewById(R.id.sredniaSnu)
        drzemkiText = findViewById(R.id.sredniaDrzemek)
        jakoscText = findViewById(R.id.sredniaJakosci)
        pobudkiText = findViewById(R.id.sredniaPobudek)
        alkoholText = findViewById(R.id.textAlkohol)
        kofeinaText = findViewById(R.id.textKofeina)
        posilekText = findViewById(R.id.textPosilek)
        telefonText = findViewById(R.id.textTelefon)
        aktywnoscText = findViewById(R.id.textAktywnosc)
        wyciszenieText = findViewById(R.id.textWyciszenie)
        wnioskiText = findViewById(R.id.textWnioski)

        val userId = auth.currentUser?.uid ?: return
        firestore = FirebaseFirestore.getInstance()

        firestore.collection("WpisySnu")
            .document(userId)
            .collection("Dni")
            .get()
            .addOnSuccessListener { result ->
                val today = Calendar.getInstance()
                val startDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -29) } // 30 dni

                val wpisy = result.documents.mapNotNull { it.toObject(WpisSnu::class.java) }
                    .filter {
                        val wpisDate = dateFormat.parse(it.data)
                        wpisDate != null && wpisDate >= startDate.time && wpisDate <= today.time
                    }

                if (wpisy.isNotEmpty()) {
                    val sredniSen = wpisy.map { obliczGodzinySnu(it.czasOd, it.czasDo) }.average()
                    val srednieDrzemki = wpisy.map { it.drzemki }.average()
                    val sredniaJakosc = wpisy.map { it.jakosc }.average()
                    val sredniePobudki = wpisy.map { it.pobudki }.average()

                    senText.text = "%.1f godzin".format(sredniSen)
                    drzemkiText.text = "%.0f minut".format(srednieDrzemki)
                    jakoscText.text = "%.1f / 5".format(sredniaJakosc)
                    pobudkiText.text = "${sredniePobudki.toInt()}"


                    val alkohol = wpisy.count { it.alkohol }
                    val kofeina = wpisy.count { it.kofeina }
                    val posilek = wpisy.count { it.posilek }
                    val telefon = wpisy.count { it.telefon }
                    val aktywnosc = wpisy.count { it.aktywnosc }
                    val wyciszenie = wpisy.count { it.wyciszenie }

                    alkoholText.text = "W ciągu ostatnich 30 dni piłeś/aś alkohol $alkohol razy."
                    kofeinaText.text = "Spożywałeś/aś napoje z kofeiną po 18:00 $kofeina razy."
                    posilekText.text = "Zjadłeś/aś późny posiłek $posilek razy."
                    telefonText.text = "Korzystałeś/aś z telefonu przed snem $telefon razy."
                    aktywnoscText.text = "Uprawiałeś/aś aktywność fizyczną $aktywnosc razy."
                    wyciszenieText.text = "Praktykowałeś/aś wyciszenie przed snem $wyciszenie razy."

                    // Generowanie wniosków
                    val ogranicz = mutableListOf<String>()
                    val zadbaj = mutableListOf<String>()

                    if (alkohol > 10) ogranicz.add("alkohol")
                    if (kofeina > 10) ogranicz.add("kofeinę po 18:00")
                    if (telefon > 10) ogranicz.add("korzystanie z telefonu przed snem")
                    if (posilek > 10) ogranicz.add("późne posiłki")

                    if (aktywnosc < 10) zadbaj.add("regularną aktywność fizyczną")
                    if (wyciszenie < 10) zadbaj.add("częstsze wyciszenie przed snem")

                    val wstep = "Na podstawie Twoich ostatnich 30 dni snu zauważyliśmy pewne nawyki, które mogą wpływać na jego jakość.\n"

                    val ograniczTekst = if (ogranicz.isNotEmpty())
                        "Postaraj się ograniczyć: ${ogranicz.joinToString(", ")}.\n" else ""

                    val zadbajTekst = if (zadbaj.isNotEmpty())
                        "Zadbaj o: ${zadbaj.joinToString(", ")}." else ""

                    val finalnaWiadomosc = if (ograniczTekst.isEmpty() && zadbajTekst.isEmpty()) {
                        "Na podstawie Twoich ostatnich 30 dni snu nie zauważyliśmy żadnych nawyków, które mogłyby negatywnie wpływać na jego jakość.\nŚwietna robota! Kontynuuj dobre praktyki – są świetną podstawą do zdrowego i regenerującego snu 😊"
                    } else {
                        "$wstep$ograniczTekst$zadbajTekst"
                    }

                    wnioskiText.text = finalnaWiadomosc
                } else {
                    senText.text = "-"
                    drzemkiText.text = "-"
                    jakoscText.text = "-"
                    pobudkiText.text = "-"
                    wnioskiText.text = "Brak danych z ostatnich 30 dni."
                }
            }
    }

    private fun obliczGodzinySnu(od: String, do_: String): Double {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        val t1 = format.parse(od)
        val t2 = format.parse(do_)
        if (t1 != null && t2 != null) {
            var duration = (t2.time - t1.time) / 1000.0 / 3600.0
            if (duration < 0) duration += 24.0
            return duration
        }
        return 0.0
    }
}
