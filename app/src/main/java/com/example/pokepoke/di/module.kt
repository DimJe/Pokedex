package com.example.pokepoke.di

import com.example.pokepoke.network.PokeService
import com.example.pokepoke.repository.PokeRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object module {

    @Singleton
    @Provides
    fun providePokeApi(): PokeService {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        return Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PokeService::class.java)
    }

    @Singleton
    @Provides
    fun provideRepo(api: PokeService) : PokeRepository = PokeRepository(api)
}