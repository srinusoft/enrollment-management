package com.schwab.task.enrollment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

	private final EnrollmentService enrollmentService;

	public EnrollmentController(EnrollmentService enrollmentService) {
		this.enrollmentService = enrollmentService;
	}

	@PostMapping
	public ResponseEntity<EnrollmentResult> enroll(@RequestBody EnrollmentRequest request) {
		EnrollmentResult result = enrollmentService.enroll(request);
		return ResponseEntity.status(enrollmentStatus(result.outcome())).body(result);
	}

	@DeleteMapping("/{learnerId}")
	public ResponseEntity<EnrollmentResult> drop(@PathVariable String learnerId) {
		EnrollmentResult result = enrollmentService.drop(learnerId);
		return ResponseEntity.status(dropStatus(result.outcome())).body(result);
	}

	@GetMapping
	public OfferingSnapshot snapshot() {
		return enrollmentService.snapshot();
	}

	private HttpStatus enrollmentStatus(EnrollmentResult.Outcome outcome) {
		return switch (outcome) {
			case ENROLLED -> HttpStatus.CREATED;
			case WAITLISTED -> HttpStatus.ACCEPTED;
			case ALREADY_ENROLLED, ALREADY_WAITLISTED -> HttpStatus.CONFLICT;
			case PREREQUISITE_NOT_MET -> HttpStatus.UNPROCESSABLE_CONTENT;
			case INVALID_REQUEST -> HttpStatus.BAD_REQUEST;
			case NOT_ENROLLED -> HttpStatus.NOT_FOUND;
			case DROPPED -> HttpStatus.INTERNAL_SERVER_ERROR;
		};
	}

	private HttpStatus dropStatus(EnrollmentResult.Outcome outcome) {
		return switch (outcome) {
			case DROPPED -> HttpStatus.OK;
			case INVALID_REQUEST -> HttpStatus.BAD_REQUEST;
			case NOT_ENROLLED -> HttpStatus.NOT_FOUND;
			case ENROLLED, WAITLISTED, ALREADY_ENROLLED, ALREADY_WAITLISTED, PREREQUISITE_NOT_MET ->
					HttpStatus.INTERNAL_SERVER_ERROR;
		};
	}
}
