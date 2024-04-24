package com.example.pokepoke.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable


@JsonClass(generateAdapter = true)
data class PokemonList(val results: List<Pokemon>,val next: String)

@JsonClass(generateAdapter = true)
data class Pokemon(val name: String,val url: String)

@JsonClass(generateAdapter = true)
data class PokemonDetail(val height: Int,val weight: Int,val stats: List<Stat>,val types: List<TypeData>,val name: String,val id: Int) : Serializable

@JsonClass(generateAdapter = true)
data class TypeData(val type: Type,val slot: Int)
@JsonClass(generateAdapter = true)
data class Type(val name: String)

@JsonClass(generateAdapter = true)
data class Stat(val stat: StatDetail,@Json(name="base_stat")val baseStat: Int)

@JsonClass(generateAdapter = true)
data class StatDetail(val name: String)

data class PokemonListItem(val name: String,val imgUrl: String,val number: Int)
fun Pokemon.toListItem(): PokemonListItem{

    val number = if(url.endsWith("/")) {
        url.dropLast(1).takeLastWhile { it.isDigit() }
    } else {
        url.takeLastWhile { it.isDigit() }
    }
    return PokemonListItem(
        name = name,
        number = number.toInt(),
        imgUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${number}.png"
    )
}
