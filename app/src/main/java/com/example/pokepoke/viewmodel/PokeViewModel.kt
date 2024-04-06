package com.example.pokepoke.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokepoke.data.PokemonListItem
import com.example.pokepoke.network.ResultType
import com.example.pokepoke.repository.PokeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokeViewModel @Inject constructor(
    private val repository: PokeRepository
): ViewModel(){

    var pageNum = 1

    var pokemonList = mutableStateOf<List<PokemonListItem>>(listOf())
    var loadError = mutableStateOf("")
    var isLoading = mutableStateOf(false)
    var endReached = mutableStateOf(false)

    fun loadPokemonList(){
        viewModelScope.launch {
            isLoading.value = true
            val result = repository.getPokemonList(pageNum * 50)
            when(result){
                is ResultType.Success -> {
                    endReached.value = pageNum * 50 >= result.data!!.results.size
                }
                is ResultType.Error -> {
                    loadError.value = result.message!!
                    isLoading.value = false
                }
            }
        }
    }

}