package com.schwab.task.enrollment;

public record EnrollmentResult(
		Outcome outcome,
		String learnerId,
		String message,
		Integer waitlistPosition,
		String promotedLearnerId) {

	public enum Outcome {
		ENROLLED,
		WAITLISTED,
		ALREADY_ENROLLED,
		ALREADY_WAITLISTED,
		PREREQUISITE_NOT_MET,
		INVALID_REQUEST,
		NOT_ENROLLED,
		DROPPED
	}
}
