package com.example.pokepoke.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokepoke.data.PokemonDetail
import com.example.pokepoke.data.PokemonListItem
import com.example.pokepoke.data.ScreenState
import com.example.pokepoke.data.toListItem
import com.example.pokepoke.network.ResultType
import com.example.pokepoke.repository.PokeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PokeViewModel @Inject constructor(
    private val repository: PokeRepository
): ViewModel(){

    var pageNum = 0

    var pokemonList = mutableStateOf<List<PokemonListItem>>(listOf())
    var loadError = mutableStateOf("")
    var isLoading = mutableStateOf(false)
    var endReached = mutableStateOf(false)
    var pokemonDetail = mutableStateOf<PokemonDetail?>(null)
    fun loadPokemonList(){
        viewModelScope.launch {
            isLoading.value = true
            val result = repository.getPokemonList(pageNum * 20)
            when(result){
                is ResultType.Success -> {
                    endReached.value = pageNum * 20 >= result.data!!.results.size
                    val pokeListItem = result.data.results.map { it.toListItem() }

                    pageNum++
                    loadError.value = ""
                    isLoading.value = false
                    pokemonList.value += pokeListItem
                }
                is ResultType.Error -> {
                    loadError.value = result.message!!
                    isLoading.value = false
                }
            }
        }
    }
    fun loadDetail(num: String){
        viewModelScope.launch {
            val result = repository.getPokemonDetail(num)
            when(result){
                is ResultType.Error -> {
                    Timber.e(result.message)
                }
                is ResultType.Success -> {
                    Timber.e("${result.data}")
                    pokemonDetail.value = result.data
                }
            }
        }
    }


}