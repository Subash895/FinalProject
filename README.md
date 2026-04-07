# Smart City Project

Spring Boot 3.3.2 application with static frontend pages and a MySQL database.

## Run locally

Local, Docker, and deployment now use the same MySQL-based configuration so behavior stays consistent across environments.

Set these environment variables before starting the app:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

Then run:

On Windows PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/smartcity_minimal"
$env:SPRING_DATASOURCE_USERNAME="root"
$env:SPRING_DATASOURCE_PASSWORD="your-password"
.\mvnw.cmd spring-boot:run
```

If you already have these values in `.env`, you can simply run:

```powershell
.\mvnw.cmd spring-boot:run
```

Then open:

- `http://localhost:8080`

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

- The frontend now calls `/api` on the same host, so it works locally, in Docker, and after deployment.
- Do not commit real database passwords to `application.properties`.
- Cloud deployment needs both the app and a MySQL database.
- GitHub Actions now runs `mvn test` on pushes and pull requests.

## Publish To The World

The easiest path for this project is Railway because the repo already contains a `Dockerfile` and now includes a `railway.toml`.

1. Push the latest code to GitHub.
2. Go to Railway and create a new project.
3. Choose `Deploy from GitHub repo`.
4. Select this repository.
5. Add a MySQL service in the same Railway project.
6. Set these environment variables on the app service:

```text
SPRING_DATASOURCE_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}
SPRING_DATASOURCE_USERNAME=${{MySQL.MYSQLUSER}}
SPRING_DATASOURCE_PASSWORD=${{MySQL.MYSQLPASSWORD}}
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

7. Open the app service settings and generate a public domain.

After that, Railway gives you a public URL that anyone can open.

## Deploy From Local

For Windows PowerShell or Command Prompt, you can deploy directly from this machine with:

```bat
scripts\deploy-railway.cmd
```

Optional project name:

```bat
scripts\deploy-railway.cmd my-smart-city
```

What the script does:

- logs you into Railway if needed
- creates a Railway project on first run
- creates a MySQL service
- creates and links the app service
- sets Spring Boot database variables
- deploys the current directory
- generates a public Railway domain

After the first setup, running the same script again will redeploy the app from local.
