package com.schwab.task.enrollment;

public record EnrollmentRequest(String learnerId, boolean prerequisiteSatisfied) {
}
