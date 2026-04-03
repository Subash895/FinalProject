# Smart City Project

Spring Boot 3.3.2 application with static frontend pages and a MySQL database.

## Run locally

### Option 1: Quick local run with H2

This does not require MySQL.

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
.\mvnw.cmd spring-boot:run
```

Then open:

- `http://localhost:8080`
- `http://localhost:8080/h2-console`

### Option 2: Local run with MySQL

Set these environment variables before starting the app:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

Then run:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/smartcity_minimal"
$env:SPRING_DATASOURCE_USERNAME="root"
$env:SPRING_DATASOURCE_PASSWORD="your-password"
.\mvnw.cmd spring-boot:run
```

## Docker

Build the image:

```bash
docker build -t smart-city-app .
```

Run the container:

```bash
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://<host>:3306/smartcity_minimal \
  -e SPRING_DATASOURCE_USERNAME=<user> \
  -e SPRING_DATASOURCE_PASSWORD=<password> \
  smart-city-app
```

## Deployment Notes

- The frontend now calls `/api` on the same host, so it works locally and after deployment.
- Do not commit real database passwords to `application.properties`.
- Cloud deployment needs both the app and a MySQL database.
- GitHub Actions now runs `mvn test` on pushes and pull requests.
