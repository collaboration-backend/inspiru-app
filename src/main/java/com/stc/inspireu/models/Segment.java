package com.stc.inspireu.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "segments")
public class Segment extends BaseEntity {

    @Column(nullable = false)
    private String segment;

    public Segment(String segment) {
        super();
        this.segment = segment;
    }

}
