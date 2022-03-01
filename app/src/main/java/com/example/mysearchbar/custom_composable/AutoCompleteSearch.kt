package com.example.mysearchbar.custom_composable

import android.inputmethodservice.Keyboard
import android.text.Html
import android.util.Log
import android.view.Surface
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotApplyResult
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextPainter
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mysearchbar.data.ResultData
import com.example.mysearchbar.data.Value
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


const val CONNECTION_TIMEOUT = 60_000


@Composable
fun AutoCompleteSearchBar(
    modifier: Modifier,
    trailingIcon: ImageVector,
    countryCode: String = "IN",
) {
    val scope = rememberCoroutineScope()
    var userInput by remember {
        mutableStateOf("")
    }
    var trailingIconState by remember {
        mutableStateOf(trailingIcon)
    }
    var queryResults by remember {
        mutableStateOf(listOf<Value>())
    }

    Surface(
        modifier = modifier,
        elevation = 5.dp
    ) {

        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedTextField(
                value = userInput,
                onValueChange = { query ->
                    userInput = query
                    scope.launch {
                        if (userInput == "") {
                            queryResults = emptyList()
                        }
                        trailingIconState = Icons.Default.Close

                        val df = async {
                            val result = getResults(userInput, countryCode)
                            result
                        }
                        df.await()?.suggestions?.let { list ->
                            if (list.isNotEmpty()) {
                                queryResults = list.subList(0, list.size - 1)
                            }
                        }

                        trailingIconState = Icons.Default.Search
                    }
                },
                placeholder = { Text(text = "Search..") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Done),
                trailingIcon = {
                    Icon(imageVector = trailingIconState,
                        contentDescription = "Search")
                },
                modifier = Modifier.border(width = 1.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(3.dp))
            )
            if (queryResults.isNotEmpty() && userInput.isNotEmpty()) {
                Column(modifier = modifier
                    .clip(RoundedCornerShape(3.dp)),
                    horizontalAlignment = Alignment.Start
                ) {
                    LazyColumn {
                        items(queryResults) {
                            if (it.value.contains(userInput, true)) {
                                val term = it.value
                                Log.d("testing", "User Input: $userInput Matched String: $userInput")
                                term.replace(userInput, "<font color='red'>$userInput</font>")
                                Text(text = term)
                            }
                        }
                    }
                }
            }
        }

    }
}

suspend fun getResults(query: String, countryCode: String): ResultData? {
    return try {
        HttpClient(Android) {
            install(JsonFeature) {
                serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                    prettyPrint = true
                    isLenient = false
                    ignoreUnknownKeys = true
                })
            }
            install(DefaultRequest) {
                host = "www.mims.com/"
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
        }.get {
            url("autocomplete?countryCode=${countryCode}&query=${query}")
        }
    } catch (ex: Exception) {
        ex.message?.let { Log.d("exception", it) }
        return null
    }
}

