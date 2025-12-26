Org User Role Service
📌 Purpose

A Spring Boot REST API for organization-based user and role management, secured with JWT authentication and role-based authorization.

Features include:

Authentication with JWT
Organization-scoped users
Role management (assign / revoke)
Admin-only protected endpoints
Database migrations with Flyway
Fully Dockerized setup

🛠 Tech Stack
Java 21
Spring Boot
Spring Security (JWT)
PostgreSQL
Flyway

Docker & Docker Compose

Swagger / OpenAPI

▶️ How to Run (Local)

Prerequisites

Docker Desktop installed and running

Steps

git clone <repository-url>
cd org-user-role-service
docker compose up --build


The application will start at:

http://localhost:8080
SWAGGER API DOCUMENTATION:
http://localhost:8080/swagger-ui/index.html
🗄 Database & Migrations

PostgreSQL runs in Docker

Schema managed via Flyway

Migrations located in:

src/main/resources/db/migration


They are applied automatically at startup.

IF you face an error  try this commands:
If you have Maven installed
mvn -DskipTests clean package

If your project includes Maven Wrapper (recommended)
.\mvnw.cmd -DskipTests clean package

2. Confirm you now have a JAR
   dir .\target\*.jar

3. Rebuild and run
   docker compose up --build

To START the project has one seeded organization with one admin 
email:superadmin@org.com
pass:Admin123!
You should login with it first in postman to access the token.

