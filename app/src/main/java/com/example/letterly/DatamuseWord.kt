package com.example.letterly

import com.google.gson.annotations.SerializedName

data class DatamuseWord(
    @SerializedName("word") val word: String,
    @SerializedName("score") val score: Int,
    @SerializedName("defs") val definitions: List<String>? = null
)
