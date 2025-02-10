package org.example

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.kotlin.logger


class App {
    val greeting: String
        get() {
            return "Hello"
        }
}

fun main() = runBlocking {
    launch {
        delay(1000L)
        println("async k-World!")
        logger.info("Launched")
    }
    logger.info("App started")
    println(App().greeting)
}
