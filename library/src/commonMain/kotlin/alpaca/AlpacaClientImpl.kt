package alpaca

import alpaca.logger.LoggerRepository
import alpaca.model.AlpacaAccount
import alpaca.model.AlpacaErrorCodeMessageResponse
import alpaca.model.AlpacaOrder
import alpaca.model.AlpacaOrderIdStatus
import alpaca.model.AlpacaOrderRequest
import alpaca.model.AlpacaPosition
import alpaca.model.AlpacaReplaceOrderRequest
import alpaca.model.AlpacaResponseInterface
import alpaca.model.AlpacaStockExchangeOption
import alpaca.model.AlpacaSubscriptionMessage
import alpaca.model.AlpacaSubscriptionRequest
import alpaca.model.AlpacaSuccessMessageResponse
import alpaca.model.AlpacaTrades
import alpaca.model.BarSchema
import alpaca.model.QuoteSchema
import alpaca.model.TradeSchema
import alpaca.model.TradeUpdateSchema
import alpaca.model.stream.StreamingRequestResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlin.collections.forEach
import kotlin.time.ExperimentalTime

class AlpacaClientImpl(
    isPaper: Boolean = true,
    private val apiKey: String,
    private val apiSecret: String,
    private val httpClient: HttpClient,
    private val logger: LoggerRepository,
) : AlpacaClient {

    companion object {
        const val PAPER_API_URL = "https://paper-api.alpaca.markets"
        const val LIVE_API_URL = "https://api.alpaca.markets"

        const val PAPER_STREAM_URL = "paper-api.alpaca.markets"
        const val LIVE_STREAM_URL = "api.alpaca.markets"

        const val API_DATA_URL = "https://data.alpaca.markets"
    }

    private val apiDomain = if (isPaper) PAPER_API_URL else LIVE_API_URL
    private val streamDomain = if (isPaper) PAPER_STREAM_URL else LIVE_STREAM_URL
    private val apiDataDomain = API_DATA_URL

    private fun HttpRequestBuilder.withAlpacaHeaders() {
        headers {
            append("APCA-API-KEY-ID", apiKey)
            append("APCA-API-SECRET-KEY", apiSecret)
        }
        contentType(ContentType.Application.Json)
    }

    override suspend fun getAccount(): AlpacaAccount? {
        val rsp = httpClient.get("$apiDomain/v2/account") {
            withAlpacaHeaders()
        }
        if (rsp.status == HttpStatusCode.OK) {
            return rsp.body<AlpacaAccount>()
        }
        return null
    }

    override suspend fun getPositions(): List<AlpacaPosition>? {
        val rsp = httpClient.get("$apiDomain/v2/positions") {
            withAlpacaHeaders()
        }
        if (rsp.status == HttpStatusCode.OK) {
            return rsp.body<List<AlpacaPosition>>()
        }
        return null
    }

    override suspend fun getPosition(symbol: String): AlpacaPosition? {
        val rsp = httpClient.get("$apiDomain/v2/positions/$symbol") {
            withAlpacaHeaders()
        }
        if (rsp.status == HttpStatusCode.OK) {
            return rsp.body<AlpacaPosition>()
        }
        return null
    }

    override suspend fun closeAllPositions(cancelOrders: Boolean): List<AlpacaPosition> {
        val rsp = httpClient.delete("$apiDomain/v2/positions") {
            withAlpacaHeaders()
            parameter("cancel_orders", cancelOrders)
        }
        if (rsp.status == HttpStatusCode.OK) {
            return rsp.body<List<AlpacaPosition>>()
        }
        return emptyList()
    }

    override suspend fun closePosition(
        symbol: String,
        cancelOrders: Boolean,
        qty: String?,
        percentage: String?
    ): AlpacaOrder? {
        val rsp = httpClient.delete("$apiDomain/v2/positions/$symbol") {
            withAlpacaHeaders()
            parameter("cancel_orders", cancelOrders)
            parameter("qty", qty)
            parameter("percentage", percentage)
        }
        if (rsp.status == HttpStatusCode.OK) {
            return rsp.body<AlpacaOrder>()
        }
        return null
    }

    override suspend fun placeOrder(orderRequest: AlpacaOrderRequest): AlpacaOrder? {
        val rsp = httpClient.post("$apiDomain/v2/orders") {
            withAlpacaHeaders()
            setBody(orderRequest)
        }
        if (rsp.status == HttpStatusCode.OK) {
            return rsp.body<AlpacaOrder>()
        }
        return null
    }

    override suspend fun getOrders(
        status: String,
        limit: Int,
        after: String?,
        until: String?,
        direction: String,
        nested: Boolean?,
        symbols: List<String>?
    ): List<AlpacaOrder> {
        val rsp = httpClient.get("$apiDomain/v2/orders") {
            withAlpacaHeaders()
            parameter("status", status)
            parameter("limit", limit)
            parameter("after", after)
            parameter("until", until)
            parameter("direction", direction)
            parameter("nested", nested)
            parameter("symbols", symbols?.joinToString(","))
        }
        if (rsp.status == HttpStatusCode.OK) {
            return rsp.body<List<AlpacaOrder>>()
        }
        return emptyList()
    }

    override suspend fun getOrder(orderId: String): AlpacaOrder? {
        val rsp = httpClient.get("$apiDomain/v2/orders/$orderId") {
            withAlpacaHeaders()
        }
        if (rsp.status == HttpStatusCode.OK) {
            return rsp.body<AlpacaOrder>()
        }
        return null
    }

    override suspend fun getOrderByClientId(clientOrderId: String): AlpacaOrder? {
        val rsp = httpClient.get("$apiDomain/v2/orders:by_client_order_id") {
            withAlpacaHeaders()
            parameter("client_order_id", clientOrderId)
        }
        if (rsp.status == HttpStatusCode.OK) {
            return rsp.body<AlpacaOrder>()
        }
        return null
    }

    override suspend fun replaceOrder(
        orderId: String,
        replaceOrderRequest: AlpacaReplaceOrderRequest
    ): AlpacaOrder {
        val order: AlpacaOrder = httpClient.patch("$apiDomain/v2/orders/$orderId") {
            withAlpacaHeaders()
            contentType(ContentType.Application.Json)
            setBody(replaceOrderRequest)
        }.body()
        return order
    }

    override suspend fun cancelAllOrders(): List<AlpacaOrderIdStatus> {
        val rsp = httpClient.delete("$apiDomain/v2/orders") {
            withAlpacaHeaders()
        }
        if (rsp.status == HttpStatusCode.OK) {
            return rsp.body<List<AlpacaOrderIdStatus>>()
        }
        return emptyList()
    }

    override suspend fun cancelOrder(orderId: String): AlpacaOrderIdStatus? {
        val rsp = httpClient.delete("$apiDomain/v2/orders/$orderId") {
            withAlpacaHeaders()
        }
        if (rsp.status == HttpStatusCode.OK) {
            return rsp.body<AlpacaOrderIdStatus>()
        }
        return null
    }

    @OptIn(ExperimentalTime::class)
    override fun monitorStockPrice(
        symbols: Set<String>,
        stockExchange: AlpacaStockExchangeOption,
    ): Flow<List<AlpacaResponseInterface>> = flow {
        try {
            httpClient.webSocket(
                method = HttpMethod.Get,
                host = "stream.data.alpaca.markets",
                path = "/v2/${stockExchange.name.lowercase()}",
                request = {
                    url {
                        protocol = URLProtocol.WSS
                    }
                    withAlpacaHeaders()
                }
            ) {

                val subscriptionRequest = AlpacaSubscriptionMessage(
                    action = "subscribe",
                    trades = symbols.toList(),
                    quotes = symbols.toList(),
                    bars = symbols.toList()
                )

                val myText = Json.encodeToString(
                    AlpacaSubscriptionMessage.serializer(),
                    subscriptionRequest
                )
                send(Frame.Text(myText))

                // Receive messages
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val text = frame.readText()
//                            logger.d("Received WebSocket frame: $text")

                            // Parse the message
                            val apiResponse =
                                Json.decodeFromString<List<AlpacaResponseInterface>>(text)

                            apiResponse.forEach { response ->
                                when (response) {
                                    is AlpacaErrorCodeMessageResponse -> {
//                                            logger.e("Error response: $response")
                                        emit(apiResponse)
                                        return@forEach
                                    }

                                    is AlpacaSuccessMessageResponse -> {
//                                            logger.d("success response: ${response.msg}")
                                        emit(apiResponse)
                                        return@forEach
                                    }

                                    is AlpacaSubscriptionRequest -> {
//                                            logger.d("Subscription request: $response")
                                        emit(apiResponse)
                                        return@forEach
                                    }

                                    is TradeSchema -> {
//                                            logger.d("TradeSchema response: $response")
                                        emit(apiResponse)
                                        return@forEach
                                    }

                                    is QuoteSchema -> {
//                                            logger.d("QuoteSchema response: $response")
                                        emit(apiResponse)
                                        return@forEach
                                    }

                                    is BarSchema -> {
//                                            logger.d("BarSchema response: $response")
                                        emit(apiResponse)
                                        return@forEach
                                    }

                                    is TradeUpdateSchema -> {
//                                            logger.d("TradeUpdateSchema response: $response")
                                        emit(apiResponse)
                                        return@forEach
                                    }
                                }
                            }
                        }

                        else -> logger.e("Received non-text frame: $frame")
                    }
                }
            }
        } catch (e: Exception) {
            logger.e("Error in WebSocket connection: ${e.message}")
            throw e
        }
    }

    override fun streamAccount(): Flow<StreamingRequestResponse> = flow {
        try {
            httpClient.webSocket(
                method = HttpMethod.Get,
                host = streamDomain,
                path = "/stream",
                request = {
                    url {
                        protocol = URLProtocol.WSS
                    }
                }
            ) {
                // Authenticate
                val authMessage = Json.encodeToString(
                    AlpacaSubscriptionMessage.serializer(),
                    AlpacaSubscriptionMessage(action = "auth", key = apiKey, secret = apiSecret)
                )
                send(Frame.Text(authMessage))

                // Subscribe to account updates
                val subscriptionMessage = Json.encodeToString(
                    AlpacaSubscriptionMessage.serializer(),
                    AlpacaSubscriptionMessage(
                        action = "listen",
                        data = AlpacaSubscriptionMessage.Data(streams = listOf("trade_updates"))
                    )
                )
                send(Frame.Text(subscriptionMessage))

                for (frame in incoming) {
                    when (frame) {
//                        is Frame.Text -> {
//                            val text = frame.readText()
//                            val apiResponse =
//                                Json.decodeFromString<List<AlpacaResponseInterface>>(text)
//                            emit(apiResponse)
//                        }

                        is Frame.Binary -> {
                            val response = frame.readBytes().decodeToString()
                            logger.d("frame: $response")
                            val apiResponse =
                                Json.decodeFromString<StreamingRequestResponse>(response)
                            emit(apiResponse)
                        }

                        else -> logger.e("Received non-text frame: $frame")
                    }
                }
            }
        } catch (e: Exception) {
            logger.e("Error in WebSocket connection: ${e.message}")
            throw e
        }
    }

    override suspend fun getTrades(
        symbol: String,
        start: String?,
        end: String?,
        limit: Int,
        pageToken: String?
    ): AlpacaTrades? {
        val rsp = httpClient.get("$apiDataDomain/v2/stocks/$symbol/trades") {
            withAlpacaHeaders()
            parameter("start", start)
            parameter("end", end)
            parameter("limit", limit)
            parameter("page_token", pageToken)
        }
        if (rsp.status == HttpStatusCode.OK) {
            return rsp.body<AlpacaTrades>()
        }
        return null
    }
}
