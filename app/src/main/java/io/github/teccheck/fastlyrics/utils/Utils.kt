package io.github.teccheck.fastlyrics.utils

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success

object Utils {
    fun <T, E> result(value: T?, exception: E): Result<T, E> {
        return if (value == null)
            Failure(exception)
        else
            Success(value)
    }
}