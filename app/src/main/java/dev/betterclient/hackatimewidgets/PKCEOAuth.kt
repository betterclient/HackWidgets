package dev.betterclient.hackatimewidgets

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import kotlin.random.asKotlinRandom

val Context.dataStore by preferencesDataStore(name = "token_store")

fun getToken(context: Context): Flow<String?> {
    val tokenKey = stringPreferencesKey("hackatime_token")
    return context.dataStore.data.map { preferences ->
        preferences[tokenKey]
    }
}

suspend fun clearToken(context: Context) {
    val tokenKey = stringPreferencesKey("hackatime_token")
    context.dataStore.edit { preferences ->
        preferences.remove(tokenKey)
    }
}

suspend fun setToken(context: Context, token: String) {
    val tokenKey = stringPreferencesKey("hackatime_token")

    context.dataStore.edit { preferences ->
        preferences[tokenKey] = token
    }
}

class PKCEOAuth(
    private val onFinish: (AuthorizationOutput?, String?) -> Unit,
    onLink: (String) -> Unit
) {
    companion object {
        private val secureRandom = SecureRandom().asKotlinRandom()
        const val CLIENT_ID = "N7vkH8obTyMoGNE6KJby6yAs2fRG1MpH25QhmPjyrpE"
        private const val REDIRECT_URI = "hackwidget://callback"
    }

    private val codeVerifier: String = (1..(43..128).random(secureRandom))
        .map { (('a'..'z') + ('A'..'Z') + ('0'..'9') + listOf('-', '.', '_', '~')).random(secureRandom) }
        .joinToString("")
    private val state: String = String("*".repeat(67).map { (97..122).random().toChar() }.toCharArray())

    init {
        val sha256 = MessageDigest.getInstance("SHA-256").digest(codeVerifier.toByteArray())
        val b64 = Base64.getUrlEncoder().withoutPadding().encodeToString(sha256)

        val parameters = mutableMapOf<String, String>()
        parameters["client_id"] = CLIENT_ID
        parameters["redirect_uri"] = REDIRECT_URI
        parameters["response_type"] = "code"
        parameters["scope"] = "profile read"
        parameters["state"] = state
        parameters["code_challenge"] = b64
        parameters["code_challenge_method"] = "S256"

        val link = "https://hackatime.hackclub.com/oauth/authorize?" + parameters.map {
                (k, v) -> "$k=${URLEncoder.encode(v, StandardCharsets.UTF_8)}"
        }.joinToString("&")
        onLink(link)
    }

    suspend fun handleRedirect(uri: Uri) {
        val code = uri.getQueryParameter("code")
        val returnedState = uri.getQueryParameter("state")

        if (uri.getQueryParameter("error") != null) {
            onFinish(null, "Authorization denied")
            return
        }

        if (returnedState != state) {
            onFinish(null, "State mismatch")
            return
        }

        if (code != null) {
            exchangeCodeForToken(code)
        }
    }

    private suspend fun exchangeCodeForToken(code: String) {
        withContext(Dispatchers.IO) {
            try {
                val params = mutableMapOf<String, String>()
                params["grant_type"] = "authorization_code"
                params["client_id"] = CLIENT_ID
                params["code"] = code
                params["redirect_uri"] = REDIRECT_URI
                params["code_verifier"] = codeVerifier

                val postData = params.map { (k, v) ->
                    "${k}=${URLEncoder.encode(v, StandardCharsets.UTF_8)}"
                }.joinToString("&")

                val connection = URL("https://hackatime.hackclub.com/oauth/token").openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                connection.outputStream.use { os ->
                    os.write(postData.toByteArray(StandardCharsets.UTF_8))
                }

                val responseCode = connection.responseCode
                val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
                val out = stream.bufferedReader().use { it.readText() }

                if (responseCode in 200..299) {
                    onFinish(Json.decodeFromString(out), null)
                } else {
                    onFinish(null, "Token exchange failed: $out")
                }
            } catch (e: Exception) {
                onFinish(null, e.message)
            }
        }
    }
}

@Serializable
class AuthorizationOutput(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("expires_in")
    val expiresIn: Int,
    val scope: String,
    @SerialName("created_at")
    val createdAt: Int
)