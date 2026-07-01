# FX Triangulation Engine

FX Triangulation Engine is a real-time backend system that simulates an FX pricing engine with streaming market data, synthetic cross-rate generation, and arbitrage-aware pricing logic.

## GitHub Description

Real-time FX pricing engine built with Spring Boot and React. Streams live quotes over WebSockets, derives synthetic cross rates through triangulation, applies client-specific spreads, and visualizes price movements in a trading-style dashboard.

## Overview

This project simulates the core workflow of an FX pricing engine:

- ingest raw market rates
- generate client-facing bid, mid, and ask quotes
- derive missing cross pairs from tradable FX legs
- detect and reduce simple pair-vs-path arbitrage issues
- publish live updates to a browser dashboard over WebSockets

The backend is built in Java with Spring Boot, and the frontend is built in React with Recharts for live charting.

## Features

- Live FX inputs using Twelve Data for direct market anchors
- Synthetic cross-rate calculation using FX triangulation
- Bid/ask quote generation from client-specific spreads
- Executable-path pricing for derived crosses
- Basic no-arbitrage normalization between direct and synthetic quotes
- Real-time publishing over STOMP/WebSocket
- Interactive frontend dashboard with latency, latest update time, and chart history
- Smooth price movement overlay for a more natural demo and visualization experience

## Design Goals
- Low-latency quote updates
- Separation of pricing and transport layers
- Stateless REST + stateful streaming via WebSockets
- Extensible pricing logic for additional FX pairs

## Supported Pairs

The current project streams and prices:

- `EUR/USD`
- `USD/JPY`
- `EUR/JPY`

`EUR/JPY` is derived from the direct market anchors and priced through the engine logic rather than treated as a raw input.

## Architecture

### Backend

- `pricing.alpha`
  Pulls anchor market rates from Twelve Data and exposes them through a stable pricing interface.

- `pricing.service`
  Builds client-facing quotes from market anchors, applies spreads, handles inverses, and normalizes direct quotes against synthetic alternatives.

- `pricing.triangulation`
  Computes cross rates and executable synthetic bid/ask paths across the currency graph.

- `pricing.websocket`
  Pushes live prices to subscribed clients once per second.

- `controller`
  Exposes REST endpoints for direct quote requests.

### Frontend

- Subscribes to live price topics through SockJS/STOMP
- Displays bid, mid, ask, status, and latency in a pricing board
- Tracks short rolling history for each pair
- Renders an interactive line chart for the selected pair

## Tech Stack

### Backend

- Java 21
- Spring Boot
- Spring WebSocket / STOMP
- Maven

### Frontend

- React
- SockJS
- STOMP.js
- Recharts

### Data

- Twelve Data FX API

## Pricing Logic

The engine does more than pass through raw API prices.

### Direct Pairs

For directly supported market pairs, the backend:

- reads the latest market mid
- applies the configured client spread
- returns bid, mid, and ask

### Synthetic Pairs

For derived pairs such as `EUR/JPY`, the backend:

- uses direct legs like `EUR/USD` and `USD/JPY`
- builds an executable conversion path
- derives synthetic bid/ask values instead of multiplying mids only

### Arbitrage Protection

The engine compares direct quotes with synthetic alternatives and keeps direct prices inside a basic no-arbitrage band. This reduces the chance that a client can exploit a mismatch between a direct pair and its triangular path.

## Live Data Setup

This project reads the Twelve Data API key from an environment variable so secrets are not committed to GitHub.

### Application Property

The backend uses:

```properties
twelvedata.api-key=${TWELVEDATA_API_KEY:}
```

### PowerShell

Set the API key before starting the backend:

```powershell
$env:TWELVEDATA_API_KEY="your-api-key"
```

## Running the Project

### Backend

From:

`C:\Users\FarhanWork\Documents\Dev\FX-Triangulation-Engine\fx-triangulation-engine`

Run:

```powershell
./mvnw.cmd spring-boot:run
```

The backend starts on:

`http://localhost:8080`

### Frontend

From:

`C:\Users\FarhanWork\Documents\Dev\FX-Triangulation-Engine\fx_ui\fx-ui`

Run:

```powershell
npm start
```

The frontend starts on:

`http://localhost:3000`

## REST Endpoint

Example quote request:

```text
GET /price?client=CLIENT_A&base=EUR&quote=JPY
```

Example response:

```json
{
  "mid": 165.100589,
  "bid": 165.024269,
  "ask": 165.176910,
  "arbitrage": false
}
```

## WebSocket Topics

The frontend subscribes to:

- `/topic/prices/EURUSD`
- `/topic/prices/USDJPY`
- `/topic/prices/EURJPY`

The backend publishes refreshed prices every second.

## Tests

The backend test suite covers:

- spread calculation
- triangulation logic
- inverse pricing
- no-arbitrage normalization
- alpha generator stability and live-rate integration behavior

Run:

```powershell
./mvnw.cmd test
```

## Notes

- This is a learning and portfolio project, not a production trading system.
- The pricing model includes useful market-making concepts, but it does not yet handle inventory skew, execution risk, market data quality controls, or institutional-grade latency management.
- The frontend is intentionally designed as a compact trading-style monitor for quick visual feedback.

## Why This Project Matters

This project demonstrates practical work across both quant-style pricing logic and full-stack engineering:

- financial data ingestion
- graph-based cross-rate pricing
- spread and quote construction
- arbitrage-aware logic
- WebSocket streaming
- frontend visualization

It is a strong example of building a pricing application end-to-end rather than only modeling the math in isolation.
