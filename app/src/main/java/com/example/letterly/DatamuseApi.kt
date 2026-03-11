package com.example.letterly

import retrofit2.http.GET
import retrofit2.http.Query

interface DatamuseApi {
    /**
     * @param spelledLike A pattern like 'a??e' to find 4-letter words starting with 'a' and ending with 'e'.
     * @param metadata 'd' for definitions.
     * @param max Max number of results.
     */
    @GET("words")
    suspend fun findWords(
        @Query("sp") spelledLike: String,
        @Query("md") metadata: String = "d",
        @Query("max") max: Int = 100
    ): List<DatamuseWord>
}
