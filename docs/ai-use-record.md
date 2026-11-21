# AI-assisted engineering use record

## Activity and prompt

**Prompt used:** “Using the existing current demo application project, implement a new java project by reading all the requiremnets from attached assement.pdf; create all the file needed to full fill the requiremnts in the attached doc; add all required depedencies in pom.xml required to full fill the requirement; make sure no error in the compile time; who ever will use this code, they just build and run it.”

**AI contribution summary:** The assistant extracted the assessment requirements and proposed a bounded single-offering Spring service with explicit prerequisite and FIFO rules, REST endpoints, in-memory synchronized state, focused tests, and design/setup/AI-use documentation.

## Review and disposition

| Suggestion | Disposition | Review or change |
|---|---|---|
| Keep scope to one course offering and use in-memory state when the starter has no data model or persistence | Accepted | Preserves the assessment boundary and keeps setup to build-and-run. |
| Use a REST boundary in the existing Spring Boot project | Accepted | Uses existing Spring MVC and JSON dependencies; no unnecessary dependencies or database were added. |
| Treat prerequisite eligibility as immutable during this one offering and reject ineligible requests before waitlisting | Accepted with explicit limitation | Documented in the design and README. The promotion rule also states how stale ineligible entries should be handled. |
| Protect transitions with synchronization and add a focused concurrent enrollment test | Accepted | Implemented with synchronized service methods and a concurrent test asserting capacity and uniqueness invariants. |

## Verification evidence

The assistant ran `.\mvnw.cmd test` against the starter before changes (build succeeded), then ran `.\mvnw.cmd clean test` after implementation. The tests cover available capacity, full capacity, duplicate enrollment and waitlist requests, FIFO promotion, prerequisite rejection, invalid/repeated drops, and concurrent requests. The source and test results were reviewed against the assessment's stated acceptance criteria.

No credentials or external service data were included in the prompt or implementation.
