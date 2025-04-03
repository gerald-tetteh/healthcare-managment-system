package io.geraldaddo.hc.user_data_module.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.geraldaddo.hc.user_data_module.attribute_converters.RolesConverter;
import io.geraldaddo.hc.user_data_module.enums.Role;
import io.geraldaddo.hc.user_data_module.enums.Sex;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements UserDetails {
    @Id
    @GeneratedValue
    @Column(nullable = false)
    private Integer userId;
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Column(nullable = false)
    private Integer age;
    @Column(nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate dateOfBirth;
    @Column(nullable = false)
    private String number;
    @Column(nullable = false)
    @ColumnDefault("true")
    private boolean active;
    @Column(nullable = false)
    @Convert(converter = RolesConverter.class)
    private Set<Role> roles;
    @Column(nullable = false)
    private String emergencyFirstName;
    @Column(nullable = false)
    private String emergencyLastName;
    @Column(nullable = false)
    private String emergencyNumber;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String addressLineOne;
    @Column(nullable = false)
    private String addressLineTwo;
    @Column(nullable = false)
    private String county;
    @Column(nullable = false)
    private String city;
    @Column(nullable = false)
    private String postCode;
    @Column(nullable = false)
    private String country;
    @Column
    private String insuranceNumber;
    @Column(nullable = false)
    private Sex sex;
    @Column(nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joined;
    @CreationTimestamp
    @Column(nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return active;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return active;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    public Integer getUserId() {
        return userId;
    }

    public User setUserId(Integer userId) {
        this.userId = userId;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public User setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public User setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public Integer getAge() {
        return age;
    }

    public User setAge(Integer age) {
        this.age = age;
        return this;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public User setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public String getNumber() {
        return number;
    }

    public User setNumber(String number) {
        this.number = number;
        return this;
    }

    public boolean isActive() {
        return active;
    }

    public User setActive(boolean active) {
        this.active = active;
        return this;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public User setRoles(Set<Role> roles) {
        this.roles = roles;
        return this;
    }

    public String getEmergencyFirstName() {
        return emergencyFirstName;
    }

    public User setEmergencyFirstName(String emergencyFirstName) {
        this.emergencyFirstName = emergencyFirstName;
        return this;
    }

    public String getEmergencyLastName() {
        return emergencyLastName;
    }

    public User setEmergencyLastName(String emergencyLastName) {
        this.emergencyLastName = emergencyLastName;
        return this;
    }

    public String getEmergencyNumber() {
        return emergencyNumber;
    }

    public User setEmergencyNumber(String emergencyNumber) {
        this.emergencyNumber = emergencyNumber;
        return this;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getAddressLineOne() {
        return addressLineOne;
    }

    public User setAddressLineOne(String addressLineOne) {
        this.addressLineOne = addressLineOne;
        return this;
    }

    public String getAddressLineTwo() {
        return addressLineTwo;
    }

    public User setAddressLineTwo(String addressLineTwo) {
        this.addressLineTwo = addressLineTwo;
        return this;
    }

    public String getCounty() {
        return county;
    }

    public User setCounty(String county) {
        this.county = county;
        return this;
    }

    public String getInsuranceNumber() {
        return insuranceNumber;
    }

    public User setInsuranceNumber(String insuranceNumber) {
        this.insuranceNumber = insuranceNumber;
        return this;
    }

    public Sex getSex() {
        return this.sex;
    }

    public User setSex(Sex sex) {
        this.sex = sex;
        return this;
    }

    public String getCity() {
        return city;
    }

    public User setCity(String city) {
        this.city = city;
        return this;
    }

    public String getPostCode() {
        return postCode;
    }

    public User setPostCode(String postCode) {
        this.postCode = postCode;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public User setCountry(String country) {
        this.country = country;
        return this;
    }

    public LocalDateTime getJoined() {
        return joined;
    }

    public User setJoined(LocalDateTime joined) {
        this.joined = joined;
        return this;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public User setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public User setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
}