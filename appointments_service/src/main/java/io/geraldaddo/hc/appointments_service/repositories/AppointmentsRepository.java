package io.geraldaddo.hc.appointments_service.repositories;

import io.geraldaddo.hc.appointments_service.entities.Appointment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentsRepository extends JpaRepository<Appointment, Integer> {
    List<Appointment> findAllByDoctorId(int doctorId, Pageable pageable);
    List<Appointment> findAllByPatientId(int patientId, Pageable pageable);
}
