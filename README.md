# 📡 Cell Tower Call Simulator

A Java-based call record simulator that mimics real-world cell tower traffic, including dropped calls. Built with [Quarkus](https://quarkus.io/) and Kafka, it emits call records at a configurable drop rate to simulate network health for demos, dashboards, or testing self-healing systems.

---

## 🚀 Features

- Emits realistic call records on a timer (e.g., every 3 seconds)
- Simulates dropped calls using a configurable drop rate
- Sends records to Kafka
- Exposes a `/control` REST API for:
    - Toggling the emitter on/off
    - Adjusting drop rate dynamically
    - Viewing current emitter state
- Dev-friendly: runs locally with Quarkus Dev Services (no Kafka setup needed)
- Deployable to OpenShift or any container platform

---

## 🔧 Configuration

Set in `application.properties` or via environment variables:

```properties
# Kafka topic config
mp.messaging.outgoing.callrecord-out.topic=${CALL_RECORD_TOPIC:call-records}

```

---

## 🛠 REST API

**Base path:** `/control`

| Method | Endpoint                     | Description                         |
|--------|------------------------------|-------------------------------------|
| GET    | `/control`                   | Returns drop rate & emitter status  |
| POST   | `/control/drop-rate?value=0.2` | Sets the drop rate (0.0 - 1.0)      |
| POST   | `/control/enable`            | Enables call emission               |
| POST   | `/control/disable`           | Disables call emission              |

### Example:

```bash
curl -X POST http://localhost:8080/control/drop-rate?value=0.3
```

---

## 🧪 Running Locally

```bash
mvn quarkus:dev
```

Quarkus will automatically start a Kafka container using Dev Services. No manual setup needed.

---

## ☁️ Deploying to OpenShift

1. Build the JAR:
   ```bash
   ./mvnw clean package -DskipTests
   ```

2. Deploy via S2I or custom image:
   ```bash
   oc start-build <your-app> --from-file=target/quarkus-app/quarkus-run.jar
   ```

3. Expose a route if needed:
   ```bash
   oc expose svc/<your-app>
   ```

✅ Make sure to remove or configure TLS correctly for your route if you're not using HTTPS inside Quarkus.

---

## 📄 Example Call Record Format

```json
{
  "cell_id": "A1B2C3",
  "lat": 37.7749,
  "lng": -122.4194,
  "signal_strength": -78,
  "is_dropped": false,
  "timestamp": "2025-05-19T18:30:00Z"
}
```

---

## 📦 Tech Stack

- Quarkus (RESTEasy + Scheduler + Kafka)
- Kafka Dev Services
- OpenShift (for deployment)

---

## 📝 License

MIT License. Use it, break it, improve it.
