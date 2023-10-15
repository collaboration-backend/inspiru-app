package com.stc.inspireu.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity()
@Getter
@Setter
@NoArgsConstructor
@Table(name = "startup_attendance", indexes = {@Index(columnList = "eventType, eventTypeId, startupId")})
public class StartupAttendance extends BaseEntity {

    @Column(nullable = false, name = "eventType")
    private String eventType;

    @Column(nullable = false, name = "eventTypeId")
    private Long eventTypeId;

    @Column(nullable = false, name = "startupId")
    private Long startupId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String userAttendanceJson;
}
