package dev.betterclient.hackatimewidgets.api

import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import dev.betterclient.hackatimewidgets.MainActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

suspend fun MainActivity.launchAuth() {
    val token = getToken(this).first()
    if (token == null) {
        activeAuth = PKCEOAuth(
            onFinish = { output, error ->
                if (output != null) {
                    lifecycleScope.launch {
                        setToken(
                            this@launchAuth,
                            output.accessToken
                        )
                    }
                    startWidgetUI(output.accessToken)
                } else {
                    Toast.makeText(
                        this,
                        error,
                        Toast.LENGTH_SHORT
                    ).show()
                    exitProcess(0)
                }
            },
            onLink = { link ->
                val customTabsIntent = CustomTabsIntent.Builder().build()
                customTabsIntent.launchUrl(this, link.toUri())
            }
        )
    } else {
        startWidgetUI(token)
    }
}