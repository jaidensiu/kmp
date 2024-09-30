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
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kmp.composeapp.generated.resources.Res
import kmp.composeapp.generated.resources.compose_multiplatform
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        var showWondons by remember { mutableStateOf(false) }
        var wondonButtonText by remember { mutableStateOf("") }

        val wondonsFlow = remember {
            flow {
                emit(emptyList())
                emit(getWondons())
            }
        }
        val wondons by wondonsFlow.collectAsState(initial = emptyList())

        LaunchedEffect(showWondons) {
            if (showWondons) {
                wondonButtonText = if (wondons.isEmpty()) "Get more wondons?" else "Refresh wondons"
            } else {
                wondonButtonText = "Get wondons!"
            }
        }

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            Button(
                onClick = {
                    showWondons = !showWondons
                }
            ) {
                Text(
                    text = wondonButtonText
                )
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
                    Text(
                        text = "Wondons:"
                    )
                    wondons.forEach { wondon ->
                        Text(wondon)
                    }
                }
            }
        }
    }
}

suspend fun getWondons(): List<String> {
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    return try {
        val response: HttpResponse = client.get("http://localhost:8080/wondons")
        val responseText = response.bodyAsText()
        Json.decodeFromString<List<String>>(responseText)
    } catch (e: Exception) {
        println("Error fetching wondons: ${e.message}")
        e.printStackTrace()
        emptyList()
    } finally {
        client.close()
    }
}
