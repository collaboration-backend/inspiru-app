package com.stc.inspireu.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

    @Column(unique = true)
    private String email;

    @Column
    private String phoneNumber;

    @Column
    private String phoneDialCode;

    @Column
    private String phoneCountryCodeIso2;

    @Column
    @JsonIgnore
    private String password;

    @Column(columnDefinition = "boolean default true", name = "otp_verified")
    private Boolean otpVerified;

    @Column
    private String otp;

    @Column
    private String passwordResetToken;

    @Column(columnDefinition = "TEXT")
    private String inviteToken;

    @Column
    private String alias;

    @Column
    private String jobTitle;

    @Column
    private Boolean enableEmail;

    @Column
    private Boolean enableWeb;

    @Column(nullable = false)
    private String invitationStatus;

    @Column
    private String profilePic;

    @Column
    private String signaturePic;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "startupId")
    private Startup startup;

    @Column
    private Boolean willManagement;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinTable(name = "users_intake_programs", joinColumns = {
        @JoinColumn(referencedColumnName = "id", name = "userId")}, inverseJoinColumns = {
        @JoinColumn(referencedColumnName = "id", name = "intakeProgramId")})
    private Set<IntakeProgram> intakePrograms;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "roleId")
    private Role role;

    @Column
    private Boolean isRemovable;

    @Column(name = "failure_count")
    private Integer failureCount;

    @Column(name = "last_failure_time")
    private LocalDateTime lastFailureTime;
}
