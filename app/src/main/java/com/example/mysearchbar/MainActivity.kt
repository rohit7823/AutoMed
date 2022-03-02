package com.example.mysearchbar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.max
import com.example.mysearchbar.custom_composable.AutoCompleteSearchBar
import com.example.mysearchbar.data.Value
import com.example.mysearchbar.ui.theme.MySearchbarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val conf = LocalConfiguration.current
            var query by remember { mutableStateOf("") }
            var results by remember { mutableStateOf(listOf<Value>()) }

            var mutableModifier by remember {
                mutableStateOf(Modifier
                    .background(Color.White)
                    .width(conf.screenWidthDp.dp * 0.9f)
                    .wrapContentHeight()
                    .padding(12.dp)
                    .border(width = 2.dp, color = Color.LightGray))
            }


            MySearchbarTheme {
                Column(modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally) {
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
                                    .padding(10.dp)
                            }
                        }
                    )

                    // user have to follow this layout structure
                    Box(modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter) {
                        //region your composable content will go here
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_background),
                                contentDescription = "dummy",
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        //endregion

                        //region suggestion list will generate here
                        GenerateSuggestionList(
                            results = results,
                            query = query,
                            modifier = mutableModifier
                        )
                        //endregion
                    }
                }
            }
        }
    }
}

@Composable
fun GenerateSuggestionList(results: List<Value>, query: String, modifier: Modifier) {
    if (results.isNotEmpty() && query != "") {
        Surface(
            modifier = modifier,
        ) {
            LazyColumn {
                items(results) {
                    val annotatedString = buildAnnotatedString {
                        it.value.forEach { ch ->
                            if (query.contains(ch, ignoreCase = true)) {
                                withStyle(style = SpanStyle(color = Color.Red,
                                    fontWeight = FontWeight.Bold)) {
                                    append(ch)
                                }
                            } else {
                                append(ch)
                            }
                        }
                    }
                    Text(
                        text = annotatedString,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = false)
@Composable
fun DefaultPreview() {
    MySearchbarTheme {
    }
}