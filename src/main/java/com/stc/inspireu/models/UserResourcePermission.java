package com.stc.inspireu.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user_resource_permissions", indexes = {
    @Index(name = "uniqueMulitIndex", columnList = "userId, resourceId, resource", unique = true)})
public class UserResourcePermission extends BaseEntity {

    @Column(columnDefinition = "TEXT", name = "urlPath")
    private String urlPath;

    @Column(nullable = false, name = "resource")
    private String resource;

    @Column(nullable = false, name = "resourceId")
    private Long resourceId;

    @Column(name = "parentResourceId")
    private Long parentResourceId;

    @Column(name = "grandParentResourceId")
    private Long grandParentResourceId;

    @Column(nullable = false, name = "actionsOrMethods")
    private String actionsOrMethods;

    @Column(name = "userId")
    private Long userId;

    @Column(name = "startupId")
    private Long startupId;

    @Column(name = "intakeProgramId")
    private Long intakeProgramId;
}
