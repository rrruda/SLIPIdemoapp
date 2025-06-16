package com.example.slipi1

data class WpisSnu(
    val data: String = "",
    val czasOd: String = "",
    val czasDo: String = "",
    val pobudki: Int = 0,
    val drzemki: Int = 0,
    val jakosc: Int = 0,
    val kofeina: Boolean = false,
    val alkohol: Boolean = false,
    val telefon: Boolean = false,
    val posilek: Boolean = false,
    val aktywnosc: Boolean = false,
    val wyciszenie: Boolean = false,
    val timestamp: Long = 0
)
