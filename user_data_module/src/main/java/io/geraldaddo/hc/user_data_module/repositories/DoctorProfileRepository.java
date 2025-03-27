package io.geraldaddo.hc.user_data_module.repositories;

import io.geraldaddo.hc.user_data_module.entities.DoctorProfile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorProfileRepository extends CrudRepository<DoctorProfile, Integer> {
}
