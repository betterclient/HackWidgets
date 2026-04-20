package dev.betterclient.hackatimewidgets.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlin.time.Clock

class Api(val token: String) {
    val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun revoke() {
        client.submitForm(
            url = "https://hackatime.hackclub.com/oauth/revoke",
            formParameters = parameters {
                append("token", token)
                append("client_id", PKCEOAuth.Companion.CLIENT_ID)
            }
        )
    }

    suspend fun getCodeTime(
        start: LocalDate = now().minus(7, DateTimeUnit.DAY),
        end: LocalDate = now()
    ): HoursResponse {
        return client.get("https://hackatime.hackclub.com/api/v1/authenticated/hours") {
            bearerAuth(token)

            url {
                parameters.append("start_date", start.toString())
                parameters.append("end_date", end.toString())
            }
        }.body<HoursResponse>()
    }
}

fun now() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date