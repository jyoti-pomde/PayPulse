# PayPulse — Distributed Event-Driven Payment & Order Processing Engine

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Resilience4j](https://img.shields.io/badge/Fault%20Tolerance-Resilience4j%20Circuit%20Breaker-blue.svg)](https://resilience4j.readme.io/)
[![Architecture](https://img.shields.io/badge/Pattern-Event--Driven%20Saga-purple.svg)](https://microservices.io/patterns/data/saga.html)
[![React](https://img.shields.io/badge/Frontend-React%2019%20%2B%20Vite-61DAFB.svg?logo=react&logoColor=black)](https://react.dev/)
[![OpenAPI](https://img.shields.io/badge/API%20Docs-Swagger%20v3-green.svg?logo=swagger&logoColor=white)](http://localhost:8080/swagger-ui.html)

A distributed, event-driven payment and order processing engine designed around real-world FinTech workflows. Demonstrates asynchronous event streaming, FinTech idempotency protection, Resilience4j circuit breakers, token-bucket rate limiting, and distributed request tracing.

---

## 💡 Why I Built This Project

While monolithic CRUD applications are common for entry-level portfolios, modern enterprise tech companies (such as Amazon, Swiggy, and Razorpay) run on **distributed microservices and event-driven architectures**.

I built PayPulse to practically master the core distributed system challenges:
1. **Asynchronous Decoupling**: Decoupling order placement from payment processing so that slow external payment APIs never block the checkout flow.
2. **FinTech Idempotency**: Ensuring that rapid double-clicks, network retries, or duplicate webhooks can **never** result in a customer being double-charged.
3. **Cascading Failure Prevention**: Using Resilience4j Circuit Breakers to fail-fast and serve graceful fallbacks when a downstream payment gateway experiences high latency or outages.
4. **Distributed Observability**: Tracking transactions across services using unified `X-Correlation-ID` request headers and MDC logging.

---

## 🏗️ System Architecture & Workflow

```text
               +---------------------------------------------+
               |        React 19 + Vite FinTech UI          |
               |        (Live Stepper, Wallet, Chaos Panel)  |
               +----------------------+----------------------+
                                      |
                     HTTP REST / JSON | (X-Correlation-ID, Idempotency-Key)
                                      v
               +---------------------------------------------+
               |             API Gateway Layer               |
               |  - Token Bucket Rate Limiter (HTTP 429)     |
               |  - Correlation ID Injection & MDC Logging   |
               +----------------------+----------------------+
                                      |
                     +----------------+----------------+
                     |                                 |
                     v                                 v
        +-------------------------+       +-------------------------+
        |      Order Service      |       |  Payment & Wallet Svc   |
        |  - State Machine        |       |  - Idempotency Ledger   |
        |  - Product Catalog      |       |  - Atomic Deductions    |
        +------------+------------+       +------------+------------+
                     |                                 |
                     | Emits "OrderPlacedEvent"        | Emits "PaymentProcessedEvent"
                     +----------------+----------------+
                                      |
                                      v
               +---------------------------------------------+
               |         Asynchronous Event Bus              |
               |     (Decoupled Worker Thread Pool)          |
               +----------------------+----------------------+
                                      |
                     +----------------+----------------+
                     |                                 |
                     v                                 v
        +-------------------------+       +-------------------------+
        | Resilience4j Circuit    |       | Observability & Audit   |
        | Breaker (PaymentGateway)|       | Trail (Correlation Logs)|
        +-------------------------+       +-------------------------+
```

---

## ✨ Key Engineering Highlights

### 1. FinTech Idempotency Key Pattern
- Every payment request carries a unique `Idempotency-Key` header (UUID).
- Before charging the wallet, the system checks the `payment_transactions` ledger.
- If a duplicate key is detected, the system immediately returns the previous receipt with `idempotentReplay: true` without deducting funds a second time.
- *Try it live*: Use the **"Test Rapid-Fire Duplicate Payment"** button in the dashboard to see this verified in real-time.

### 2. Resilience4j Fault Tolerance (Circuit Breaker)
- Configured with a count-based sliding window (size 5, 50% failure rate threshold).
- When third-party gateway failures exceed the threshold, the circuit automatically trips from **CLOSED** to **OPEN**, preventing thread pool exhaustion.
- During outages, requests trigger an immediate, graceful fallback (`FALLBACK_CIRCUIT_OPEN`) instead of timing out.
- *Try it live*: Click **"Trigger Simulated Gateway Outage"** in the Chaos Engineering panel to watch the circuit trip live!

### 3. Choreographed Saga & Eventual Consistency
- Orders are initially recorded in `PENDING` state and an `OrderPlacedEvent` is published.
- The payment consumer asynchronously processes the transaction and publishes a `PaymentProcessedEvent`.
- The order state transitions to **`CONFIRMED`** on payment approval, or **`FAILED`** if balance is insufficient or the gateway is unavailable.

### 4. Token-Bucket Rate Limiter
- An in-memory token bucket filter guards the API gateway (120 token capacity with a refill rate of 20 tokens/sec).
- Protects downstream services from traffic spikes and floods, returning standard `HTTP 429 Too Many Requests`.

### 5. Distributed Request Tracing (`X-Correlation-ID`)
- A gateway filter inspects every incoming HTTP request for `X-Correlation-ID` (or generates one if absent).
- Propagated through SLF4J MDC across service calls and audit logs, enabling complete end-to-end request tracing.

---

## 🛠️ Tech Stack

### Backend
- **Language**: Java 21 LTS
- **Framework**: Spring Boot 3.3.5
- **Fault Tolerance**: Resilience4j (Circuit Breaker, Event Consumers)
- **Data Persistence**: Spring Data JPA, Hibernate, H2 In-Memory Database
- **API Documentation**: Springdoc OpenAPI 2.6.0 (Swagger UI v3)
- **Testing**: JUnit 5, Mockito, AssertJ

### Frontend
- **Framework**: React 19 + Vite 6
- **Styling**: Custom FinTech Dark Theme (Slate-950 + Emerald + Indigo accents)
- **Icons**: Lucide React
- **Animations**: Canvas Confetti, CSS Keyframe Glow Pulses
- **Networking**: Axios with automatic `X-Correlation-ID` interceptors

---

## 🚀 How to Run Locally

### Prerequisites
- **Java 21 LTS** (`java -version`)
- **Maven 3.8+** (`mvn -v`)
- **Node.js 18+** & **npm** (`node -v`)

### 1. Clone the Repository
```bash
git clone https://github.com/jyoti-pomde/PayPulse.git
cd PayPulse
```

### 2. Run Backend (Spring Boot 3)
```bash
cd backend
mvn spring-boot:run
```
- Backend starts at: `http://localhost:8080`
- Interactive Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 Database Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:paypulsedb`, User: `sa`, Password: *empty*)
- Actuator Health & Circuit Breaker metrics: `http://localhost:8080/actuator/health`

### 3. Run Frontend (React 19 + Vite)
In a separate terminal:
```bash
cd frontend
npm install
npm run dev
```
- Open browser at: `http://localhost:3000`

---

## 🧪 Interactive Live Demos in the UI

Once the application is running, you can explore the following live features:

1. **One-Click Order Stepper**: Click **"Buy Now"** on any peripheral in the catalog to watch the visual 3-step checkout animation (Order Created ➔ Idempotency Checked ➔ Payment Confirmed with confetti).
2. **Double-Deduction Protection**: Click **"Test Rapid-Fire Duplicate Payment"** to see two identical requests dispatched simultaneously with proof of idempotent caching.
3. **Live Circuit Breaker Trip**: Toggle **"Trigger Simulated Gateway Outage"** and place an order to see Resilience4j trip the circuit and return a clean fallback error.
4. **Instant Wallet Top-Up**: Add ₹500, ₹1,000, or custom funds with atomic balance updates.
5. **Distributed Audit Drawer**: Click **"Audit Trail"** in the top navigation to search and filter logs by service name or Correlation ID.

---

## 🧠 Challenges I Faced & Key Learnings

1. **Managing Idempotency in Concurrent Environments**:  
   *Challenge*: If two requests with the same key arrive at the exact same millisecond, both might pass the database lookup before either writes a record.  
   *Solution*: Added unique database constraints on `idempotency_key` combined with atomic checks in the service layer to prevent race conditions.

2. **Circuit Breaker State Visibility**:  
   *Challenge*: Making Resilience4j's internal circuit state transparent to the user without polling heavy Actuator endpoints.  
   *Solution*: Built a dedicated `/api/chaos/status` endpoint querying the `CircuitBreakerRegistry` directly, providing real-time state (`CLOSED`, `OPEN`, `HALF-OPEN`) to the React frontend.

3. **MDC Context Propagation in Async Threads**:  
   *Challenge*: Java's `ThreadLocal`-based MDC does not automatically propagate to `@Async` worker threads.  
   *Solution*: Passed the `correlationId` explicitly in event payload records (`OrderPlacedEvent`, `PaymentProcessedEvent`), ensuring audit logs remain correlated across thread boundaries.

---

## 📂 Project Structure

```text
PayPulse/
├── backend/
│   ├── src/main/java/com/jyoti/paypulse/
│   │   ├── audit/          # AuditLog model, repository, service, and trace endpoints
│   │   ├── config/         # OpenAPI, CORS, Async ThreadPool, DataInitializer
│   │   ├── events/         # Event models (OrderPlaced, PaymentProcessed), Publisher, Listeners
│   │   ├── gateway/        # Token-Bucket RateLimitingFilter, CorrelationIdFilter
│   │   ├── order/          # Order and Product domain, DTOs, service, controller
│   │   └── payment/        # Wallet and Transaction domain, Resilience4j Circuit Breaker, service
│   ├── src/main/resources/
│   │   └── application.yml # Resilience4j configs, H2 datasource, Actuator endpoints
│   └── pom.xml             # Spring Boot 3.3.5, Java 21, Resilience4j 2.2.0
├── frontend/
│   ├── src/
│   │   ├── api/            # Axios client with correlation ID injection
│   │   ├── components/     # WalletCard, ChaosCircuitBreakerCard, ProductCatalog, LiveOrderList, Modals
│   │   ├── App.jsx         # Main dashboard orchestrator
│   │   └── App.css         # FinTech dark design system
│   ├── package.json
│   └── vite.config.js
└── README.md
```

---

## 🔮 Future Enhancements
- [ ] Connect Apache Kafka cluster using Docker Compose for distributed multi-broker scaling
- [ ] Implement Redis-backed Distributed Lock (`Redisson`) for high-concurrency inventory reservation
- [ ] Integrate Stripe / Razorpay sandbox test webhook endpoints

---

## 👩‍💻 Author

**Jyoti Pomde**  
- Portfolio: [jyoti-pomde.github.io/Jyoti_portfolio](https://jyoti-pomde.github.io/Jyoti_portfolio/)  
- GitHub: [@jyoti-pomde](https://github.com/jyoti-pomde)  
- LinkedIn: [linkedin.com/in/jyotipomde](https://linkedin.com/in/jyotipomde)  
- Email: jyopomde0001@gmail.com
