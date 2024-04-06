package com.example.pokepoke.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.pokepoke.network.ApiService
import com.example.pokepoke.network.PokeService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class PokemonSource(private val service: PokeService) : PagingSource<Int,Pokemon>(){


    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Pokemon> {
        return try {
            Timber.e("PagingSource load called")
            Timber.e("${params.key}")
            val nextPageNumber = params.key ?: 0
            val response = service.getPokemonList(offset = nextPageNumber.times(20))

            LoadResult.Page(
                data = response.body()?.results!!,
                prevKey = if(nextPageNumber == 0) null else nextPageNumber.plus(1),
                nextKey = nextPageNumber.plus(1)
            )


        } catch (e: Exception){
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Pokemon>): Int? {
        Timber.e("getRefreshKey")
        return state.anchorPosition?.let {
            val anchorPage = state.closestPageToPosition(it)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }


}