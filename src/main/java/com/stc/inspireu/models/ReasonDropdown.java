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
@Table(name = "reason_dropdowns")
public class ReasonDropdown extends BaseEntity {

    @Column(nullable = false)
    private String reason;

    public ReasonDropdown(String reason) {
        super();
        this.reason = reason;
    }
}
