# VMS Backend

Spring Boot REST API for the Vehicle Management System (VMS) frontend.

## Deployment information

Store these public URLs and non-secret database identifiers with your project notes.
Never store database passwords, JWT secrets, administrator passwords, or Render deploy hooks in Git.

| Item | Value |
| --- | --- |
| GitHub repository | `https://github.com/0jayer/vms-backend` |
| Backend service | `vms-backend` |
| Public backend URL | `https://vms-backend-suqd.onrender.com` |
| Health endpoint | `https://vms-backend-suqd.onrender.com/api/health` |
| Frontend repository | `https://github.com/0jayer/vms-frontend` |
| Public frontend URL | `https://vmsfrontend.netlify.app` |
| Database provider | Render PostgreSQL |
| Database region | Singapore (Southeast Asia) |
| Database name | Record the database name from Render |
| Database username | Record the database username in Render |
| Database internal hostname | Record the hostname from the Internal Database URL |
| Database port | `5432` |

Keep the following values in a password manager or private project document:

- Database password
- JWT secret
- Administrator username
- Administrator password
- Render deploy hook

## Requirements

- Java 25
- Maven, through the included Maven Wrapper
- PostgreSQL 14 or newer
- Docker Desktop, optional for containerized local and Render deployment
- Windows PowerShell examples are provided below

No secrets should be committed to Git.

## Environment variables

| Variable | Purpose |
| --- | --- |
| `DB_URL` | PostgreSQL JDBC URL, for example `jdbc:postgresql://localhost:5432/vms_db` |
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |
| `JWT_SECRET` | Random value containing at least 32 characters |
| `ADMIN_USERNAME` | Initial administrator username |
| `ADMIN_PASSWORD` | Strong initial administrator password |
| `APP_CORS_ALLOWED_ORIGINS` | Comma-separated allowed origins, for example `http://localhost:*,http://127.0.0.1:*` |

The database URL must start with `jdbc:postgresql://`.
Do not store database credentials or authentication secrets in `application.properties`,
Git, Flutter, or Netlify environment variables.

## Run locally

### 1. Clone the repository

```powershell
git clone https://github.com/0jayer/vms-backend.git
cd vms-backend
```

### 2. Install and select Java 25

Verify Java:

```powershell
java -version
```

If needed, set Java for the current PowerShell session:

```powershell
$env:JAVA_HOME="C:\Users\<username>\.jdks\openjdk-25.0.2"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
```

Java 25 must satisfy the Maven `java.version` setting.

### 3. Create a PostgreSQL database

Create a database using PostgreSQL or pgAdmin. Example values:

```text
Database: vms_db
Username: vms_user
Password: choose-a-local-password
Port: 5432
```

The database name should contain lowercase letters, numbers, and underscores.

### 4. Set local environment variables

PowerShell session variables:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/vms_db"
$env:DB_USERNAME="vms_user"
$env:DB_PASSWORD="choose-a-local-password"
$env:JWT_SECRET="replace-with-a-random-secret-at-least-32-characters"
$env:ADMIN_USERNAME="admin"
$env:ADMIN_PASSWORD="choose-a-local-admin-password"
$env:APP_CORS_ALLOWED_ORIGINS="http://localhost:*,http://127.0.0.1:*"
```

Use actual data here.
PowerShell 7 allows the environment variables to be set permanently for the user with:

```powershell
[Environment]::SetEnvironmentVariable("DB_URL", "jdbc:postgresql://localhost:5432/vms_db", "User")
```

Repeat this only for values you intentionally want stored permanently.
Open a new terminal after changing user environment variables.

### 5. Start the backend

```powershell
.\mvnw.cmd spring-boot:run
```

The backend starts on `http://localhost:8080` by default.

Verify it:

```powershell
Invoke-RestMethod "http://localhost:8080/api/health"
```

Expected response:

```json
{
  "status": "UP",
  "database": "UP"
}
```

## Run tests

Run the complete test suite:

```powershell
.\mvnw.cmd test
```

Run a focused service test:

```powershell
.\mvnw.cmd -Dtest=VehicleServiceTest test
```

Run a focused controller test:

```powershell
.\mvnw.cmd -Dtest=VehicleControllerTest test
```

The tests use an in-memory H2 database where a Spring application context or real PostgreSQL is unnecessary.
Local and deployed production data are not modified by the unit and slice tests.

## Build a JAR

```powershell
.\mvnw.cmd clean package -DskipTests
```

Output:

```text
target/vms-backend-0.0.1-SNAPSHOT.jar
```

Run the JAR locally:

```powershell
java -jar target/vms-backend-0.0.1-SNAPSHOT.jar
```

## Run with Docker

Build the image:

```powershell
docker build -t vms-backend .
```

Run it while forwarding the application port:

```powershell
docker run --rm -p 8080:8080 `
  -e DB_URL="jdbc:postgresql://host.docker.internal:5432/vms_db" `
  -e DB_USERNAME="vms_user" `
  -e DB_PASSWORD="choose-a-local-password" `
  -e JWT_SECRET="replace-with-a-random-secret-at-least-32-characters" `
  -e ADMIN_USERNAME="admin" `
  -e ADMIN_PASSWORD="choose-a-local-admin-password" `
  -e APP_CORS_ALLOWED_ORIGINS="http://localhost:*,http://127.0.0.1:*" `
  vms-backend
```

`host.docker.internal` lets a Docker container reach PostgreSQL on the host Windows machine.

## Render deployment

This repository includes [render.yaml](render.yaml) and [Dockerfile](Dockerfile).

### Create PostgreSQL

In Render:

1. Create a PostgreSQL database.
2. Set a valid lowercase database name, for example `vms_db`.
3. For a Render Web Service, copy the database's **Internal Database URL** details.
4. Convert the internal URL to a JDBC URL:

```text
jdbc:postgresql://INTERNAL_HOST:5432/DATABASE_NAME
```

For example:

```text
DB_URL=jdbc:postgresql://dpg-example-host:5432/vms_db
```

### Create the Docker Web Service

In Render:

1. Select **New → Web Service**.
2. Connect `0jayer/vms-backend`.
3. Select the `main` branch.
4. Leave **Root Directory** empty.
5. Select **Docker**.
6. Use `./Dockerfile` as the Dockerfile path.
7. Leave **Docker Command** blank.
8. Set the health check path to `/api/health`.
9. Enable auto-deploy on commit if desired.

### Render environment variables

Configure these values in **Environment**:

| Variable | Value |
| --- | --- |
| `DB_URL` | JDBC PostgreSQL URL |
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |
| `JWT_SECRET` | Random secret with at least 32 characters |
| `ADMIN_USERNAME` | Initial administrator username |
| `ADMIN_PASSWORD` | Strong initial administrator password |
| `APP_CORS_ALLOWED_ORIGINS` | Exact deployed Netlify origin |

Example CORS value:

```text
https://your-site.netlify.app
```

Do not include a trailing slash. After the Netlify URL is known, replace local CORS origins rather than using a wildcard:

```text
APP_CORS_ALLOWED_ORIGINS=https://your-site.netlify.app
```

Local origins and the production origin can be combined:

```text
http://localhost:*,http://127.0.0.1:*,https://your-site.netlify.app
```

The Render Dockerfile uses the platform-provided `$PORT` and falls back to port `8080`.

### Verify Render

Open:

```text
https://YOUR-SERVICE.onrender.com/api/health
```

PowerShell:

```powershell
Invoke-RestMethod "https://YOUR-SERVICE.onrender.com/api/health"
```

Expected result:

```json
{
  "status": "UP",
  "database": "UP"
}
```

## Connect the Flutter frontend

Set this Netlify environment variable to the backend's public URL:

```text
API_BASE_URL=https://YOUR-SERVICE.onrender.com
```

Use no `/api` suffix. The frontend builds it with:

```text
flutter build web --release --dart-define=API_BASE_URL=$API_BASE_URL
```

Publish directory:

```text
build/web
```

After changing `API_BASE_URL`, trigger a Netlify redeploy because Flutter compiles that value into the web bundle.

## API

The vehicle endpoints are documented in [API.md](API.md).

Main routes:

| Method | Path | Authentication |
| --- | --- | --- |
| `POST` | `/api/auth/login` | Public |
| `GET` | `/api/health` | Public |
| `GET` | `/api/vehicles` | Bearer token |
| `POST` | `/api/vehicles` | Bearer token |
| `GET` | `/api/vehicles/{id}` | Bearer token |
| `PUT` | `/api/vehicles/{id}` | Bearer token |
| `DELETE` | `/api/vehicles/{id}` | Bearer token |

## Security notes

- Never commit database credentials, JWT secrets, or administrator passwords.
- Use environment variables for all deployments.
- The JWT secret must remain stable across restarts or previously issued tokens will become invalid.
- `spring.jpa.hibernate.ddl-auto=update` is intended for development; production deployments should use controlled database migrations.
- CORS should contain exact production origins and should not use `*` for the deployed frontend.