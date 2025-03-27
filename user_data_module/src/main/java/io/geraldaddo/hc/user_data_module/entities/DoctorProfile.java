package io.geraldaddo.hc.user_data_module.entities;

import io.geraldaddo.hc.user_data_module.attribute_converters.AvailabilityConverter;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class DoctorProfile {
    @Id
    @GeneratedValue
    @Column(nullable = false)
    private Long profileId;
    @Column(nullable = false, unique = true)
    private String licenseNumber;
    @Column(nullable = false)
    private String specialisation;
    @Column(nullable = false)
    private double consultationFee;
    @Column(nullable = false)
    @Convert(converter = AvailabilityConverter.class)
    private List<Availability> availabilityList;
    @OneToOne
    @JoinColumn(name = "userId")
    private User userProfile;
}
