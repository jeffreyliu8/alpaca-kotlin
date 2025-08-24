package alpaca.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlpacaClockResponse(
    @SerialName("is_open") val isOpen: Boolean,
    @SerialName("next_close") val nextClose: String,//"2025-08-25T16:00:00-04:00"
    @SerialName("next_open") val nextOpen: String,  //"2025-08-25T09:30:00-04:00"
    @SerialName("timestamp") val timestamp: String?,//"2025-08-23T22:23:50.659466386-04:00"
)