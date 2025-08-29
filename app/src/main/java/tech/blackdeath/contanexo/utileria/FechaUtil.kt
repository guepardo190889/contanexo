package tech.blackdeath.contanexo.utileria

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


/**
 * Fecha simple local YYYY-MM-DD (compat API 24).
 */
fun formatDateCompat(epochUtc: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    // mostramos en zona local del usuario
    sdf.timeZone = TimeZone.getDefault()
    return sdf.format(Date(epochUtc))
}