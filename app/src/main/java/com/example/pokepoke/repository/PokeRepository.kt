package com.example.pokepoke.repository

import com.example.pokepoke.data.PokemonList
import com.example.pokepoke.network.PokeService
import com.example.pokepoke.network.ResultType
import javax.inject.Inject


class PokeRepository @Inject constructor(
    private val api: PokeService
) {
    suspend fun getPokemonList(offset:Int): ResultType<PokemonList> {
        val response = try {
            api.getPokemonList(offset = offset)
        }catch (e: Exception){
            return ResultType.Error(e.message!!)
        }
        return ResultType.Success(response.body()!!)
    }

    //suspend fun getPokemonDetail
}