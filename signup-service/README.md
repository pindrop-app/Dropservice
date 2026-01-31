# signup-service

Creates a new user in DynamoDB.

## What it does
- Hashes password using BCrypt
- Writes a user record into the `Users` DynamoDB table
- Prevents duplicate signup with the same `loginId`

## Endpoints
- `POST /api/v1/signup`
- `POST /api/v1/register/user` (backward compatible)

### Request body
```json
{
  "loginId": "rahul",
  "email": "rahul@example.com",
  "password": "Min8Chars!",
  "dob": "1990-01-01"
}
```

### Response
```json
{ "loginId": "rahul" }
```

## DynamoDB table
`Users`
- PK: `UserId` (String)
- Attributes: `Email`, `Password` (BCrypt hash), `DOB`, `CreatedAt`, `Status`

## Run
```bash
./gradlew bootRun
```

Config (defaults in `application.yml`):
- `app.aws.region`
- `app.tables.users`
