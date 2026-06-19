# RPSC Campaign Console

React + Firebase starter app for bulk multi-channel campaigns described in `Executive Summary.docx`.

## Features

- Firebase Auth login with per-user Firestore data.
- Provider settings for SendGrid, Twilio SMS, and Twilio WhatsApp.
- AES-GCM encrypted provider credentials stored only by Cloud Functions.
- Excel/CSV lead import with preview.
- Campaign builder with template placeholders like `{{Name}}`.
- One-click queue/send flow with consent confirmation.
- Provider health dashboard and message queue monitoring.
- Firestore security rules that isolate each user namespace.

## Setup

1. Install dependencies:
   ```bash
   npm install
   npm --prefix functions install
   ```

2. Copy `.env.example` to `.env` and fill Firebase web config.

3. Configure function secrets:
   ```bash
   firebase functions:secrets:set PROVIDER_ENCRYPTION_KEY
   firebase functions:secrets:set OPENAI_API_KEY
   ```

   `PROVIDER_ENCRYPTION_KEY` should be a 32-byte base64 value:
   ```bash
   node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"
   ```

4. Run locally:
   ```bash
   npm run dev
   firebase emulators:start --only auth,firestore,functions,hosting
   ```

5. Deploy:
   ```bash
   npm run build
   npm run functions:build
   firebase deploy --only hosting,firestore,functions
   ```

## Important Compliance Notes

Only send campaigns to contacts with valid opt-in. SMS and WhatsApp are regulated channels. WhatsApp outbound notifications may require approved templates outside the 24-hour customer service window.
