# PinDrop Backend (DynamoDB)

This backend is cleaned up to use AWS DynamoDB (region: us-east-2) and insert items into the `Pins` table.

## Prereqs (local)
1. Java 17+
2. AWS CLI configured (default profile)
   - `aws configure`
   - `aws sts get-caller-identity --region us-east-2`
3. DynamoDB table exists: `Pins` with partition key `pinId` (String)

## Run
- Start Spring Boot from IntelliJ, or:
  - `./gradlew bootRun`

## Test insert (POST)
Endpoint:
- `POST http://localhost:8080/pins`

Sample body:
```json
{
  "userId": "u1",
  "title": "Test Pin",
  "description": "hello"
}
```

The backend generates `pinId` and `createdAt` if not provided.

Health check:
- `GET http://localhost:8080/pins/health`
