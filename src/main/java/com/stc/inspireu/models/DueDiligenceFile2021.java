package com.stc.inspireu.models;

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
@Table(name = "due_diligence_files")
public class DueDiligenceFile2021 extends BaseEntity {
    @Column
    private String fieldId;

    @Column(nullable = false)
    private String name;

    @Column
    private String path;

    @Column
    private String status;

    @ManyToOne(fetch = FetchType.LAZY) // cascade = CascadeType.ALL
    @JoinColumn(name = "createdUserId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User createdUser;

    @ManyToOne(fetch = FetchType.LAZY) // cascade = CascadeType.ALL
    @JoinColumn(name = "startupId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Startup startup;

    @ManyToOne(fetch = FetchType.LAZY) // cascade = CascadeType.ALL
    @JoinColumn(name = "dueDiligenceTemplateId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DueDiligenceTemplate2021 dueDiligenceTemplate2021;

}
