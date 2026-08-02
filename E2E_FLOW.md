# EventTix — End-to-end flow

## Happy path (demo / mock payments)

1. **Signup / Login** → `POST /api/auth/signup` or `/login`
2. **Become organizer** → `POST /api/auth/become-organizer`
3. **Create event** → `POST /api/organizer/events`
4. **Add tier** → `POST /api/organizer/events/{id}/ticket-tiers`
5. **Publish** → `POST /api/organizer/events/{id}/publish`
6. **Browse** → `GET /api/events` / `GET /api/events/{slug}`
7. **Hold** → `POST /api/checkout/hold` `{ tierId, quantity, sessionId }`
   - Redis hold 10 min; does **not** decrement `quantity_sold`
8. **Create intent** → `POST /api/checkout/create-payment-intent`
   - Creates `Order(PENDING)` with `tier_id`, `quantity`, `hold_key`
9. **Confirm (mock)** → `POST /api/checkout/confirm-mock` `{ orderId }`
   - Sets PAID, increments `quantity_sold` under row lock
   - Generates HMAC-signed tickets
   - Async email with QR PDF (if mail configured)
10. **Check-in** → `POST /api/checkin/scan` `{ qrPayload, eventId }`

## Production payments

- Set `RAZORPAY_KEY_*` or `STRIPE_SECRET_KEY`
- Skip confirm-mock; complete via provider UI
- Webhook `POST /api/webhooks/razorpay|stripe` calls same `finalizePaidOrder`

## Local run

```bash
docker compose up -d   # needs POSTGRES_PASSWORD, REDIS_PASSWORD, JWT_SECRET, QR_HMAC_SECRET in .env
cd eventtix-backend && mvn spring-boot:run
cd eventtix-frontend && npm i && npm run dev
```
