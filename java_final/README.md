# Media Portal CMS

Spring Boot backend for a multi-format media portal.

## Stack

- Java 21 and Spring Boot 4.0.8
- PostgreSQL for persistent content and users
- Redis for caching, view counters, and popularity rankings
- Spring Security with JWT authentication
- Swagger/OpenAPI documentation

## Start dependencies

Docker is required for PostgreSQL and Redis:

```bash
docker compose up -d
```

## Run the application

```bash
./mvnw spring-boot:run
```

Run tests without external services:

```bash
./mvnw test
```

## Documentation

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

## Authentication endpoints

### Register

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "username": "editor",
  "password": "password123"
}
```

### Login

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "username": "editor",
  "password": "password123"
}
```

Use the returned token for content management requests:

```text
Authorization: Bearer <token>
```

## Content endpoints

| Method | Endpoint | Access |
|---|---|---|
| `POST` | `/api/content` | Authenticated |
| `GET` | `/api/content` | Public |
| `GET` | `/api/content/{id}` | Public |
| `PUT` | `/api/content/{id}` | Authenticated |
| `DELETE` | `/api/content/{id}` | Authenticated |
| `POST` | `/api/content/{id}/views` | Public |
| `GET` | `/api/content/popular` | Public |

Filter the list by type:

```text
GET /api/content?type=ARTICLE
GET /api/content?type=VIDEO
GET /api/content?type=PODCAST
```

### Article

```json
{
  "type": "ARTICLE",
  "title": "Introduction to Spring Boot",
  "text": "Article content",
  "author": "John Doe",
  "publicationDate": "2026-10-20"
}
```

### Video

```json
{
  "type": "VIDEO",
  "title": "Spring Boot Tutorial",
  "url": "https://example.com/video",
  "durationSeconds": 1200
}
```

### Podcast

```json
{
  "type": "PODCAST",
  "title": "Java Podcast",
  "audioUrl": "https://example.com/podcast.mp3",
  "episodes": [
    {
      "number": 1,
      "title": "Introduction",
      "durationSeconds": 1800
    }
  ]
}
```

`POST /api/content/{id}/views` increments both the total view counter and
the current-hour Redis sorted-set ranking. `GET /api/content/popular` returns
the most-viewed content for the current UTC hour.
