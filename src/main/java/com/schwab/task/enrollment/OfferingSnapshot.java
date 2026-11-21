package com.schwab.task.enrollment;

import java.util.List;

public record OfferingSnapshot(int capacity, List<String> enrolled, List<String> waitlisted) {
}
