package io.geraldaddo.hc.user_data_module.repositories;

import io.geraldaddo.hc.user_data_module.entities.DoctorProfile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorProfileRepository extends CrudRepository<DoctorProfile, Integer> {
    @Query(value = "SELECT * FROM doctor_profile AS p WHERE p.user_id = :userId", nativeQuery = true)
    Optional<DoctorProfile> findByUserId(@Param("userId") int userId);
}
