package com.stc.inspireu.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "partners")
public class Partner extends BaseEntity {

    @Column
    private String name;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column
    private String phoneNumber;

    @Column
    private String phoneDialCode;

    @Column
    private String phoneCountryCodeIso2;

    @Column
    private String email;

    @Column
    private String link;

    @Column
    private String logo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;
}
