package com.example.kotlinprojectmark2.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dtFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())

fun formatInstant(i: Instant?): String = i?.let { dtFormatter.format(it) } ?: "-"

fun prettyElapsed(ms: Long): String {
    val s = ms / 1000
    val hours = s / 3600
    val mins = (s % 3600) / 60
    val secs = s % 60
    return String.format("%02d:%02d:%02d", hours, mins, secs)
}
