package com.example.myfirstapp

import kotlinx.coroutines.delay

class Repository {

    suspend fun doWork(value: Int): Int {
        delay(5_000)
        return value
    }
}
