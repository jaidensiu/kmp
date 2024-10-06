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
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kmp.composeapp.generated.resources.Res
import kmp.composeapp.generated.resources.compose_multiplatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showWondons by remember { mutableStateOf(false) }
        var wondonButtonText by remember { mutableStateOf("") }
        var age by remember { mutableStateOf(2) }

        val wondonsFlow = remember {
            flow {
                emit(emptyList())
                emit(getWondons())
            }
        }
        val wondons by wondonsFlow.collectAsState(initial = emptyList())

        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(showWondons) {
            wondonButtonText = if (showWondons) {
                if (wondons.isEmpty()) "Get more wondons?" else "Refresh wondons"
            } else {
                "Get wondons!"
            }
        }

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = {
                    showWondons = !showWondons
                    coroutineScope.launch {
                        getWondons()
                    }
                }
            ) {
                Text(
                    text = wondonButtonText
                )
            }

            Button(
                onClick = {
                    age++
                    coroutineScope.launch {
                        val res = putWondon(Wondon(name = "Wondon$age", age = age))
                        println("PUT request result: $res")
                    }
                }
            ) {
                Text(
                    text = "Add wondon"
                )
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
                        Text("${wondon.name} is ${wondon.age}")
                    }
                }
            }
        }
    }
}

suspend fun getWondons(): List<Wondon> {
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
        Json.decodeFromString<List<Wondon>>(responseText)
    } catch (e: Exception) {
        println("Error fetching wondons: ${e.message}")
        e.printStackTrace()
        emptyList()
    } finally {
        client.close()
    }
}

suspend fun putWondon(wondon: Wondon): Boolean {
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    return try {
        withContext(Dispatchers.Default) {
            val response: HttpResponse = client.put("http://localhost:8080/wondons") {
                contentType(ContentType.Application.Json)
                setBody(wondon)
            }
            response.status == HttpStatusCode.Created
        }
    } catch (e: Exception) {
        println("Error adding wondon: ${e.message}")
        e.printStackTrace()
        false
    } finally {
        client.close()
    }
}
