package alpaca.model.stream

import kotlinx.serialization.Serializable

@Serializable
data class AlpacaNewsSubscriptionMessage(
    val action: String = "subscription",
    val news: List<String> = emptyList(),
)