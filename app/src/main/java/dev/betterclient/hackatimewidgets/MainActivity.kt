package dev.betterclient.hackatimewidgets

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import dev.betterclient.hackatimewidgets.api.Api
import dev.betterclient.hackatimewidgets.ui.AddWidgetsUI
import dev.betterclient.hackatimewidgets.ui.setContent0
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    private var activeAuth: PKCEOAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        startAuthFlow()
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    fun startAuthFlow() {
        setContent0 {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("Authenticating...")
                Text("Please restart app if stuck here")
                CircularWavyProgressIndicator()
            }
        }

        lifecycleScope.launch {
            val token = getToken(this@MainActivity).first()
            if (token == null) {
                activeAuth = PKCEOAuth(
                    onFinish = { output, error ->
                        if (output != null) {
                            lifecycleScope.launch { setToken(this@MainActivity, output.accessToken) }
                            setContent0 {
                                AddWidgetsUI(Api(output.accessToken), this@MainActivity)
                            }
                        } else {
                            Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
                            exitProcess(0)
                        }
                    },
                    onLink = { link ->
                        val customTabsIntent = CustomTabsIntent.Builder().build()
                        customTabsIntent.launchUrl(this@MainActivity, link.toUri())
                    }
                )
            } else {
                setContent0 {
                    AddWidgetsUI(Api(token), this@MainActivity)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val uri = intent.data
        if (uri != null && uri.scheme == "hackwidget") {
            lifecycleScope.launch {
                activeAuth?.handleRedirect(uri)
            }
        }
    }
}