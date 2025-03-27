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

    public Long getProfileId() {
        return profileId;
    }

    public DoctorProfile setProfileId(Long profileId) {
        this.profileId = profileId;
        return this;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public DoctorProfile setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
        return this;
    }

    public String getSpecialisation() {
        return specialisation;
    }

    public DoctorProfile setSpecialisation(String specialisation) {
        this.specialisation = specialisation;
        return this;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public DoctorProfile setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
        return this;
    }

    public List<Availability> getAvailabilityList() {
        return availabilityList;
    }

    public DoctorProfile setAvailabilityList(List<Availability> availabilityList) {
        this.availabilityList = availabilityList;
        return this;
    }

    public User getUserProfile() {
        return userProfile;
    }

    public DoctorProfile setUserProfile(User userProfile) {
        this.userProfile = userProfile;
        return this;
    }
}
