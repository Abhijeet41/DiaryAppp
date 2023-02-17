package com.example.util.model

import com.example.util.model.RequestState

sealed class RequestState<out T>{
    object Idle: RequestState<Nothing>()
    object Loading: RequestState<Nothing>()
    data class Success<T>(val data:T): RequestState<T>()
    data class Error<T>(val error:Throwable): RequestState<T>()
}
