# login-service

Authenticates a user against the `Users` DynamoDB table.

## What it does
- Fetches user record by `loginId` (PK `UserId`)
- Compares plaintext password with stored BCrypt hash
- Returns a **dev token** (not JWT yet) so the UI can proceed

## Endpoints
- `POST /api/v1/login`
- `GET /api/v1/health`

### Request body
```json
{ "loginId": "rahul", "password": "Min8Chars!" }
```

### Success response
```json
{
  "userId": "rahul",
  "token": "mock-<uuid>",
  "issuedAt": "2026-01-29T..."
}
```

### Failure
HTTP 401 with message `Invalid loginId or password`

## DynamoDB table
`Users` (same as signup-service)

## Run
```bash
./gradlew bootRun
```

Config (defaults in `application.yml`):
- `app.aws.region`
- `app.tables.users`
