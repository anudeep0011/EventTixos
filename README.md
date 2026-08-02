# EventTixos — Event Discovery & Ticketing Platform

Production-oriented Eventbrite + Ticket Tailor style platform.

**Repo:** https://github.com/anudeep0011/EventTixos

## Stack
- Backend: Spring Boot 3.3 / Java 21, PostgreSQL, Redis, Flyway, JWT
- Frontend: Next.js 14 + Tailwind (Vercel-ready)
- Scanner: Expo React Native
- Payments: Razorpay + **Stripe** gateways with webhook signature verification

## Quick start
```bash
docker compose up -d
cd eventtix-backend && mvn spring-boot:run
cd eventtix-frontend && npm i && NEXT_PUBLIC_API_URL=http://localhost:8080/api npm run dev
```

## Deploy
- **Frontend → Vercel** (this folder `eventtix-frontend`)
- **Backend → Railway / Fly.io / Render** (Spring Boot JAR + managed Postgres/Redis)

See full implementation status in the source tree under `/home/workdir/artifacts` and subsequent commits.
