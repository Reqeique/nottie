package com.ultraone.nottie.model

sealed class Result {

    class SUCCESS< T>(val data: T) : Result()
    data class FAILED(val throwable: Throwable): Result()
    object LOADING : Result()
    object NULL_VALUE : Result()

}

