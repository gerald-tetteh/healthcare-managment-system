package io.geraldaddo.hc.appointments_service.dto;

import lombok.*;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AppointmentListDto {
    List<AppointmentDto> appointments;
    int page;
    int numberOfRecords;
}
