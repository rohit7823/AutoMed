package com.rohit.automed.custom_composable

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rohit.automed.data.ResultData
import com.rohit.automed.data.Value
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds


const val CONNECTION_TIMEOUT = 60_000


@OptIn(FlowPreview::class)
@Composable
fun AutoCompleteSearchBar(
    modifier: Modifier,
    trailingIcon: ImageVector,
    countryCode: String = "IN",
    userInput: String,
    onUserInputChange: (String) -> Unit,
    queryResults: List<Value>,
    onQueryResultsChange: (List<Value>) -> Unit,
) {
    var loadingState by remember {
        mutableStateOf(false)
    }
    var query by remember(userInput) {
        mutableStateOf(userInput)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = userInput,
            onValueChange = { input ->
                query = input
                onUserInputChange(input)
                if (input == "") {
                    onQueryResultsChange(emptyList())
                }
            },
            placeholder = { Text(text = "Search..") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                if (loadingState) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Red,
                        strokeCap = StrokeCap.Round
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(3.dp)

                )
                .fillMaxWidth(),

        )

    }


    LaunchedEffect(query) {
        snapshotFlow { query }.debounce(500.milliseconds).filter { it.isNotEmpty() }
            .collectLatest { input ->

                loadingState = true

                val defJob = async(Dispatchers.IO) {
                    getResults(input, countryCode)
                }

                defJob.await()?.suggestions?.let { list ->
                    if (list.isNotEmpty()) {
                        onQueryResultsChange(list.subList(0, list.size - 1))
                    }
                }

                defJob.invokeOnCompletion {
                    loadingState = false
                }
            }
    }
}

suspend fun getResults(query: String, countryCode: String): ResultData? {
    return try {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(
                    json = Json.Default,
                    contentType = ContentType.Application.Json
                )
            }
        }.get {
            url {
                host = "www.mims.com"
                path("autocomplete")
                protocol = URLProtocol.HTTPS
                parameter("countryCode", countryCode)
                parameter("query", query)
            }
        }.body()
    } catch (ex: Exception) {
        ex.message?.let { Log.d("exception", it) }
        return null
    }
}

