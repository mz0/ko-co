package org.example

import org.apache.logging.log4j.kotlin.logger

class App {
    val greeting: String
        get() {
            return "Hello k-World!"
        }

    companion object {
        val logger = logger()
    }

}

fun main() {
    App.logger.info("App started")
    println(App().greeting)
}
