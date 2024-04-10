package com.example.pokepoke.network

import com.example.pokepoke.data.PokemonDetail
import com.example.pokepoke.data.PokemonList
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface PokeService {

    @GET("v2/pokemon/")
    suspend fun getPokemonList(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<PokemonList>

    @GET
    suspend fun getPokemonDetail(@Url url: String): Response<PokemonDetail>

    @GET("v2/pokemon/{id}/")
    suspend fun getPokemonX(id: Int): Response<PokemonDetail>
}