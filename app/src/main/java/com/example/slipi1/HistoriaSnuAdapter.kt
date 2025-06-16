package com.example.slipi1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoriaSnuAdapter(private val listaWpisow: List<WpisSnu>) :
    RecyclerView.Adapter<HistoriaSnuAdapter.WpisViewHolder>() {

    class WpisViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dataText: TextView = itemView.findViewById(R.id.dateText)
        val czasSnuText: TextView = itemView.findViewById(R.id.sleepDuration)
        val seekBar: SeekBar = itemView.findViewById(R.id.sleepSeekBar)
        val pobudkiText: TextView = itemView.findViewById(R.id.wakeupsText)
        val drzemkiText: TextView = itemView.findViewById(R.id.napsText)
        val infoText: TextView = itemView.findViewById(R.id.infoText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WpisViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.element_historia_snu, parent, false)
        return WpisViewHolder(view)
    }

    override fun onBindViewHolder(holder: WpisViewHolder, position: Int) {
        val wpis = listaWpisow[position]

        // Data
        holder.dataText.text = wpis.data.ifEmpty { "Brak daty" }

        // Czas snu
        val czasOd = wpis.czasOd.ifEmpty { "?" }
        val czasDo = wpis.czasDo.ifEmpty { "?" }
        holder.czasSnuText.text = "$czasOd – $czasDo"

        // Jakość snu
        holder.seekBar.progress = wpis.jakosc
        holder.seekBar.isEnabled = false

        // Pobudki i drzemki
        holder.pobudkiText.text = "${wpis.pobudki} pobudki"
        holder.drzemkiText.text = "${wpis.drzemki} min drzemek"

        // Lista zachowań
        val info = mutableListOf<String>()
        if (wpis.kofeina) info.add("✓ Kofeina po 18:00")
        if (wpis.telefon) info.add("✓ Telefon przed snem")
        if (wpis.posilek) info.add("✓ Późny posiłek")
        if (wpis.aktywnosc) info.add("✓ Aktywność fizyczna")
        if (wpis.wyciszenie) info.add("✓ Wyciszenie przed snem")

        holder.infoText.text = if (info.isEmpty()) "Brak dodatkowych czynników" else info.joinToString("\n")
    }

    override fun getItemCount(): Int = listaWpisow.size
}
