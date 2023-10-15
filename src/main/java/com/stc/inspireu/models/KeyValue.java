package com.stc.inspireu.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "key_values")
public class KeyValue {

    @Id
    @Column(name = "keyName")
    private String keyName;

    @Column(columnDefinition = "TEXT")
    private String valueName;

    @Column
    private String description;
}
