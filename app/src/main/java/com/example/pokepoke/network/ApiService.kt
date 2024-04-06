package com.example.pokepoke.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ApiService {

    private val baseUrl = "https://pokeapi.co/api/"
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    var nextUrl = ""
    val instance = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(PokeService::class.java)
}