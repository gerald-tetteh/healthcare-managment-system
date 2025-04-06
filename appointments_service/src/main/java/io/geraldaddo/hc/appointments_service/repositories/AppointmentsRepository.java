package io.geraldaddo.hc.appointments_service.repositories;

import io.geraldaddo.hc.appointments_service.entities.Appointment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentsRepository extends CrudRepository<Appointment, Integer> {
}
