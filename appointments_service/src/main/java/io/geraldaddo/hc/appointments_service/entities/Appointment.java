package io.geraldaddo.hc.appointments_service.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue
    @Column(nullable = false)
    private Integer appointmentId;
    @Column(nullable = false)
    private Integer doctorId;
    @Column(nullable = false)
    private Integer patientId;
    @Column(nullable = false)
    private LocalDateTime dateTime;
    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private AppointmentStatus status;
    @Column(nullable = false)
    private String notes;
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
