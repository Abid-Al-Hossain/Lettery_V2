package com.example.letterly

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface DatamuseApi {

    /** 
     * Generic search method using a QueryMap for flexible relation searches.
     * Common keys: ml, sl, sp, rel_jja, rel_jjb, rel_syn, rel_ant, rel_spc, rel_gen, 
     * rel_com, rel_par, rel_bga, rel_bgb, rel_rhy, rel_hom, rel_cns, rel_trg
     */
    @GET("words")
    suspend fun searchWords(
        @QueryMap options: Map<String, String>,
        @Query("md") metadata: String = "d",
        @Query("max") max: Int = 100
    ): List<DatamuseWord>

    /** Legacy/Convenience methods */
    @GET("words")
    suspend fun findWords(
        @Query("sp") spelledLike: String,
        @Query("md") metadata: String = "d",
        @Query("max") max: Int = 100
    ): List<DatamuseWord>

    @GET("words")
    suspend fun lookupWord(
        @Query("sp") word: String,
        @Query("md") metadata: String = "d",
        @Query("max") max: Int = 5
    ): List<DatamuseWord>

    @GET("words")
    suspend fun findRhymes(
        @Query("rel_rhy") word: String,
        @Query("md") metadata: String = "d",
        @Query("max") max: Int = 100
    ): List<DatamuseWord>

    @GET("words")
    suspend fun findByMeaning(
        @Query("ml") concept: String,
        @Query("md") metadata: String = "d",
        @Query("max") max: Int = 100
    ): List<DatamuseWord>
}
