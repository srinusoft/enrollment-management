# Assessment acceptance evidence

The supplied assessment references `Course-Registration-Rubric-Score.md`, but that rubric was not present in the starter project or attached files. This evidence maps the available assessment criteria to the implementation and tests rather than inventing a score from an unavailable rubric.

| Assessment criterion | Implementation evidence | Automated evidence |
|---|---|---|
| Enroll with available capacity and required prerequisite | `EnrollmentService.enroll` | `enrollsWhenCapacityIsAvailable` |
| Full course waitlists eligible learners once; duplicates do not create records | Enrollment/waitlist uniqueness checks | `waitlistsEligibleLearnersOnceWhenCourseIsFull`, `rejectsDuplicateEnrollmentWithoutChangingState` |
| Dropping promotes earliest eligible learner | `EnrollmentService.drop` and FIFO queue | `promotesWaitlistedLearnersInFifoOrder` |
| Ineligible learners are neither enrolled nor promoted; policy documented | Prerequisite rejection before queueing; stale ineligible entries are specified to be skipped and removed | `rejectsLearnersWithoutPrerequisitesAndNeverPromotesThem` |
| Invalid and repeated drops preserve state and return clear outcomes | Explicit `INVALID_REQUEST` and `NOT_ENROLLED` outcomes | `invalidAndRepeatedDropsDoNotChangeState` |
| Focused behavior tests and concurrency safety | Synchronized service transitions | `concurrentEnrollmentsNeverExceedCapacityOrDuplicateWaitlistEntries` |
| AI use and independent verification recorded | `docs/ai-use-record.md` | Maven test command and test list recorded there |

Run all tests with `.\mvnw.cmd clean test`.
