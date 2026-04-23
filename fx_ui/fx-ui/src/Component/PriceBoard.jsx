import { useEffect, useState, useRef } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
} from "recharts";

function PriceBoard() {
  const pairs = ["EURUSD", "EURJPY", "USDJPY"];

  const [prices, setPrices] = useState(
    Object.fromEntries(pairs.map((p) => [p, {}])),
  );

  const [history, setHistory] = useState({});
  const [selectedPair, setSelectedPair] = useState(pairs[0]);
  const [now, setNow] = useState(Date.now());

  const prevPrices = useRef({});
  const flashState = useRef({}); // ✅ NEW (stores flash color)

  // ⏱ Tick for latency
  useEffect(() => {
    const interval = setInterval(() => {
      setNow(Date.now());
    }, 1000);

    return () => clearInterval(interval);
  }, []);

  // 📡 WebSocket
  useEffect(() => {
    const socket = new SockJS("http://localhost:8080/ws-prices");

    const client = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      onConnect: () => {
        pairs.forEach((pair) => {
          client.subscribe(`/topic/prices/${pair}`, (msg) => {
            const data = JSON.parse(msg.body);
            const nowTime = new Date();

            // 📈 Chart history
            setHistory((h) => {
              const prevArr = h[pair] || [];

              return {
                ...h,
                [pair]: [
                  ...prevArr,
                  {
                    time: nowTime.toLocaleTimeString(),
                    mid: data.mid,
                  },
                ].slice(-30),
              };
            });

            // 💥 Detect movement → trigger flash
            const prevMid = prevPrices.current[pair]?.mid;
            if (prevMid && data.mid) {
              if (data.mid > prevMid) {
                flashState.current[pair] = "green";
              } else if (data.mid < prevMid) {
                flashState.current[pair] = "red";
              }

              // remove flash after 300ms
              setTimeout(() => {
                flashState.current[pair] = null;
              }, 300);
            }

            // 💰 Update prices
            setPrices((prev) => {
              prevPrices.current[pair] = prev[pair];

              return {
                ...prev,
                [pair]: {
                  ...data,
                  timestamp: nowTime,
                },
              };
            });
          });
        });
      },
    });

    client.activate();
    return () => client.deactivate();
  }, []);

  const format = (n) => (n ? Number(n).toFixed(5) : "-");

  const getColor = (pair, field) => {
    const prev = prevPrices.current[pair]?.[field];
    const curr = prices[pair]?.[field];

    if (!prev || !curr) return "#94a3b8";
    if (curr > prev) return "#22c55e";
    if (curr < prev) return "#ef4444";
    return "#94a3b8";
  };

  const getLatency = (pair) => {
    const ts = prices[pair]?.timestamp;
    if (!ts) return "-";

    const diff = (now - ts.getTime()) / 1000;
    return diff.toFixed(1) + "s";
  };

  // ✅ FLASH STYLE
  const getFlashStyle = (pair) => {
    const flash = flashState.current[pair];

    if (flash === "green") {
      return { backgroundColor: "rgba(34,197,94,0.3)", transition: "0.3s" };
    }
    if (flash === "red") {
      return { backgroundColor: "rgba(239,68,68,0.3)", transition: "0.3s" };
    }
    return {};
  };

  return (
    <div
      style={{
        display: "flex",
        height: "100vh",
        backgroundColor: "#020617",
        color: "white",
        fontFamily: "monospace",
      }}
    >
      {/* LEFT PANEL */}
      <div
        style={{
          width: "45%",
          borderRight: "1px solid #1e293b",
          padding: "20px",
        }}
      >
        <h2 style={{ marginBottom: "20px", color: "#38bdf8" }}>FX PRICES</h2>

        <table style={{ width: "100%", borderCollapse: "collapse" }}>
          <thead style={{ color: "#64748b", fontSize: "12px" }}>
            <tr>
              <th>PAIR</th>
              <th>BID</th>
              <th>MID</th>
              <th>ASK</th>
              <th>STATUS</th>
              <th>UPDATED</th>
              <th>LAT</th>
            </tr>
          </thead>

          <tbody>
            {pairs.map((pair) => (
              <tr
                key={pair}
                onClick={() => setSelectedPair(pair)}
                style={{
                  cursor: "pointer",
                  backgroundColor:
                    selectedPair === pair ? "#1e293b" : "transparent",
                }}
              >
                <td style={{ padding: "10px" }}>
                  {pair.slice(0, 3) + "/" + pair.slice(3)}
                </td>

                <td
                  style={{
                    color: getColor(pair, "bid"),
                    ...getFlashStyle(pair),
                  }}
                >
                  {format(prices[pair]?.bid)}
                </td>

                <td
                  style={{
                    color: getColor(pair, "mid"),
                    ...getFlashStyle(pair),
                  }}
                >
                  {format(prices[pair]?.mid)}
                </td>

                <td
                  style={{
                    color: getColor(pair, "ask"),
                    ...getFlashStyle(pair),
                  }}
                >
                  {format(prices[pair]?.ask)}
                </td>

                <td
                  style={{
                    color: prices[pair]?.arbitrage ? "#f97316" : "#22c55e",
                  }}
                >
                  {prices[pair]?.arbitrage ? "ARB" : "OK"}
                </td>

                <td style={{ color: "#94a3b8" }}>
                  {prices[pair]?.timestamp
                    ? prices[pair].timestamp.toLocaleTimeString()
                    : "-"}
                </td>

                <td style={{ color: "#94a3b8" }}>{getLatency(pair)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* RIGHT PANEL */}
      <div style={{ width: "55%", padding: "20px" }}>
        <h2 style={{ marginBottom: "10px", color: "#38bdf8" }}>
          {selectedPair.slice(0, 3) + "/" + selectedPair.slice(3)} CHART
        </h2>

        <div style={{ marginBottom: "10px", color: "#94a3b8" }}>
          Last update:{" "}
          {prices[selectedPair]?.timestamp
            ? prices[selectedPair].timestamp.toLocaleTimeString()
            : "-"}
        </div>

        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={history[selectedPair] || []}>
            <XAxis dataKey="time" hide />
            <YAxis domain={["auto", "auto"]} />
            <Tooltip />

            <Line
              type="monotone"
              dataKey="mid"
              stroke={getColor(selectedPair, "mid")}
              strokeWidth={2}
              dot={false}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

export default PriceBoard;
