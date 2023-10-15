package com.stc.inspireu.models;

import com.stc.inspireu.enums.Activity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "activity_logs")
public class ActivityLog extends BaseEntity {

    @Column(name = "time_stamp")
    private LocalDateTime timeStamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity")
    private Activity activity;

    @Column(name = "ip_address")
    private String ipAddress;

    public ActivityLog(LocalDateTime timeStamp, Activity activity, String ipAddress) {
        this.timeStamp = timeStamp;
        this.activity = activity;
        this.ipAddress = ipAddress;
    }
}
