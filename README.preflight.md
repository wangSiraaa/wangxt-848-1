# Trae Preflight

This folder is prepared for `wangxt-848-1`.

Use `.env` for stable local ports and compose project identity:

- APP_PORT: 18148
- API_PORT: 19148
- WEB_PORT: 20148
- DB_PORT: 21148
- REDIS_PORT: 22148

Smoke entry:

```bash
bash scripts/smoke.sh
```

The preflight files are environment scaffolding only. The generated business
project can replace or extend them when needed.
