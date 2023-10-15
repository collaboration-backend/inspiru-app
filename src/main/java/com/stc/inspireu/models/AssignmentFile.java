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
@Table(name = "assignment_files")
public class AssignmentFile extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String path;

    @Column
    private Boolean willManagement;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "assignmentId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Assignment assignment;

}
