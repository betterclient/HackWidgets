package dev.betterclient.hackatimewidgets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.betterclient.hackatimewidgets.ui.theme.HackatimeWidgetsTheme
import java.net.HttpURLConnection
import java.net.ServerSocket
import java.net.Socket
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import kotlin.random.Random
import kotlin.random.asKotlinRandom

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HackatimeWidgetsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HackatimeWidgetsTheme {
        Greeting("Android")
    }
}

val secureRandom = SecureRandom().asKotlinRandom()
val clientId = ""

fun main() {
    val codeVerifier = (1..(43..128).random(secureRandom))
        .map { (('a'..'z') + ('A'..'Z') + ('0'..'9') + listOf('-', '.', '_', '~')).random(secureRandom) }
        .joinToString("")
    val state = String("*".repeat(67).map { (97..122).random().toChar() }.toCharArray())

    val sha256 = MessageDigest.getInstance("SHA-256").digest(codeVerifier.toByteArray())
    val b64 = Base64.getUrlEncoder().withoutPadding().encodeToString(sha256)

    val parameters = mutableMapOf<String, String>()
    parameters["client_id"] = clientId
    parameters["redirect_uri"] = "http://localhost:9999"
    parameters["response_type"] = "code"
    parameters["scope"] = "profile read"
    parameters["state"] = state
    parameters["code_challenge"] = b64
    parameters["code_challenge_method"] = "S256"

    val link = "https://hackatime.hackclub.com/oauth/authorize?" + parameters.map {
        (k, v) -> "$k=${URLEncoder.encode(v, StandardCharsets.UTF_8)}"
    }.joinToString("&")
    println("Go to $link")

    val out = receiveDataFromServer()
    val outParameters = out
        .lines()
        .first()
        .substringAfter("GET /?")
        .substringBeforeLast("HTTP")
        .split("&")
        .mapNotNull { segment ->
            val parts = segment.split("=", limit = 2)
            if (parts.size == 2) parts[0] to parts[1] else null
        }
        .toMap()

    if (outParameters.containsKey("error")) {
        throw RuntimeException("Authorization denied")
    }

    if (outParameters["state"]?.contains(state) == false) {
        //invalid
        throw RuntimeException("Provided state doesn't match input state")
    }

    val code = outParameters["code"]!!

    val params = mutableMapOf<String, String>()
    params["grant_type"] = "authorization_code"
    params["client_id"] = clientId
    params["code"] = code
    params["redirect_uri"] = "http://localhost:9999"
    params["code_verifier"] = codeVerifier

    val postData = params.map { (k, v) ->
        "${k}=${URLEncoder.encode(v, StandardCharsets.UTF_8)}"
    }.joinToString("&")

    val connection = URL("https://hackatime.hackclub.com/oauth/token").openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.doOutput = true // Required to send a body
    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
    connection.outputStream.use { os ->
        os.write(postData.toByteArray(StandardCharsets.UTF_8))
    }

    println(String(connection.inputStream.readAllBytes()))
}

fun receiveDataFromServer(): String {
    val server = ServerSocket(9999)

    val out: String
    server.accept().use { client ->
        out = String(client.inputStream.readAllBytes())
        val output = client.getOutputStream()
        output.write("You're good to close this now.\n".toByteArray())
    }

    server.close()
    return out
}