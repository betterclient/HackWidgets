package dev.betterclient.hackatimewidgets.api

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class HoursResponse(
    val start_date: LocalDate,
    val end_date: LocalDate,
    val total_seconds: Int
)
