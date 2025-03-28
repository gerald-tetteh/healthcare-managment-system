package io.geraldaddo.hc.patients_service.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserProfileDto {
    private String firstName;
    private String lastName;
    private Integer age;
    private LocalDate dateOfBirth;
    private String number;
    private String emergencyFirstName;
    private String emergencyLastName;
    private String emergencyNumber;
    private String email;
    private String addressLineOne;
    private String addressLineTwo;
    private String county;
    private String insuranceNumber;
    private String city;
    private String postCode;
    private String country;
    private LocalDateTime joined;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Integer getAge() {
        return age;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getNumber() {
        return number;
    }

    public String getEmergencyFirstName() {
        return emergencyFirstName;
    }

    public String getEmergencyLastName() {
        return emergencyLastName;
    }

    public String getEmergencyNumber() {
        return emergencyNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getAddressLineOne() {
        return addressLineOne;
    }

    public String getAddressLineTwo() {
        return addressLineTwo;
    }

    public String getCounty() {
        return county;
    }

    public String getInsuranceNumber() {
        return insuranceNumber;
    }

    public String getCity() {
        return city;
    }

    public String getPostCode() {
        return postCode;
    }

    public String getCountry() {
        return country;
    }

    public LocalDateTime getJoined() {
        return joined;
    }

    public UserProfileDto setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public UserProfileDto setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public UserProfileDto setAge(Integer age) {
        this.age = age;
        return this;
    }

    public UserProfileDto setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public UserProfileDto setNumber(String number) {
        this.number = number;
        return this;
    }

    public UserProfileDto setEmergencyFirstName(String emergencyFirstName) {
        this.emergencyFirstName = emergencyFirstName;
        return this;
    }

    public UserProfileDto setEmergencyLastName(String emergencyLastName) {
        this.emergencyLastName = emergencyLastName;
        return this;
    }

    public UserProfileDto setEmergencyNumber(String emergencyNumber) {
        this.emergencyNumber = emergencyNumber;
        return this;
    }

    public UserProfileDto setEmail(String email) {
        this.email = email;
        return this;
    }

    public UserProfileDto setAddressLineOne(String addressLineOne) {
        this.addressLineOne = addressLineOne;
        return this;
    }

    public UserProfileDto setAddressLineTwo(String addressLineTwo) {
        this.addressLineTwo = addressLineTwo;
        return this;
    }

    public UserProfileDto setCounty(String county) {
        this.county = county;
        return this;
    }

    public UserProfileDto setInsuranceNumber(String insuranceNumber) {
        this.insuranceNumber = insuranceNumber;
        return this;
    }

    public UserProfileDto setCity(String city) {
        this.city = city;
        return this;
    }

    public UserProfileDto setPostCode(String postCode) {
        this.postCode = postCode;
        return this;
    }

    public UserProfileDto setCountry(String country) {
        this.country = country;
        return this;
    }

    public UserProfileDto setJoined(LocalDateTime joined) {
        this.joined = joined;
        return this;
    }
}
