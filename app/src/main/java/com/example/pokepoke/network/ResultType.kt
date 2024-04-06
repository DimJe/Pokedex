package com.example.pokepoke.network

sealed class ResultType<T>(val data: T? = null,val message: String? = null) {
    class Success<T>(data: T) : ResultType<T>(data)
    class Error<T>(message: String, data: T? = null) : ResultType<T>(data, message)
}