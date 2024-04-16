package com.rohit.automed.data

import kotlinx.serialization.Serializable

@Serializable
data class ResultData(
   val query: String,
   val suggestions: List<Value>
)
