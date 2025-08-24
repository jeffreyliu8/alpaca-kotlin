package io.github.jeffreyliu8

import alpaca.AlpacaClient
import alpaca.AlpacaClientImpl
import alpaca.logger.LoggerRepositoryImpl
import alpaca.model.AlpacaResponseInterface
import alpaca.model.AlpacaStockExchangeOption
import alpaca.model.stream.StreamingRequestResponse
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MyCommonUnitTest {
    @Test
    fun basicExample() = runTest {
        val client: AlpacaClient = AlpacaClientImpl(
            isPaper = true,
            apiKey = apiKey,
            apiSecret = apiSecret,
        )

        val account = client.getAccount()
        assertEquals("1f8d7713-771d-4679-a756-e324276762bc", account?.id)
    }

    @Test
    fun customerClientLoggerExample() = runTest {
        val httpClient = HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
//                    isLenient = true
//                    ignoreUnknownKeys = true
                })
            }
            install(Logging) {
                logger = object : io.ktor.client.plugins.logging.Logger {
                    override fun log(message: String) {
                        co.touchlab.kermit.Logger.v(message, null, "ktor")
                    }
                }
                level = LogLevel.ALL
            }
            install(WebSockets) {
            }
        }

        val logger = LoggerRepositoryImpl()

        val client: AlpacaClient = AlpacaClientImpl(
            isPaper = true,
            apiKey = apiKey,
            apiSecret = apiSecret,
            httpClient = httpClient,
            logger = logger,
        )
        assertEquals(1, 1)
        val account = client.getAccount()
        assertEquals("1f8d7713-771d-4679-a756-e324276762bc", account?.id)

//        client.streamAccount().collect {
//            println(it)
//        }
//
//        client.monitorStockPrice(setOf("FAKEPACA"), overrideWithTestMode = true).collect {
//            println(it)
//        }

//        combine(
//            client.streamAccount(),
//            client.monitorStockPrice(setOf("TSLA"), overrideWithTestMode = false)
//        ) { a, b ->
//            println("$a")
//        }.collect {
//
//        }

        val pair: Pair<StreamingRequestResponse, List<AlpacaResponseInterface>> = combine(
            client.streamAccount(),
            client.monitorStockPrice(
                setOf("FAKEPACA"),
                stockExchange = AlpacaStockExchangeOption.TEST
            )
        ) { a, b ->
            println("$a, $b")
            Pair(a, b)
        }.first()
        assertNotNull(pair.first.toString())
    }

    @Test
    fun monitorFakepacaStockPriceExample() = runTest {
        val client: AlpacaClient = AlpacaClientImpl(
            isPaper = true,
            apiKey = apiKey,
            apiSecret = apiSecret,
        )
        val response = client.monitorStockPrice(
            setOf("FAKEPACA"),
            stockExchange = AlpacaStockExchangeOption.TEST
        ).take(4).last()
        println("fakepeca response: ${response.last()}")
        assertNotNull(response)
    }

    @Test
    fun clockTestExample() = runTest {
        val client: AlpacaClient = AlpacaClientImpl(
            isPaper = true,
            apiKey = apiKey,
            apiSecret = apiSecret,
        )
        val response = client.getClock()
        println("clock response: $response")
        assertNotNull(response)
    }

    @Test
    fun getNewsTest() = runTest {
        val client: AlpacaClient = AlpacaClientImpl(
            isPaper = true,
            apiKey = apiKey,
            apiSecret = apiSecret,
        )
        val response = client.getNews(symbols = "AAPL,TSLA")
        println("subscribeNewsTest response: $response")
        assertNotNull(response)
    }

    @Test
    fun subscribeNewsTest() = runTest {
        val client: AlpacaClient = AlpacaClientImpl(
            isPaper = true,
            apiKey = apiKey,
            apiSecret = apiSecret,
        )
        val response = client.streamNews().take(3)
        println("subscribeNewsTest response: ${response.first()}")
        println("subscribeNewsTest response: ${response.last()}")
        assertNotNull(response)
    }
}