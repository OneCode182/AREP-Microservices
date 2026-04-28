# AREP Twitter — Secure Microservices Application

![Java](https://img.shields.io/badge/Java-17+-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?logo=spring-boot&logoColor=white)
![React](https://img.shields.io/badge/React-18+-61DAFB?logo=react&logoColor=black)
![Vite](https://img.shields.io/badge/Vite-5+-646CFF?logo=vite&logoColor=white)
![AWS Lambda](https://img.shields.io/badge/AWS_Lambda-Serverless-FF9900?logo=awslambda&logoColor=white)
![Auth0](https://img.shields.io/badge/Auth0-Security-EB5424?logo=auth0&logoColor=white)
![DynamoDB](https://img.shields.io/badge/DynamoDB-Storage-4053D6?logo=amazondynamodb&logoColor=white)

> [!NOTE]
> _"Please enable audio in the demo video to hear the full demonstration and instructions."_
<video src="https://github.com/user-attachments/assets/bd84d922-f68a-46a1-85dc-0d52cfca8fb2" controls="controls" style="max-width: 100%;">
</video>




## Description

AREP Twitter is a simplified Twitter-like application built as a university project for the AREP course at Escuela Colombiana de Ingeniería Julio Garavito. Users can create posts of up to 140 characters and view a public stream of all posts. Authentication is handled entirely by **Auth0** using OAuth2/OIDC — no sign-up forms, no passwords. Users log in through Auth0's Universal Login and receive a JWT that the backend validates on every protected request.



The project evolves through three phases:
1. **Monolith** — Spring Boot + H2 database (local development)
2. **Frontend SPA** — React + Vite + TypeScript with Auth0 integration
3. **Microservices on AWS** — Three Lambda functions behind API Gateway with DynamoDB, frontend hosted on S3

## Architecture

### General Architecture

![architecture](./media/architecture.png)

### Monolith (Local Development)

```mermaid
graph TD
    Browser -->|HTTP| SB["Spring Boot :8080"]
    SB --> SC["SecurityConfig\nJWT Validation"]
    SC -->|public| Stream["GET /api/stream"]
    SC -->|public| PostsGet["GET /api/posts"]
    SC -->|JWT| PostsPost["POST /api/posts"]
    SC -->|JWT| UserMe["GET /api/me"]
    Stream --> PS["PostService"]
    PostsGet --> PS
    PostsPost --> PS
    PS --> H2[("H2 Database")]
    Auth0["Auth0"] -->|issues JWT| Browser
```

### Auth0 Authentication Flow

```mermaid
sequenceDiagram
    participant U as User
    participant SPA as React SPA
    participant A0 as Auth0
    participant API as API Gateway + Lambda

    U->>SPA: clicks Login
    SPA->>A0: redirect to Universal Login
    A0->>U: login page
    U->>A0: authenticates
    A0->>SPA: authorization code
    SPA->>A0: exchange for tokens
    A0->>SPA: access_token (JWT) + id_token
    SPA->>API: POST /api/posts (Bearer JWT)
    API->>A0: validate JWT
    A0-->>API: valid
    API->>SPA: 201 Created
```

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Local Setup](#local-setup)
3. [Running Locally](#running-locally)
4. [AWS Deployment](#aws-deployment)
5. [Testing](#testing)
6. [Project Structure](#project-structure)
7. [Links](#links)
8. [Evaluation Rubric](#evaluation-rubric)
9. [Author](#author)

## Links

| Component | URL |
|---|---|
| **Frontend (S3)** | https://arep-twitter-frontend-sergiosilva.s3.us-west-2.amazonaws.com/index.html |
| **Backend API (API Gateway)** | https://xgoucasuwj.execute-api.us-west-2.amazonaws.com/prod |
| **Public Stream (no auth)** | https://xgoucasuwj.execute-api.us-west-2.amazonaws.com/prod/api/stream |
| **Auth0 Tenant** | https://onecode1.us.auth0.com |
| **Swagger UI (local only)** | http://localhost:8080/swagger-ui.html |

## Prerequisites

- Java 17 (OpenJDK or Oracle)
- Maven 3.9+
- Node.js 18+ with npm
- Auth0 tenant — configured at `onecode1.us.auth0.com`
- AWS account with permissions for Lambda, API Gateway, DynamoDB, and S3

## Local Setup

### 1. Clone and navigate

```bash
git clone <repo-url>
cd AREP-Microservices
```

### 2. Set environment variables

```bash
# Linux / macOS
export AUTH0_DOMAIN=onecode1.us.auth0.com
export AUTH0_AUDIENCE=https://onecode1.us.auth0.com/api/v2/

# Windows (Command Prompt)
set AUTH0_DOMAIN=onecode1.us.auth0.com
set AUTH0_AUDIENCE=https://onecode1.us.auth0.com/api/v2/

# Windows (PowerShell)
$env:AUTH0_DOMAIN="onecode1.us.auth0.com"
$env:AUTH0_AUDIENCE="https://onecode1.us.auth0.com/api/v2/"
```

> **Note:** The `monolith/src/main/resources/application.yml` already contains the hardcoded values for convenience. The environment variables above are only needed if you override the defaults.

### 3. Build monolith

```bash
cd monolith
mvn clean install
```

### 4. Install frontend dependencies

```bash
cd ../frontend
npm install
```

## Running Locally

### Start monolith (Spring Boot)

```bash
cd monolith
mvn spring-boot:run
# Backend runs on http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
# H2 Console: http://localhost:8080/h2-console
```

### Start frontend (React dev server)

```bash
cd frontend
npm run dev
# Frontend runs on http://localhost:5173
# API proxy: /api/* → http://localhost:8080
```

### Test API with curl

```bash
# Public endpoint — no token needed
curl http://localhost:8080/api/stream

# Get JWT from Auth0 (Machine-to-Machine test token)
# Then call protected endpoint:
curl -H "Authorization: Bearer <ACCESS_TOKEN>" http://localhost:8080/api/me

# Create a post (authenticated)
curl -X POST http://localhost:8080/api/posts \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"content": "Hello from curl!"}'
```

## AWS Deployment

### Deployment Status

All components are deployed and operational:

| Component | Service | Status |
|---|---|---|
| **Backend API** | API Gateway + 3 Lambda functions | Live |
| **Database** | DynamoDB (`arep-twitter-posts`) | Live |
| **Frontend** | S3 Static Website Hosting | Live |
| **Authentication** | Auth0 (OAuth2/OIDC) | Configured |

### Deployed Resources

- **API Gateway**: `https://xgoucasuwj.execute-api.us-west-2.amazonaws.com/prod`
- **Lambda Functions**:
  - `UserFunction` → `GET /api/me` (returns user profile from JWT)
  - `PostsFunction` → `POST /api/posts` (creates a post, stores in DynamoDB)
  - `StreamFunction` → `GET /api/stream`, `GET /api/posts` (reads all posts from DynamoDB)
- **DynamoDB Table**: `arep-twitter-posts` (on-demand billing, partition key: `id`, sort key: `createdAt`)
- **S3 Bucket**: `arep-twitter-frontend-sergiosilva` (static website hosting enabled)

### Auth0 Configuration

Application Client ID: `EWtdI5Fnwx8BkAEBHjseTmclhSwMObl4`

| Auth0 Setting | Values |
|---|---|
| **Allowed Callback URLs** | `http://localhost:5173`, S3 frontend URL |
| **Allowed Logout URLs** | `http://localhost:5173`, S3 frontend URL |
| **Allowed Web Origins** | `http://localhost:5173`, S3 frontend URL |
| **Allowed Origins (CORS)** | `http://localhost:5173`, S3 frontend URL |

## Testing

### Phase 1 (Monolith) — Automated Tests

```bash
cd monolith
mvn test
```

**Results: 6/6 tests pass**

| Test | Description | Result |
|------|-------------|--------|
| `shouldReturnStreamWithoutAuth` | `GET /api/stream` returns 200 without JWT | PASS |
| `shouldRejectPostWithoutJwt` | `POST /api/posts` without Authorization returns 401 | PASS |
| `shouldRejectPostOver140Chars` | `POST /api/posts` with 141-char content returns 400 | PASS |
| `shouldCreatePostWithValidJwt` | `POST /api/posts` with mock JWT returns 201 + post body | PASS |
| `shouldReturnUserInfoWithJwt` | `GET /api/me` with mock JWT returns sub/email/name | PASS |
| `postServiceCreatesPost` | Unit test: `PostService.createPost()` sets correct fields | PASS |

### Phase 2 (Frontend) — Manual Testing Checklist

Start both `mvn spring-boot:run` (port 8080) and `npm run dev` (port 5173), then:

1. Open `http://localhost:5173` — dark theme, navbar with Login button visible
2. Click **Login** → redirects to Auth0 Universal Login
3. Authenticate → returns to `http://localhost:5173` with user profile shown
4. Create a post (≤140 chars) → character counter shows green → click **Post** → post appears in stream
5. Type 141+ chars → counter turns red → **Post** button disabled
6. Click **Refresh** on stream → posts reload
7. Click **Logout** → Auth0 logout → redirected to origin, Login button restored
8. Try to POST without being logged in → 401 returned, error toast shown

### Phase 3 (Lambda) — Compile Verification

```bash
# All three compile successfully:
ls microservices/user-function/target/user-function-0.0.1-SNAPSHOT.jar    # exists
ls microservices/posts-function/target/posts-function-0.0.1-SNAPSHOT.jar  # exists
ls microservices/stream-function/target/stream-function-0.0.1-SNAPSHOT.jar # exists
```

### Phase 3 (Lambda) — Optional Local Testing

```bash
cd infrastructure
sam local start-api
# Simulates API Gateway + Lambda locally
# Endpoints available at http://localhost:3000/api/...
```

## Project Structure

```
AREP-Microservices/
├── monolith/                              # Phase 1: Spring Boot (H2 + Auth0)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/edu/eci/arep/twitter/
│   │   │   │   ├── TwitterApplication.java
│   │   │   │   ├── config/
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   ├── AudienceValidator.java
│   │   │   │   │   ├── CorsConfig.java
│   │   │   │   │   └── OpenApiConfig.java
│   │   │   │   ├── controller/
│   │   │   │   │   ├── PostController.java
│   │   │   │   │   ├── StreamController.java
│   │   │   │   │   └── UserController.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── CreatePostRequest.java
│   │   │   │   │   └── ErrorResponse.java
│   │   │   │   ├── model/
│   │   │   │   │   └── Post.java
│   │   │   │   ├── repository/
│   │   │   │   │   └── PostRepository.java
│   │   │   │   └── service/
│   │   │   │       └── PostService.java
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   │       └── java/edu/eci/arep/twitter/
│   │           └── PostControllerTest.java
│   └── pom.xml
├── frontend/                              # Phase 2: React + Vite + TypeScript
│   ├── src/
│   │   ├── components/
│   │   │   ├── Navbar.tsx
│   │   │   ├── LoginButton.tsx
│   │   │   ├── LogoutButton.tsx
│   │   │   ├── PostForm.tsx
│   │   │   ├── StreamFeed.tsx
│   │   │   └── UserProfile.tsx
│   │   ├── hooks/
│   │   │   └── useApi.ts
│   │   ├── App.tsx
│   │   ├── main.tsx
│   │   └── index.css
│   ├── index.html
│   ├── package.json
│   ├── tsconfig.json
│   └── vite.config.ts
├── microservices/                         # Phase 3: AWS Lambda handlers
│   ├── user-function/
│   │   ├── src/main/java/edu/eci/arep/twitter/lambda/
│   │   │   └── UserHandler.java
│   │   └── pom.xml
│   ├── posts-function/
│   │   ├── src/main/java/edu/eci/arep/twitter/lambda/
│   │   │   └── PostsHandler.java
│   │   └── pom.xml
│   └── stream-function/
│       ├── src/main/java/edu/eci/arep/twitter/lambda/
│       │   └── StreamHandler.java
│       └── pom.xml
├── infrastructure/
│   └── template.yaml                      # SAM IaC (Lambda + API GW + DynamoDB)
├── README.md
└── .gitignore
```


## Author

**Sergio Andrey Silva Rodriguez**
Escuela Colombiana de Ingeniería Julio Garavito — AREP 2026

## License

MIT
