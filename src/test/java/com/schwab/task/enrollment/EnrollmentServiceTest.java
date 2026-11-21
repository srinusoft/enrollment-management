package com.schwab.task.enrollment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

class EnrollmentServiceTest {

	@Test
	void enrollsWhenCapacityIsAvailable() {
		EnrollmentService service = new EnrollmentService(2);

		EnrollmentResult result = service.enroll(request("learner-1", true));

		assertEquals(EnrollmentResult.Outcome.ENROLLED, result.outcome());
		assertEquals(List.of("learner-1"), service.snapshot().enrolled());
		assertTrue(service.snapshot().waitlisted().isEmpty());
	}

	@Test
	void waitlistsEligibleLearnersOnceWhenCourseIsFull() {
		EnrollmentService service = new EnrollmentService(1);
		service.enroll(request("learner-1", true));

		assertEquals(EnrollmentResult.Outcome.WAITLISTED,
				service.enroll(request("learner-2", true)).outcome());
		EnrollmentResult duplicate = service.enroll(request("learner-2", true));

		assertEquals(EnrollmentResult.Outcome.ALREADY_WAITLISTED, duplicate.outcome());
		assertEquals(1, duplicate.waitlistPosition());
		assertEquals(List.of("learner-2"), service.snapshot().waitlisted());
	}

	@Test
	void rejectsDuplicateEnrollmentWithoutChangingState() {
		EnrollmentService service = new EnrollmentService(2);
		service.enroll(request("learner-1", true));

		EnrollmentResult result = service.enroll(request("learner-1", true));

		assertEquals(EnrollmentResult.Outcome.ALREADY_ENROLLED, result.outcome());
		assertEquals(List.of("learner-1"), service.snapshot().enrolled());
	}

	@Test
	void promotesWaitlistedLearnersInFifoOrder() {
		EnrollmentService service = new EnrollmentService(2);
		service.enroll(request("learner-1", true));
		service.enroll(request("learner-2", true));
		service.enroll(request("learner-3", true));
		service.enroll(request("learner-4", true));

		EnrollmentResult firstDrop = service.drop("learner-1");
		EnrollmentResult secondDrop = service.drop("learner-2");

		assertEquals("learner-3", firstDrop.promotedLearnerId());
		assertEquals("learner-4", secondDrop.promotedLearnerId());
		assertEquals(List.of("learner-3", "learner-4"), service.snapshot().enrolled());
		assertTrue(service.snapshot().waitlisted().isEmpty());
	}

	@Test
	void rejectsLearnersWithoutPrerequisitesAndNeverPromotesThem() {
		EnrollmentService service = new EnrollmentService(1);
		service.enroll(request("learner-1", true));

		assertEquals(EnrollmentResult.Outcome.PREREQUISITE_NOT_MET,
				service.enroll(request("ineligible", false)).outcome());
		service.enroll(request("eligible-waiter", true));

		EnrollmentResult drop = service.drop("learner-1");

		assertEquals("eligible-waiter", drop.promotedLearnerId());
		assertEquals(List.of("eligible-waiter"), service.snapshot().enrolled());
		assertFalse(service.snapshot().enrolled().contains("ineligible"));
		assertFalse(service.snapshot().waitlisted().contains("ineligible"));
	}

	@Test
	void invalidAndRepeatedDropsDoNotChangeState() {
		EnrollmentService service = new EnrollmentService(1);
		service.enroll(request("learner-1", true));
		OfferingSnapshot before = service.snapshot();

		assertEquals(EnrollmentResult.Outcome.INVALID_REQUEST, service.drop("  ").outcome());
		assertEquals(EnrollmentResult.Outcome.NOT_ENROLLED, service.drop("missing").outcome());
		assertEquals(EnrollmentResult.Outcome.DROPPED, service.drop("learner-1").outcome());
		assertEquals(EnrollmentResult.Outcome.NOT_ENROLLED, service.drop("learner-1").outcome());
		assertEquals(List.of("learner-1"), before.enrolled());
		assertTrue(service.snapshot().enrolled().isEmpty());
		assertTrue(service.snapshot().waitlisted().isEmpty());
	}

	@Test
	void concurrentEnrollmentsNeverExceedCapacityOrDuplicateWaitlistEntries() throws Exception {
		int capacity = 3;
		int learnerCount = 20;
		EnrollmentService service = new EnrollmentService(capacity);
		ExecutorService executor = Executors.newFixedThreadPool(learnerCount);
		CountDownLatch ready = new CountDownLatch(learnerCount);
		CountDownLatch start = new CountDownLatch(1);
		List<Future<EnrollmentResult>> results = new ArrayList<>();

		try {
			for (int i = 0; i < learnerCount; i++) {
				String learnerId = "learner-" + i;
				results.add(executor.submit(() -> {
					ready.countDown();
					start.await();
					return service.enroll(request(learnerId, true));
				}));
			}
			ready.await();
			start.countDown();
			for (Future<EnrollmentResult> result : results) {
				assertNotNull(result.get());
			}
		} finally {
			executor.shutdownNow();
		}

		OfferingSnapshot snapshot = service.snapshot();
		assertEquals(capacity, snapshot.enrolled().size());
		assertEquals(learnerCount - capacity, snapshot.waitlisted().size());
		assertEquals(learnerCount,
				snapshot.enrolled().size() + snapshot.waitlisted().stream().distinct().count());
	}

	private EnrollmentRequest request(String learnerId, boolean prerequisiteSatisfied) {
		return new EnrollmentRequest(learnerId, prerequisiteSatisfied);
	}
}
