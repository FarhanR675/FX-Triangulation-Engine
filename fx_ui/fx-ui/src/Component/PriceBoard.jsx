import { useEffect, useState, useRef } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

function PriceBoard() {
  const [prices, setPrices] = useState({
    EURUSD: {},
    EURJPY: {},
  });

  // Store previous prices to detect movement
  const prevPrices = useRef({});

  const pairs = ["EURUSD", "EURJPY"];

  useEffect(() => {
    const socket = new SockJS("http://localhost:8080/ws-prices");

    const client = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      onConnect: () => {
        pairs.forEach((pair) => {
          client.subscribe(`/topic/prices/${pair}`, (msg) => {
            const data = JSON.parse(msg.body);

            setPrices((prev) => {
              prevPrices.current[pair] = prev[pair]; // store previous
              return { ...prev, [pair]: data };
            });
          });
        });
      },
    });

    client.activate();
    return () => client.deactivate();
  }, []);

  const format = (n) => (n ? Number(n).toFixed(5) : "-");

  // Determine price movement
  const getColor = (pair, field) => {
    const prev = prevPrices.current[pair]?.[field];
    const curr = prices[pair]?.[field];

    if (!prev || !curr) return "white";
    if (curr > prev) return "#16a34a"; // green
    if (curr < prev) return "#dc2626"; // red
    return "white";
  };

  const thStyle = {
    padding: "12px",
    textAlign: "left",
    fontWeight: "600",
  };

  const tdStyle = {
    padding: "12px",
    borderTop: "1px solid #334155",
  };

  return (
    <div
      style={{
        padding: "40px",
        backgroundColor: "#0f172a",
        minHeight: "100vh",
        color: "white",
        fontFamily: "Arial",
      }}
    >
      <h1 style={{ marginBottom: "20px" }}>FX Prices</h1>

      <table
        style={{
          width: "100%",
          borderCollapse: "collapse",
          backgroundColor: "#1e293b",
          borderRadius: "10px",
          overflow: "hidden",
        }}
      >
        <thead style={{ backgroundColor: "#334155" }}>
          <tr>
            <th style={thStyle}>Pair</th>
            <th style={thStyle}>Bid</th>
            <th style={thStyle}>Mid</th>
            <th style={thStyle}>Ask</th>
            <th style={thStyle}>Status</th>
          </tr>
        </thead>

        <tbody>
          {pairs.map((pair) => (
            <tr key={pair}>
              <td style={tdStyle}>{pair.slice(0, 3) + "/" + pair.slice(3)}</td>

              <td style={{ ...tdStyle, color: getColor(pair, "bid") }}>
                {format(prices[pair]?.bid)}
              </td>

              <td style={{ ...tdStyle, color: getColor(pair, "mid") }}>
                {format(prices[pair]?.mid)}
              </td>

              <td style={{ ...tdStyle, color: getColor(pair, "ask") }}>
                {format(prices[pair]?.ask)}
              </td>

              <td style={tdStyle}>LIVE</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default PriceBoard;
