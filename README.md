# EventTixos — Event Discovery & Ticketing

Production-oriented Eventbrite / Ticket Tailor style platform.

**Repo:** https://github.com/anudeep0011/EventTixos

## Stack
| Layer | Tech |
|-------|------|
| Frontend | Next.js 14 + Tailwind → **Vercel** |
| Backend | Spring Boot 3.3 / Java 21 → **Railway / Render** |
| Data | PostgreSQL + Redis + Flyway |
| Payments | Razorpay + Stripe (or MOCK for demo) |

## Monorepo layout
```
eventtix-frontend/   # Vercel root directory
eventtix-backend/    # Railway/Render root directory
eventtix-scanner/    # Expo QR scanner
```

## Production docs
- PRODUCTION.md — env vars, deploy order, security checklist
- E2E_FLOW.md — hold → pay → tickets → check-in
- eventtix-backend/DEPLOY.md — Railway/Render settings

## Local
```bash
cp .env.example .env
docker compose up -d
cd eventtix-backend && mvn spring-boot:run
cd eventtix-frontend && npm i && NEXT_PUBLIC_API_URL=http://localhost:8080/api npm run dev
```

## Deploy tip
If Railway Docker build fails, push the complete `eventtix-backend/src` tree from your local workspace.
