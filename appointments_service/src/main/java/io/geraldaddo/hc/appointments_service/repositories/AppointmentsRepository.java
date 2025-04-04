package io.geraldaddo.hc.appointments_service.repositories;

import io.geraldaddo.hc.appointments_service.entities.Appointment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentsRepository extends CrudRepository<Appointment, Integer> {
    @Query(value = "select * from appointments AS a " +
            "where (a.doctor_id = :doctorId  or a.patient_id = :patientId) " +
            "and ((:startDateTime between a.start_date_time and a.end_date_time) " +
            "or (:endDateTime between a.start_date_time and a.end_date_time))", nativeQuery = true)
    List<Appointment> countClashingAppointments(
            @Param("doctorId") int doctorId,
            @Param("patientId") int patientId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);
}
