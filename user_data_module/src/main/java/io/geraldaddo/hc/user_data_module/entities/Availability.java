package io.geraldaddo.hc.user_data_module.entities;

import io.geraldaddo.hc.user_data_module.enums.DayOfWeek;

import java.time.LocalTime;

public record Availability(LocalTime startTime, LocalTime endTime, DayOfWeek dayOfWeek) {
}
