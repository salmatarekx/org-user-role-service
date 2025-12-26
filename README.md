Org User Role Service
📌 Overview

This project is a Spring Boot REST API that manages Organizations, Users, and Roles with JWT-based authentication and role-based access control.

There are two main user types:

Organization Admin (ORG_ADMIN)
Regular User (ORG_USER)

Access to endpoints is strictly enforced using Spring Security .


What an Admin Can Do (ORG_ADMIN)
Admins have full control within their organization only.
✅ Admin Permissions

Create users in their organization
List all users in their organization
Delete users in their organization
Assign roles to users
Revoke roles from users
Create new roles
Delete roles (only if not assigned to users)
View organization details

Admin Endpoints:
POST   /users/create
GET    /users/list
DELETE /users/delete/{id}
POST   /users/assign-role/{id}/{roleName}
DELETE /users/revoke-role/{id}/{roleName}
POST   /roles/create
DELETE /roles/delete/{roleName}
GET    /organization/get-current

✅ User Permissions(ORG_USER)
Regular users have self-service access only.

View their own profile
Update their own profile

🔗 User Endpoints
GET   /users/get-profile  & 
GET /auth/get-current-user (works same as get-profile but can be for both ADMIN/USER)
PATCH /users/update-profile


Unauthorized Access Behavior
 regular user attempts to access an admin-only endpoint, the API responds with:
Response (403 Forbidden)
{
"status": 403,
"message": "Only admins have access."
}

This behavior is enforced using:
@PreAuthorize("hasRole('ORG_ADMIN')")
Custom Spring Security handlers


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

