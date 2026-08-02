# EventTix — Production readiness

## Stack
- Frontend: Next.js on Vercel (`eventtix-frontend`)
- Backend: Spring Boot 3.3 / Java 21 on Railway or Render (`eventtix-backend`)
- Data: Managed PostgreSQL + Redis

## Required env
```
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://...
DATABASE_USERNAME=...
DATABASE_PASSWORD=...
REDIS_HOST=...
REDIS_PORT=...
REDIS_PASSWORD=...
JWT_SECRET=<openssl rand -hex 32>
QR_HMAC_SECRET=<openssl rand -hex 32>
COOKIE_SECURE=true
CORS_ALLOWED_ORIGINS=https://your-app.vercel.app
PORT=8080
```

Optional: STRIPE_*, RAZORPAY_*, MAIL_*

## Deploy order
1. Postgres + Redis
2. Backend root `eventtix-backend` (Dockerfile)
3. Health: GET /actuator/health
4. Vercel root `eventtix-frontend` + NEXT_PUBLIC_API_URL
5. CORS + payment webhooks

## E2E
Signup → become organizer → create/publish event → hold → intent → confirm-mock/webhook → tickets → check-in

## Security
Flyway-only schema, JWT refresh rotation, HMAC QR, Redis holds, rate limits, non-root Docker, no default passwords.
