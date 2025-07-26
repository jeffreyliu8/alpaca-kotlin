# alpaca-kotlin

[![Maven Central](https://img.shields.io/maven-central/v/io.github.jeffreyliu8/alpaca-kotlin)](https://search.maven.org/artifact/io.github.jeffreyliu8/alpaca-kotlin)

A Kotlin Multiplatform library for interacting with the [Alpaca Trading API](https://alpaca.markets/docs/).

## Features

- Get account information
- Manage positions (get, close)
- Place and manage orders (get, place, replace, cancel)
- Stream account updates and stock prices via WebSockets
- Get historical trade data

## Installation

Add the following to your `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("io.github.jeffreyliu8:alpaca-kotlin:0.0.4")
}
```

## Usage

```kotlin
import alpaca.AlpacaClient
import alpaca.AlpacaClientImpl

// For paper trading
val client: AlpacaClient = AlpacaClientImpl(
    usePolygon = false, // Or true if you have a Polygon subscription
    key = "YOUR_PAPER_API_KEY",
    secret = "YOUR_PAPER_API_SECRET"
)

// For live trading
val liveClient: AlpacaClient = AlpacaClientImpl(
    usePolygon = false, // Or true if you have a Polygon subscription
    key = "YOUR_LIVE_API_KEY",
    secret = "YOUR_LIVE_API_SECRET",
    usePaper = false
)


// Get account information
val account = client.getAccount()
println(account)

// Get all positions
val positions = client.getPositions()
println(positions)

// Place an order
val order = client.placeOrder(
    AlpacaOrderRequest(
        symbol = "AAPL",
        qty = "1",
        side = AlpacaBuySell.BUY,
        type = "market",
        timeInForce = "day"
    )
)
println(order)
```

### Streaming Data

You can stream account updates and stock prices using Kotlin Flows.

#### Stream Account Updates

```kotlin
runBlocking {
    launch {
        client.streamAccount().collect { accountUpdate ->
            println("Received account update: $accountUpdate")
        }
    }
}
```

#### Monitor Stock Prices

```kotlin
runBlocking {
    launch {
        client.monitorStockPrice(setOf("AAPL", "GOOG")).collect { stockPriceUpdate ->
            println("Received stock price update: $stockPriceUpdate")
        }
    }
}
```

### Test Mode
You can use the test mode to get a fake stock price for testing purposes.

```kotlin
runBlocking {
    client.monitorStockPrice(
        setOf("FAKEPACA"),
        stockExchange = AlpacaStockExchangeOption.TEST
    ).collect {
        println(it)
    }
}
```

## License

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for details.