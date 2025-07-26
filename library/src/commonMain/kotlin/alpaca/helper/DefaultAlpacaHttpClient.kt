package alpaca.helper

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val defaultAlpacaHttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
//            isLenient = true
//            ignoreUnknownKeys = true
        })
    }
//    install(Logging) {
//        logger = object : io.ktor.client.plugins.logging.Logger {
//            override fun log(message: String) {
//                co.touchlab.kermit.Logger.v(message, null, "ktor")
//            }
//        }
//        level = LogLevel.ALL
//    }
    install(WebSockets) {
    }
}