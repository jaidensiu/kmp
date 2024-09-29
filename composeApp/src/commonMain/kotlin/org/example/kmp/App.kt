package org.example.kmp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kmp.composeapp.generated.resources.Res
import kmp.composeapp.generated.resources.compose_multiplatform
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        var showWondons by remember { mutableStateOf(false) }
        var wondons by remember { mutableStateOf(listOf<String>()) }

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            Button(
                onClick = {
                    showWondons = !showWondons
                    if (showWondons) {
                        fetchWondons { wondons = it }
                    }
                }
            ) {
                if (wondons.isEmpty()) {
                    Text(
                        text = "get wondons!"
                    )
                } else {
                    Text(
                        text = "get more wondons?"
                    )
                }
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: $greeting")
                }
            }
            AnimatedVisibility(showWondons) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    wondons.forEach { wondon ->
                        Text(wondon)
                    }
                }
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun fetchWondons(onSuccess: (List<String>) -> Unit) {
    val client = HttpClient()
    GlobalScope.launch(Dispatchers.Main) {
        try {
            val response = client.get("http://localhost:8080/wondons")
            if (response.status.isSuccess()) {
                val list: List<String> = response.body()
                onSuccess(list)
            } else {
                onSuccess(emptyList()) // Return an empty list if not successful
                println("Error: Received response status ${response.status}")
            }
        } catch (e: Exception) {
            onSuccess(emptyList()) // Return an empty list on error
            println("Error: ${e.message}")
        } finally {
            client.close()
        }
    }
}
