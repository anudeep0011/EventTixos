# Backend deploy (Railway / Render)

## Why the image build failed
GitHub was missing most of `eventtix-backend` (Dockerfile, pom, sources).
Those are being restored on `main`. After a full push, rebuild.

## Railway
1. New Project → GitHub → `EventTixos`
2. Add **PostgreSQL** + **Redis** plugins
3. Service settings:
   - **Root Directory:** `eventtix-backend`
   - **Builder:** Dockerfile
4. Variables:
```
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://HOST:PORT/railway
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
5. Healthcheck: `/actuator/health`

## Render
1. Web Service → root `eventtix-backend`, Docker
2. Attach Postgres + Redis
3. Same env vars as above

## Push full tree from your machine (recommended)
```bash
git clone https://github.com/anudeep0011/EventTixos.git
cd EventTixos
# copy complete local eventtix-backend over this folder
git add -A
git commit -m "Complete backend sources for Docker build"
git push origin main
```
Then redeploy on Railway/Render.
