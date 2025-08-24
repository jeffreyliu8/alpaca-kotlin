package alpaca.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlpacaNewsResponse(
    val news: List<AlpacaNewsArticle>,

    @SerialName("next_page_token")
    val nextPageToken: String? = null,
)

@Serializable
data class AlpacaNewsArticle(
    val author: String,
    val content: String,

    @SerialName("created_at")
    val createdAt: String,

    val headline: String,
    val id: Long,
    val images: List<AlpacaNewsImage>,
    val source: String,
    val summary: String,
    val symbols: List<String>,

    @SerialName("updated_at")
    val updatedAt: String,

    val url: String
)

@Serializable
data class AlpacaNewsImage(
    val size: String,
    val url: String
)