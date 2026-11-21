# Design specification: single-course enrollment and waitlist

## Service boundary and data/API design

`EnrollmentService` owns one course offering's capacity, enrolled learner IDs, and FIFO waitlist. `EnrollmentController` exposes the service through three endpoints:

- `POST /api/enrollments` accepts a learner ID and prerequisite result.
- `DELETE /api/enrollments/{learnerId}` drops an enrolled learner and reports any promotion.
- `GET /api/enrollments` returns a snapshot of capacity, enrolled IDs, and waitlisted IDs.

The service returns explicit outcome codes and messages instead of silently ignoring invalid or duplicate requests. The default capacity is three and can be set with `course.capacity`.

## Invariants and rules

- An identifier is trimmed, must be nonblank, and is unique across enrolled and waitlisted learners.
- Only learners whose prerequisite is satisfied are enrolled or added to the waitlist.
- The enrolled count never exceeds capacity.
- A drop affects state only if the learner is enrolled. An invalid or repeated drop does not alter queue or capacity state.
- On a successful drop, the earliest eligible waitlisted learner is promoted. Promotion and drop are one service operation.
- Eligibility is captured when the learner requests enrollment and is assumed not to change during this offering. If an ineligible stale entry is supplied by an external data source, the queue policy is to skip and remove it before considering the next learner.

## Concurrency and persistence

All state-changing operations and snapshots are synchronized on the service instance, making capacity checks, waitlisting, drops, and promotions atomic for this single process. State is held in memory and is lost on restart. A production multi-instance service would need a transactional persistent store and a shared locking/atomic-update strategy; neither is needed for this bounded starter-project exercise.

## Tradeoffs and limits

This slice intentionally omits identity, catalog, prerequisite integration, cancellation of waitlisted learners, multiple offerings, persistence, and frontend behavior. The prerequisite flag is a demo input, not an authorization or verification mechanism. There is no operation to change a learner's prerequisite status after requesting enrollment.
