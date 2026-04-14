package dev.betterclient.hackatimewidgets

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.forms.submitForm
import io.ktor.http.parameters

class Api(private val token: String) {
    val client = HttpClient(OkHttp)

    suspend fun revoke() {
        client.submitForm(
            url = "https://hackatime.hackclub.com/oauth/revoke",
            formParameters = parameters {
                append("token", token)
                append("client_id", PKCEOAuth.CLIENT_ID)
            }
        )
    }
}