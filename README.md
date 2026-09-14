# Enterprise Integration Gateway & Webhook Synchronization Engine

**Author:** Piyush Kumar  
**Repository:** [github.com/PIYUSHKUMAR371/integration-gateway](https://github.com/PIYUSHKUMAR371/integration-gateway)  
**Target Role:** Google Application Engineering Intern (2027 Cycle)  
**Primary Tech Stack:** Java 17 (LTS), Spring Boot 3.2.x, Spring Data JPA, Spring Web, HMAC SHA-256 Security, H2/PostgreSQL, Jackson, Lombok, Maven

---

## 📌 Executive Summary & Core Architectural Problem

In high-scale enterprise backend infrastructure (such as Google’s internal business systems), applications constantly interface with third-party software vendor platforms (e.g., Stripe, Salesforce, Workday, ServiceNow, GitHub, Twilio). 

Integrating external systems via HTTP webhooks presents three major production challenges:

1. **Duplicate Execution & Replay Vulnerabilities (Idempotency):** External vendors employ network retry policies. If a transient network glitch occurs after a vendor sends an event, they will resend the webhook multiple times. Without strict **Idempotency controls**, processing duplicate payloads leads to severe data corruption, duplicate financial charges, or redundant workflow executions.
2. **Cryptographic Payload Forgery & Timing Side-Channel Attacks:** Public API endpoints receiving external webhooks must verify that payloads truly originated from trusted vendor servers without being tampered with in transit. Naive string verification is vulnerable to cryptographic **timing side-channel exploits**.
3. **Data Schema Fragmentation (Adapter Pattern Needs):** Different vendor platforms emit event payloads in completely distinct JSON structures. Core internal enterprise domain services should never be coupled to third-party vendor JSON schemas.

### Solution Delivered
This project implements an **Enterprise Integration Gateway** middleware backend. It cryptographically authenticates incoming third-party HTTP requests using **HMAC SHA-256** signatures, enforces **$O(1)$ database-indexed Idempotency deduplication**, transforms heterogeneous vendor payloads into standardized internal canonical models using the **Adapter Design Pattern**, and maintains an immutable execution audit log.

---

## 🏗️ Architectural Workflow & Data Processing Pipeline

```text
[ External Vendor ] ──► ( HTTP POST /api/v1/webhooks/ingest )
(Stripe / Salesforce / GitHub)        │
                                      ▼
                        +-------------+-------------+
                        |      WebhookController    |
                        +-------------+-------------+
                                      |
                                      v
                        +-------------+-------------+
                        | IntegrationGatewayService |
                        +-------------+-------------+
                                      |
       +------------------------------+------------------------------+
       |                              |                              |
       v                              v                              v
+------+-------+              +-------+------+              +--------+-----+
| HmacValidator|              |  Idempotency |              |  Payload     |
|  (SHA-256)   |              |   Service    |              |  Transformer |
+------+-------+              +-------+------+              +--------+-----+
       |                              |                              |
 (Verify Header)               (Check Indexed DB)            (Adapter Pattern)
       |                              |                              |
       +---------------+--------------+------------------------------+
                       |
                       v
         +-------------+-------------+
         |  Database Persistence &   |
         |     System Audit Log      |
         +---------------------------+
