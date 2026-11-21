package com.schwab.task.enrollment;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EnrollmentService {

	private final int capacity;
	private final Set<String> enrolled = new LinkedHashSet<>();
	private final Deque<WaitlistEntry> waitlist = new ArrayDeque<>();

	public EnrollmentService(@Value("${course.capacity:3}") int capacity) {
		if (capacity < 1) {
			throw new IllegalArgumentException("Course capacity must be at least 1");
		}
		this.capacity = capacity;
	}

	public synchronized EnrollmentResult enroll(EnrollmentRequest request) {
		if (request == null || request.learnerId() == null || request.learnerId().isBlank()) {
			return result(EnrollmentResult.Outcome.INVALID_REQUEST, null,
					"Learner ID must not be blank", null, null);
		}

		String learnerId = request.learnerId().trim();
		if (enrolled.contains(learnerId)) {
			return result(EnrollmentResult.Outcome.ALREADY_ENROLLED, learnerId,
					"Learner is already enrolled", null, null);
		}
		if (waitlisted(learnerId)) {
			return result(EnrollmentResult.Outcome.ALREADY_WAITLISTED, learnerId,
					"Learner is already on the waitlist", waitlistPosition(learnerId), null);
		}
		if (!request.prerequisiteSatisfied()) {
			return result(EnrollmentResult.Outcome.PREREQUISITE_NOT_MET, learnerId,
					"Required prerequisite has not been satisfied", null, null);
		}
		if (enrolled.size() < capacity) {
			enrolled.add(learnerId);
			return result(EnrollmentResult.Outcome.ENROLLED, learnerId,
					"Learner enrolled successfully", null, null);
		}

		waitlist.addLast(new WaitlistEntry(learnerId, true));
		return result(EnrollmentResult.Outcome.WAITLISTED, learnerId,
				"Course is full; learner added to the waitlist", waitlist.size(), null);
	}

	public synchronized EnrollmentResult drop(String learnerId) {
		if (learnerId == null || learnerId.isBlank()) {
			return result(EnrollmentResult.Outcome.INVALID_REQUEST, learnerId,
					"Learner ID must not be blank", null, null);
		}

		String normalizedLearnerId = learnerId.trim();
		if (!enrolled.remove(normalizedLearnerId)) {
			return result(EnrollmentResult.Outcome.NOT_ENROLLED, normalizedLearnerId,
					"Learner is not currently enrolled", null, null);
		}

		String promotedLearnerId = promoteNextEligibleLearner();
		return result(EnrollmentResult.Outcome.DROPPED, normalizedLearnerId,
				promotedLearnerId == null
						? "Enrollment dropped; no learner was waiting"
						: "Enrollment dropped; the next eligible learner was promoted",
				null, promotedLearnerId);
	}

	public synchronized OfferingSnapshot snapshot() {
		return new OfferingSnapshot(capacity, List.copyOf(enrolled),
				waitlist.stream().map(WaitlistEntry::learnerId).toList());
	}

	private String promoteNextEligibleLearner() {
		while (!waitlist.isEmpty()) {
			WaitlistEntry next = waitlist.removeFirst();
			if (next.prerequisiteSatisfied()) {
				enrolled.add(next.learnerId());
				return next.learnerId();
			}
		}
		return null;
	}

	private int waitlistPosition(String learnerId) {
		int position = 1;
		for (WaitlistEntry queuedLearner : waitlist) {
			if (queuedLearner.learnerId().equals(learnerId)) {
				return position;
			}
			position++;
		}
		throw new IllegalStateException("Waitlisted learner is missing from the queue");
	}

	private boolean waitlisted(String learnerId) {
		return waitlist.stream().anyMatch(entry -> entry.learnerId().equals(learnerId));
	}

	private EnrollmentResult result(
			EnrollmentResult.Outcome outcome,
			String learnerId,
			String message,
			Integer waitlistPosition,
			String promotedLearnerId) {
		return new EnrollmentResult(outcome, learnerId, message, waitlistPosition, promotedLearnerId);
	}

	private record WaitlistEntry(String learnerId, boolean prerequisiteSatisfied) {
	}
}
