# Enterprise Integration Gateway & Webhook Synchronization Engine

**Author:** Piyush Kumar  
**Repository:** [github.com/PIYUSHKUMAR371/integration-gateway](https://github.com/PIYUSHKUMAR371/integration-gateway)  
**Primary Tech Stack:** Java 17, Spring Boot 3, Spring Data JPA, HMAC SHA-256 Security, H2/PostgreSQL, Lombok, Jackson, Maven

---

## 📌 Executive Summary & Problem Statement

In enterprise backend ecosystems, core applications constantly interface with third-party software vendor platforms (e.g., Stripe, Salesforce, GitHub, Workday, ServiceNow). Ingesting external HTTP webhooks introduces three major production challenges:

1. **Duplicate Execution & Replay Attacks (Idempotency Risks):** External vendors employ automated network retry policies. If a transient network blip occurs, vendors resend webhooks multiple times. Without strict **Idempotency controls**, duplicate payloads cause severe data corruption, duplicate financial charges, or redundant workflow executions.
2. **Cryptographic Payload Forgery & Timing Side-Channel Attacks:** Public API endpoints receiving external webhooks must verify payload authenticity without exposing the system to cryptographic **timing side-channel exploits**.
3. **Data Schema Fragmentation:** Heterogeneous vendor platforms emit events in distinct JSON structures. Core internal enterprise services require a single, normalized canonical domain schema.

### Solution Delivered
This project implements an **Enterprise Integration Gateway** middleware backend. It cryptographically authenticates incoming third-party HTTP requests using **HMAC SHA-256** signatures, enforces **$O(1)$ database-indexed Idempotency deduplication**, transforms mismatched vendor payloads into standardized internal canonical models using the **Adapter Design Pattern**, and maintains an immutable audit log.

---

## 🏗️ System Architecture & Workflow

```text
[ External Vendor ] ──► HTTP POST /api/v1/webhooks/ingest
 (Stripe / GitHub)               │
                                 ▼
                     [ WebhookController ]
                                 │
                                 ▼
                   [ IntegrationGatewayService ]
                                 │
        ┌────────────────────────┼────────────────────────┐
        ▼                        ▼                        ▼
 [ HmacValidator ]       [ Idempotency check ]   [ Payload Transformer ]
(SHA-256 Signature)     (O(1) DB Index Lookup)    (Adapter Design Pattern)
        │                        │                        │
        ▼                        ▼                        ▼
 (Reject 401 if bad)    (Skip if Duplicate)     (Normalize to Canonical)
                                 │
                                 ▼
                     [ System Audit Logging ]
