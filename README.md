# Course Waitlist Demo

A small Spring Boot REST service for enrollment, dropping, and FIFO waitlist promotion for one course offering. State is held in memory and starts empty each time the application starts.

## Requirements

- Java 17 or newer
- No separately installed Maven is needed; the project includes the Maven Wrapper.

## Build, test, and run

From the project directory:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

The service listens on `http://localhost:8080`. Course capacity defaults to `3`; override it with `--course.capacity=10` when running the application. Tests use an in-memory service and require no database or external service.

## API

Enroll or join the waitlist:

```http
POST /api/enrollments
Content-Type: application/json

{"learnerId":"learner-1","prerequisiteSatisfied":true}
```

The prerequisite flag represents the learner's prerequisite status for this offering. Outcomes are returned in JSON; enrollment returns `201`, waitlisting `202`, duplicate requests `409`, missing prerequisites `422`, and invalid input `400`.

Drop an enrolled learner:

```http
DELETE /api/enrollments/learner-1
```

The response identifies the learner promoted, if any. An unknown or already-dropped learner returns `404`; a blank identifier returns `400`.

Inspect the current offering:

```http
GET /api/enrollments
```

## Behavior assumptions

Eligibility is checked before either enrollment or waitlisting. Ineligible learners are rejected and never enter the queue. Prerequisite status is treated as an immutable snapshot for this single offering; if a stale ineligible queue entry is introduced by an external data source, the intended policy is to skip and remove it before considering the next learner. Queue order is FIFO, and learner IDs are trimmed and treated as exact, case-sensitive identifiers.

See [the design specification](docs/design-specification.md), [AI-use record](docs/ai-use-record.md), and [acceptance evidence](docs/acceptance-evidence.md) for design decisions, verification, and known limitations.
