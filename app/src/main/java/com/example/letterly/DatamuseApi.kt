package com.example.letterly

import retrofit2.http.GET
import retrofit2.http.Query

interface DatamuseApi {

    /** Pattern search: sp = spelledLike pattern; md = metadata (d = definitions) */
    @GET("words")
    suspend fun findWords(
        @Query("sp") spelledLike: String,
        @Query("md") metadata: String = "d",
        @Query("max") max: Int = 100
    ): List<DatamuseWord>

    /** Dictionary lookup: exact word, fetches definitions */
    @GET("words")
    suspend fun lookupWord(
        @Query("sp") word: String,
        @Query("md") metadata: String = "d",
        @Query("max") max: Int = 5
    ): List<DatamuseWord>

    /** Rhyme search: rel_rhy = perfect rhyme relation */
    @GET("words")
    suspend fun findRhymes(
        @Query("rel_rhy") word: String,
        @Query("md") metadata: String = "d",
        @Query("max") max: Int = 100
    ): List<DatamuseWord>

    /** Concept/Means-Like search: ml = means like (reverse dictionary) */
    @GET("words")
    suspend fun findByMeaning(
        @Query("ml") concept: String,
        @Query("md") metadata: String = "d",
        @Query("max") max: Int = 100
    ): List<DatamuseWord>
}
