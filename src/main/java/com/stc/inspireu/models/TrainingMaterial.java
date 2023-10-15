package com.stc.inspireu.models;

import com.stc.inspireu.enums.Constant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "training_materials")
public class TrainingMaterial extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String path;

    @Column
    private String description;

    @Column(nullable = false)
    private String status = Constant.NOT_SUBMITTED.toString();

    @ManyToOne(fetch = FetchType.LAZY) // cascade = CascadeType.ALL
    @JoinColumn(name = "createdUserId")
    private User createdUser;

    @ManyToOne(fetch = FetchType.LAZY) // cascade = CascadeType.ALL
    @JoinColumn(name = "workshopSessionId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private WorkshopSession workshopSession;
}
