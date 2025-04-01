package io.geraldaddo.hc.user_data_module.entities;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record Availability(LocalTime startTime, LocalTime endTime, DayOfWeek dayOfWeek) {
}
