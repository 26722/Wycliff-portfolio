# MedCare API

Spring Boot REST API for clinic scheduling: offices, doctors, specializations, patients and appointments.

## Requirements
- Java 21
- PostgreSQL

## Setup
1. Create the database:
   ```bash
   psql -U postgres -h localhost -p 5433 -c "CREATE DATABASE medcare;"
   ```
2. Copy the example config and set your credentials:
   ```bash
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   ```
3. Run:
   ```bash
   ./mvnw spring-boot:run
   ```

The API runs on `http://localhost:8081`.

## Endpoints
| Resource        | Base path                   |
|-----------------|-----------------------------|
| Offices         | `/api/v1/offices`           |
| Doctors         | `/api/v1/doctors`           |
| Specializations | `/api/v1/specializations`   |
| Patients        | `/api/v1/patients`          |
| Appointments    | `/api/v1/appointments`      |
| Reports/queries | `/api/v1/query/appointment` |

## Tests
```bash
./mvnw test
```
