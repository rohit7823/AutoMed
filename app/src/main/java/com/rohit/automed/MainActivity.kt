package com.rohit.automed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rohit.automed.custom_composable.AutoCompleteSearchBar
import com.rohit.automed.data.Value
import com.rohit.automed.ui.theme.AutoMedTheme

class MainActivity : ComponentActivity() {

    private var query by mutableStateOf("")
    private var results by mutableStateOf(listOf<Value>())
    private var selectedValue by mutableStateOf<Value?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutoMedTheme {
                val conf = LocalConfiguration.current
                var mutableModifier by remember {
                    mutableStateOf(
                        Modifier
                            .background(Color.White)
                            .width(conf.screenWidthDp.dp * 0.9f)
                            .wrapContentHeight()
                            .padding(12.dp)
                            .border(width = 2.dp, color = Color.LightGray)
                    )
                }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AutoCompleteSearchBar(
                        modifier = Modifier
                            .width(conf.screenWidthDp.dp * 0.9f)
                            .wrapContentHeight()
                            .padding(0.dp, 12.dp, 0.dp, 0.dp),
                        trailingIcon = Icons.Default.Search,
                        userInput = query,
                        onUserInputChange = { query = it },
                        queryResults = results,
                        onQueryResultsChange = {
                            results = it
                            if (it.size > 5) {
                                mutableModifier = Modifier
                                    .background(Color.White)
                                    .width(conf.screenWidthDp.dp * 0.9f)
                                    .heightIn(min = 0.dp, max = conf.screenWidthDp.dp * 0.6f)
                                    .border(width = 2.dp, color = Color.LightGray)
                            }
                        }
                    )

                    // user have to follow this layout structure
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        //region your composable content will go here
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_background),
                            contentDescription = "dummy",
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center),
                        )

                        if (selectedValue != null) {
                            Card(
                                modifier = Modifier.align(Alignment.Center),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 7.dp
                                ),
                                shape = CardDefaults.elevatedShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                )
                            ) {
                                Text(
                                    text = selectedValue?.value ?: "",
                                    modifier = Modifier
                                        .padding(12.dp)
                                )
                            }
                        }
                        //endregion

                        //region suggestion list will generate here
                        GenerateSuggestionList(
                            results = results,
                            query = query,
                            modifier = mutableModifier,
                            onSelect = ::onSelectValue
                        )
                        //endregion
                    }
                }
            }
        }
    }

    private fun onSelectValue(value: Value) {
        selectedValue = value
        query = ""
        results = emptyList()
    }
}

@Composable
fun GenerateSuggestionList(
    results: List<Value>,
    query: String,
    modifier: Modifier,
    onSelect: ((value: Value) -> Unit)? = null
) {
    if (results.isNotEmpty() && query != "") {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
            shadowElevation = 7.dp
        ) {
            LazyColumn {
                items(
                    items = results,
                    key = { result -> result.hashCode() }
                ) {
                    val annotatedString = buildAnnotatedString {
                        it.value.map { ch ->
                            if (query.contains(ch, ignoreCase = true)) {
                                withStyle(
                                    style = SpanStyle(
                                        color = Color.Red,
                                        fontWeight = FontWeight.ExtraBold,
                                    )
                                ) {
                                    append(ch)
                                }
                            } else {
                                append(ch)
                            }
                        }
                    }
                    Text(
                        text = annotatedString,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect?.invoke(it)
                            }
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}
