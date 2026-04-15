# MySmartCity

`MySmartCity` is a full-stack Smart City web application built with `Spring Boot 3.3.2`, `Java 21`, static HTML/CSS/JavaScript pages, and `MySQL`.

The project combines city information, places, businesses, community features, subscriptions, authentication, reviews, and an AI-powered assistant in one application.

## Project Overview

This application provides:

- City management and city history
- Place discovery with map integration and location search
- Business directory and advertisements
- City news and local events
- Community forum posts and comments
- User registration, login, Google sign-in, JWT auth, and profile management
- Password reset with OTP email flow
- Subscription management with Razorpay checkout support
- User reviews for cities, places, businesses, and news
- AI chatbot that answers from project data
- Seeded demo data for first run

## Tech Stack

- Backend: `Spring Boot`, `Spring Web`, `Spring Data JPA`, `Spring Security`
- Frontend: `HTML`, `CSS`, `Vanilla JavaScript`
- Database: `MySQL`
- Auth: `JWT`, optional `Google Sign-In`
- Payments: optional `Razorpay`
- Email: optional SMTP mail configuration
- AI integration: optional OpenAI-compatible endpoint, currently configured for Gemini-style endpoint support
- Deployment: `Docker`, `Docker Compose`, `Railway`

## Features Implemented

### 1. Authentication and User Features

- User registration with `USER` and `BUSINESS` roles
- Secure login with JWT token
- Google sign-in support
- Profile page for logged-in users
- Profile update support
- Password reset using OTP sent by email
- Role-aware UI for guest, user, business, and admin flows

### 2. Admin Features

- Admin-only management for cities and places
- Admin-only management for users
- Admin access to all subscriptions
- Admin edit and delete controls in frontend pages

### 3. City Module

- Add, list, update, and delete cities
- View city details
- Store city history records
- Link events, news, and places with cities

### 4. Place Module

- Add, list, update, and delete places
- Filter places by category and location
- OpenStreetMap integration for map display
- Reverse geocoding and search-based geocoding
- Current-location support
- Distance display from the user location
- Review support for places

### 5. Business Module

- Add, list, update, and delete businesses
- Business owner relation
- Featured business support
- Review support for businesses

### 6. Advertisement Module

- Create and list advertisements
- Attach advertisements to businesses

### 7. News and Events

- Add, list, update, and delete city news
- Add, list, update, and delete events
- Review support for news items

### 8. Forum and Comments

- Create, list, update, and delete forum posts
- Add, list, update, and delete comments
- Community discussion support

### 9. Subscription and Payments

- Manual subscription management
- Logged-in user subscription history
- Paid checkout flow for `PRO` and `ENTERPRISE`
- Razorpay order creation
- Razorpay payment confirmation handling
- Payment failure handling
- Webhook endpoint for payment status updates

### 10. Reviews

- Reviews for `CITY`, `PLACE`, `BUSINESS`, and `NEWS`
- Review create, update, and delete
- Average rating display in UI

### 11. AI Chatbot

- Logged-in users can open the assistant from the UI
- Chat history stored per user
- Chat can answer only from project data
- Chat history can be cleared
- Clear fallback message when AI API key is missing

### 12. Seed Data

On first run, the app seeds:

- demo users
- one city
- one place
- one business
- one advertisement
- one forum post and comment
- one event
- one news item
- one market rate
- one subscription

## Default Demo Accounts

These accounts are created automatically by `DataSeeder` on startup.

- Admin: `admin@smartcity.local`
- User: `user@smartcity.local`
- Business: `business@smartcity.local`
- Default password for seeded accounts: `Subbu@895`

Change the password and secrets before real deployment.

## Project Pages

The frontend includes these main pages:

- `index.html`
- `login.html`
- `register.html`
- `profile.html`
- `city.html`
- `place.html`
- `business.html`
- `news.html`
- `forum.html`
- `subscription.html`

## Prerequisites

Install these before running:

- `Java 21`
- `MySQL 8+` or Dockerized MySQL
- `Maven` or use the included Maven wrapper
- `Docker Desktop` if you want the Docker run path

## Required Environment Variables

At minimum, the application needs a MySQL connection.

### Minimum required

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

### Common optional variables

- `SPRING_JPA_HIBERNATE_DDL_AUTO`
- `APP_JWT_SECRET`
- `APP_AUTH_GOOGLE_CLIENT_ID`
- `APP_MAPS_GOOGLE_API_KEY`
- `SPRING_MAIL_HOST`
- `SPRING_MAIL_PORT`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`
- `APP_MAIL_FROM`
- `APP_PAYMENTS_RAZORPAY_ENABLED`
- `APP_PAYMENTS_RAZORPAY_KEY_ID`
- `APP_PAYMENTS_RAZORPAY_KEY_SECRET`
- `APP_PAYMENTS_RAZORPAY_WEBHOOK_SECRET`
- `APP_AI_BASE_URL`
- `APP_AI_API_KEY`
- `APP_AI_MODEL`

The app automatically loads `.env` because `application.properties` includes:

```properties
spring.config.import=optional:file:.env[.properties]
```

## Create `.env`

Copy `.env.example` to `.env` and replace the values for your machine.

Example:

```env
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/smartcity_minimal
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your-mysql-password
SPRING_JPA_HIBERNATE_DDL_AUTO=update
APP_JWT_SECRET=replace-with-a-strong-secret

# Optional
APP_AUTH_GOOGLE_CLIENT_ID=your-google-web-client-id.apps.googleusercontent.com
SPRING_MAIL_HOST=smtp.example.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@example.com
SPRING_MAIL_PASSWORD=your-app-password
APP_MAIL_FROM=your-email@example.com
APP_PAYMENTS_RAZORPAY_ENABLED=false
APP_AI_API_KEY=
```

Do not keep real passwords or production secrets committed in source control.

## Database Setup

Create the database before running locally:

```sql
CREATE DATABASE smartcity_minimal;
```

Hibernate is configured with:

```properties
spring.jpa.hibernate.ddl-auto=update
```

That means tables are created or updated automatically when the application starts.

## Run Locally

### Option 1. Run with Maven Wrapper

### Windows PowerShell

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/smartcity_minimal"
$env:SPRING_DATASOURCE_USERNAME="root"
$env:SPRING_DATASOURCE_PASSWORD="your-password"
.\mvnw.cmd spring-boot:run
```

If `.env` is already configured:

```powershell
.\mvnw.cmd spring-boot:run
```

### Windows Command Prompt

```bat
set SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/smartcity_minimal
set SPRING_DATASOURCE_USERNAME=root
set SPRING_DATASOURCE_PASSWORD=your-password
mvnw.cmd spring-boot:run
```

### Option 2. Use the included runner

```bat
run-project.cmd
```

This menu can:

- run locally
- run with Docker
- stop Docker services
- publish Docker image
- push Git changes
- deploy to Railway

### Open the Application

After startup, open:

- `http://localhost:8080`

## Run with Docker Compose

This project includes `docker-compose.yml` with:

- `smart-city-mysql`
- `smart-city-app`

Start both services:

```bash
docker compose up --build
```

Run in background:

```bash
docker compose up --build -d
```

Stop services:

```bash
docker compose down
```

Then open:

- `http://localhost:8080`

## Docker Notes

- The compose file uses MySQL and app containers together.
- The app container reads values from `.env`.
- The Docker MySQL service currently uses the database `smartcity_minimal`.
- Replace default passwords before using this outside local demo use.

## Build Docker Image Manually

```bash
docker build -t smart-city-app .
```

Run manually:

```bash
docker run -p 8080:8080 ^
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/smartcity_minimal ^
  -e SPRING_DATASOURCE_USERNAME=root ^
  -e SPRING_DATASOURCE_PASSWORD=your-password ^
  smart-city-app
```

## Optional Service Configuration

### Google Sign-In

Set:

- `APP_AUTH_GOOGLE_CLIENT_ID`

If this is missing, Google login/register buttons are hidden.

### Password Reset Email

Set SMTP values:

- `SPRING_MAIL_HOST`
- `SPRING_MAIL_PORT`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`
- `APP_MAIL_FROM`

If mail is not configured, password reset OTP emails cannot be sent.

### Razorpay Payments

Set:

- `APP_PAYMENTS_RAZORPAY_ENABLED=true`
- `APP_PAYMENTS_RAZORPAY_KEY_ID`
- `APP_PAYMENTS_RAZORPAY_KEY_SECRET`
- `APP_PAYMENTS_RAZORPAY_WEBHOOK_SECRET`

If not configured, paid subscription checkout will not work.

### AI Chatbot

Set:

- `APP_AI_API_KEY`

Optional:

- `APP_AI_BASE_URL`
- `APP_AI_MODEL`

If no AI key is configured, the chatbot returns a configuration message instead of AI answers.

## How Access Control Works

- Public users can open the frontend and read public API data
- Logged-in users can use profile, chat, their own subscriptions, and review features
- Admins can manage users, subscriptions, cities, and places

## API Modules Available

Main API areas in the project:

- `/api/auth`
- `/api/users`
- `/api/cities`
- `/api/cityhistory`
- `/api/places`
- `/api/businesses`
- `/api/advertisements`
- `/api/news`
- `/api/events`
- `/api/forumposts`
- `/api/comments`
- `/api/reviews`
- `/api/subscriptions`
- `/api/payments/webhook`
- `/api/chat`
- `/api/config/public`

## Deployment Notes

- The frontend calls `/api` on the same host, so local, Docker, and deployment environments stay aligned.
- A MySQL database is required in all real deployments.
- `railway.toml`, `Dockerfile`, and Railway scripts are already included.
- GitHub Actions CI is configured to run Maven tests on push and pull request.

## Railway Deployment

Basic Railway flow:

1. Push the latest code to GitHub.
2. Create a Railway project.
3. Deploy from the GitHub repository.
4. Add a MySQL service.
5. Set the Spring Boot environment variables.
6. Generate a public domain.

Important Railway variables:

```text
SPRING_DATASOURCE_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}
SPRING_DATASOURCE_USERNAME=${{MySQL.MYSQLUSER}}
SPRING_DATASOURCE_PASSWORD=${{MySQL.MYSQLPASSWORD}}
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

Local deploy helper:

```bat
scripts\deploy-railway.cmd
```

## What To Check Before Running

Before you start the project, make sure:

- Java 21 is installed
- MySQL is running
- database `smartcity_minimal` exists
- `.env` exists or environment variables are set
- `SPRING_DATASOURCE_URL`, username, and password are correct
- port `8080` is free
- port `3306` is free if using local MySQL or Docker MySQL
- optional integrations are configured only if you plan to use them

## First-Run Checklist

After the app starts:

1. Open `http://localhost:8080`
2. Log in with a seeded account or register a new user
3. Verify cities, places, businesses, forum, and news load correctly
4. Open `profile.html` and confirm profile fetch/update works
5. Test reviews on a place, business, city, or news item
6. If AI is configured, test the assistant
7. If SMTP is configured, test forgot-password flow
8. If Razorpay is configured, test checkout flow

## Troubleshooting

### App does not start

Check:

- Java version is `21`
- MySQL server is running
- datasource values are correct
- database exists

### Port 8080 already in use

Stop the existing process or run with another `PORT`.

### MySQL connection failed

Check:

- username and password
- host and port
- database name
- MySQL service status

### Google login not visible

Set `APP_AUTH_GOOGLE_CLIENT_ID`.

### Forgot-password OTP not working

Set valid SMTP configuration.

### Razorpay checkout not working

Set Razorpay key values and enable the payment flag.

### Chatbot says it is not configured

Set `APP_AI_API_KEY`.

## Security Notes

- Replace the default JWT secret before deployment
- Replace demo passwords before deployment
- Do not commit real `.env` secrets
- Do not use local demo credentials in production

## Author Notes

This README is based on the current project code, configuration, frontend pages, and startup scripts in this repository.
