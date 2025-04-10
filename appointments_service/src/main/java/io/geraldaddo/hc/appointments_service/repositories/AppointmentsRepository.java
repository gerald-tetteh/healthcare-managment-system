package io.geraldaddo.hc.appointments_service.repositories;

import io.geraldaddo.hc.appointments_service.entities.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentsRepository extends JpaRepository<Appointment, Integer> {
    Page<Appointment> findAllByDoctorId(int doctorId, Pageable pageable);
    Page<Appointment> findAllByPatientId(int patientId, Pageable pageable);
}
