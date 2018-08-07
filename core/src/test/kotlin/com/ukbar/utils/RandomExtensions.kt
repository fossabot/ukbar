package com.ukbar.utils

import io.github.benas.randombeans.EnhancedRandomBuilder
import io.github.benas.randombeans.api.EnhancedRandom

inline fun<reified T> EnhancedRandom.nextObject(vararg excludedFields: String) =
        nextObject(T::class.java, *excludedFields)!!

val enhancedRandom: EnhancedRandom = EnhancedRandomBuilder().build()

inline fun<reified T> random(): T = when (T::class) {
    String::class -> enhancedRandom.nextObject(String::class.java) as T
    Int::class -> enhancedRandom.nextInt() as T
    else -> enhancedRandom.nextObject()
}