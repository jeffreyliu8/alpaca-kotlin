package alpaca.helper

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun String.timestampInstantStringToMilliLong(): Long {
    return Instant.parse(this).toEpochMilliseconds()
}