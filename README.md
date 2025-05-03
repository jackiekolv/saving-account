# Saving Account API

A Spring Boot REST API for managing saving accounts with functionalities like customer login, teller authentication, money transfer (with verify & confirm), and monthly statements.

## Features

- Customer and Teller Authentication using JWT
- Money transfer between accounts with verify & confirm flow
- Statement retrieval per month
- In-memory cache (Caffeine) for transaction verification
- JPA/H2 for local development
- Test coverage using JUnit + MockMvc

## Getting Started

### Prerequisites

- Java 17+
- Maven 3+
- Docker (optional, for containerized deployment)

### Running the App

#### 1. Run locally:

```bash
mvn spring-boot:run
```

Access Swagger UI (if enabled): [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

#### 2. Run with Docker Compose:

Make sure Docker is installed. Then run:

```bash
docker compose up --build
```

This will start both the app and the in-memory database (if defined in docker-compose.yml).

### Postman Collection

The Postman Collection and Environment files are included in the `postman/` folder.

- `postman/saving-account-poc.postman_collection.json`
- `postman/saving-account-local.postman_environment.json`

You can import them into Postman to quickly try out the API endpoints.

## API Endpoints

### Auth
- `POST /api/auth/customer/register` – Register a new customer
- `POST /api/auth/customer/login` – Customer login
- `POST /api/auth/teller/login` – Teller login

### Account
- `POST /api/account/create?citizenId=...&deposit=...` – Create a new account for a customer (Teller role)
- `GET /api/account/info?citizenId=...` – Get account information

### Transaction
- `POST /api/transaction/deposit` – Deposit money into account (Teller role)
- `POST /api/transaction/transfer/verify` – Step 1: Verify transfer (Customer role, no PIN required)
- `POST /api/transaction/transfer/confirm` – Step 2: Confirm transfer with PIN (Customer role)

### Statement
- `GET /api/statement?accountNumber=...&year=...&month=...` – Retrieve monthly statement

Refer to included Postman Collection or Swagger for full API spec.

## Testing

Run all unit and integration tests:

```bash
mvn test
```

Generate test coverage report (e.g. using IntelliJ or Jacoco plugin).

## Sample Available Data

- **Customer #1**
  - citizenId: `1234567890101`
  - pin: `112233`
  - email: `jackie101@example.com`
  - password: `123456`

- **Customer #2**
  - citizenId: `1234567890102`
  - pin: `112233`
  - email: `john102@example.com`
  - password: `123456`

- **Teller**
  - citizenId: `1234567890501`
  - employeeId: `EMP501`
  - email: `teller501@bank.com`
  - password: `123456`

## Security & Token Usage

- All endpoints except `/auth/**` are secured using JWT.
- After call login API (for customer or teller), a JWT token will be returned in the response body.
- Include the token in the `Authorization` header using the format:

  ```
  Authorization: Bearer <your-token>
  ```

- You can use this token to call secured APIs such as `/account/info`, `/transaction/**`, and `/statement`.

## 🧑‍💻 How to Use (Step-by-step Guide)

This guide walks you through how to use the Saving Account system from login to making a transaction.

### 1. Register New Customer
- Send a `POST` request to `/api/auth/customer/register` with:
  ```json
  {
    "citizenId": "1234567890103",
    "email": "newuser@example.com",
    "password": "123456",
    "pin": "112233"
  }
  ```
- On success, you can login using the same credentials.

### 2. Customer Login
- Send a `POST` request to `/api/auth/customer/login` with:
  ```json
  {
    "citizenId": "1234567890101",
    "password": "123456"
  }
  ```
- Response will contain a JWT token.

### 3. Teller Login (Optional)
- Send `POST /api/auth/teller/login` with:
  ```json
  {
    "citizenId": "1234567890501",
    "password": "123456"
  }
  ```
- Use token to perform teller operations like deposits.

### 4. Create Account (Teller only)
- Send a `POST` request to `/api/account/create?citizenId=1234567890101&deposit=5000`
- Requires a JWT token from Teller login in the `Authorization` header:
  ```
  Authorization: Bearer <your-token>
  ```
- This will create a new account for the specified citizen ID with the initial deposit.

### 5. Teller Deposit
- Login as a teller (see step 3) to get token.
- Send a `POST` to `/api/transaction/deposit` with:
  ```json
  {
    "accountNumber": "1234101",
    "amount": 5000
  }
  ```
- This will deposit money into the target account.

### 6. Get Account Info
- Send `GET /api/account/info` with JWT to view account details.

### 7. Transfer Money
- **Step 1**: Send a `POST` to `/api/transaction/transfer/verify` with:
  ```json
  {
    "accountNumberFrom": "1234101",
    "accountNumberTo": "1234102",
    "amount": 1000,
    "channel": "MOBILEAPP"
  }
  ```
  - This will initiate the transfer and store verification in cache. *No PIN required at this step.*

- **Step 2**: Send a `POST` to `/api/transaction/transfer/confirm` within 60 seconds using:
  ```json
  {
    "accountNumberFrom": "1234101",
    "accountNumberTo": "1234102",
    "amount": 1000,
    "channel": "MOBILEAPP",
    "pin": "112233"
  }
  ```
  - The confirm request must match the verify request, with an additional correct `pin` value.

### 8. View Monthly Statement
- Send a `GET` request to:
  ```
  /api/statement?accountNumber=1234101&year=2025&month=5
  ```

## Notes

- Transaction verification uses Caffeine cache, default TTL = 60s.
- Confirm API will fail if cache expired or request mismatches.
- All secure endpoints require JWT token in `Authorization` header.
- You can also find a ready-to-use Postman Collection in the `/postman` folder.

---
© 2025 Saving Account Demo