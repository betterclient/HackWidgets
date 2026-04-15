package dev.betterclient.hackatimewidgets

import android.content.Context
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
import dev.betterclient.hackatimewidgets.api.PKCEOAuth
import dev.betterclient.hackatimewidgets.api.getToken
import dev.betterclient.hackatimewidgets.api.launchAuth
import dev.betterclient.hackatimewidgets.api.setToken
import dev.betterclient.hackatimewidgets.ui.AddWidgetsUI
import dev.betterclient.hackatimewidgets.ui.setContent0
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    var activeAuth: PKCEOAuth? = null

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
            launchAuth()
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

    fun startWidgetUI(token: String) {
        setContent0 {
            AddWidgetsUI(Api(token), this::startAuthFlow)
        }
    }
}

suspend fun getApi(context: Context): Api? {
    return getToken(context).first()?.let { Api(it) }
}