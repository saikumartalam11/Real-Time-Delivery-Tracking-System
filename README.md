# Real-Time-Delivery-Tracking-System
Real-Time Delivery Tracking System
# Real-Time Delivery Tracking System

## Overview

Real-Time Delivery Tracking System is a microservice scaffold demonstrating a production-oriented architecture for live delivery tracking (similar to Swiggy/Uber). It includes order management, delivery partner management, real-time tracking via WebSockets (STOMP), Redis caching for live locations, ETA calculation, and notification delivery.

## Architecture

```mermaid
flowchart LR
	A[Client App]
	A -->|REST| API[API Gateway]
	API -->|Eureka discovery| OrderService[Order Service]
	API --> PartnerService[Partner Service]
	API --> TrackingService[Tracking Service]
	API --> NotificationService[Notification Service]
	OrderService -->|Postgres| Postgres[(PostgreSQL)]
	PartnerService -->|Postgres| Postgres
	TrackingService -->|Redis| Redis[(Redis)]
	TrackingService -->|broadcast STOMP| ClientWS[Client WS (STOMP/SockJS)]
	TrackingService --> NotificationService
	Eureka["Eureka Server"] -. discovered by .-> API
	OrderService -. discovered by .-> Eureka
	PartnerService -. discovered by .-> Eureka
	TrackingService -. discovered by .-> Eureka
	NotificationService -. discovered by .-> Eureka
```

## Tech stack

- Java 17
- Spring Boot (per-service)
- Spring Cloud (Eureka, Spring Cloud Gateway)
- Spring WebSocket (STOMP + SockJS)
- Spring Data Redis (Lettuce)
- PostgreSQL (orders data)
- Docker & Docker Compose

## Services

- `eureka-server` — service registry
- `api-gateway` — Spring Cloud Gateway (service discovery + routing)
- `order-service` — create orders, assign partners (Postgres)
- `partner-service` — manage delivery partners (Postgres)
- `tracking-service` — receive partner location updates, cache in Redis, compute ETA, broadcast via STOMP
- `notification-service` — receive and log/send notifications

## Setup (local)

Prerequisites: Docker (or Docker Desktop), Maven, Java 17

1) Build service artifacts (from repository root):

```bash
mvn -f eureka-server/pom.xml clean package
mvn -f api-gateway/pom.xml clean package
mvn -f order-service/pom.xml clean package
mvn -f partner-service/pom.xml clean package
mvn -f tracking-service/pom.xml clean package
mvn -f notification-service/pom.xml clean package
```

2) Start infra and services with Docker Compose:

```bash
cd infra
docker compose up --build
```

Services will be available on:

- API Gateway: http://localhost:8080
- Eureka Console: http://localhost:8761

## API Endpoints

Order Service (via API Gateway prefix `order-service`):

- Create order
	- POST /order-service/orders
	- Body: { "customerName": "Alice", "pickupLat": number, "pickupLng": number, "dropoffLat": number, "dropoffLng": number }

- Get order
	- GET /order-service/orders/{orderId}

- Assign partner
	- POST /order-service/orders/{orderId}/assign/{partnerId}

Partner Service:

- Create partner
	- POST /partner-service/partners
	- Body: { "name": "Bob", "lat": number, "lng": number }

- Update partner location (simple REST on partner service)
	- POST /partner-service/partners/{id}/location

Tracking Service:

- Partner location update (primary ingestion)
	- POST /tracking-service/tracking/update/{orderId}
	- Body: { "partnerId": number, "lat": number, "lng": number }
	- Behavior: caches partner location in Redis, calculates ETA to dropoff, broadcasts to subscribers, and sends ETA notification to Notification Service.

- Get latest tracking state
	- GET /tracking-service/tracking/latest/{orderId}

WebSocket (STOMP through API Gateway):

- STOMP endpoint: ws://localhost:8080/tracking-service/ws/track (SockJS fallback enabled)
- Subscribe to order topic: `/topic/orders/{orderId}` to receive JSON updates with `type: location` payloads.

Notification Service:

- POST /notification-service/notifications — receives notifications (eta, assignment)

## Redis caching

- `tracking-service` stores partner locations keyed by `partner:{partnerId}` with a configurable TTL (default 120s) using JSON serialization.

## ETA calculation

- ETA uses the Haversine formula to compute great-circle distance and estimates time assuming a configurable average speed (default 30 km/h). Implementation in `tracking-service/src/main/java/com/example/trackingservice/service/EtaService.java`.

## Docker

- Each service includes a `Dockerfile` that packages the Spring Boot fat jar.
- `infra/docker-compose.yml` composes: Postgres, Redis, Eureka, API Gateway, and all services.

## Run example (quick)

1) Create a partner

```bash
curl -X POST http://localhost:8080/partner-service/partners -H 'Content-Type: application/json' -d '{"name":"Bob","lat":12.9,"lng":77.5}'
```

2) Create an order

```bash
curl -X POST http://localhost:8080/order-service/orders -H 'Content-Type: application/json' -d '{"customerName":"Alice","pickupLat":12.9,"pickupLng":77.5,"dropoffLat":12.95,"dropoffLng":77.6}'
```

3) Partner updates location for order

```bash
curl -X POST http://localhost:8080/tracking-service/tracking/update/1 -H 'Content-Type: application/json' -d '{"partnerId":1,"lat":12.92,"lng":77.52}'
```

4) Client subscribe (JS + SockJS + Stomp):

```js
const socket = new SockJS('http://localhost:8080/tracking-service/ws/track');
const client = Stomp.over(socket);
client.connect({}, () => {
	client.subscribe('/topic/orders/1', msg => console.log(JSON.parse(msg.body)));
});
```

## Project resume description (copy-paste)

Real-Time Delivery Tracking System — a Spring Boot microservices prototype implementing real-time delivery tracking using WebSockets (STOMP), Redis for live location caching, PostgreSQL for transactional order data, and Spring Cloud for service discovery and gateway routing. Key features: order lifecycle management, partner assignment, live GPS ingestion, ETA calculation via Haversine formula, STOMP-based broadcast to subscribed clients, and notification delivery. Built with production concerns in mind (containerized services, externalized configuration, small service boundaries) and designed for easy extension (geofencing, route optimization, map integrations).

## Notes & next steps

- Add authentication/authorization (JWT, OAuth2)
- Replace simple notification-service with real push channels (FCM, SMS, email)
- Scale WebSocket broker with a message broker (RabbitMQ/Redis pub-sub) for multi-instance horizontal scaling
- Add metrics, tracing, circuit breakers, and resilience patterns

---
Small components live under each service folder; see their `src/main` for controllers, services, and configs.

