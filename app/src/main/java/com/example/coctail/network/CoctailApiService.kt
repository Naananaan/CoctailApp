package com.example.coctail.network

import com.example.coctail.model.CocktailResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface CocktailApiService {
    @GET("search.php")
    suspend fun searchCocktails(@Query("s") name: String): Response<CocktailResponse>

    companion object {
        private const val BASE_URL = "https://www.thecocktaildb.com/api/json/v1/1/"

        fun create(): CocktailApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(CocktailApiService::class.java)
        }
    }
}
