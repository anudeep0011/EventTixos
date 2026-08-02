# EventTix Frontend

## Vercel setup

1. Import repo EventTixos
2. **Root Directory must be:** `eventtix-frontend`
3. Framework: Next.js (auto)
4. Install command: leave default (`npm install`)
5. Build command: leave default (`npm run build`)
6. Env var:
   ```
   NEXT_PUBLIC_API_URL=https://YOUR-BACKEND-URL/api
   ```
7. Deploy

Do **not** set a custom install command with `cd eventtix-frontend` — Vercel is already in that folder when Root Directory is set.
