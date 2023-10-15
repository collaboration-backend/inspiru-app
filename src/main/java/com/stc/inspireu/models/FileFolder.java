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
@Table(name = "files_folders")
public class FileFolder extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String uid;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column
    private Boolean isFile;

    @Column
    private String parentFolder;

    @Column
    private String tags;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "createdUserId")
    private User createdUser;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "refFileFolder")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private FileFolder refFileFolder;

    @Column
    private Boolean isPublic;
}
