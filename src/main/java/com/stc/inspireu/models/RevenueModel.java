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
@Table(name = "revenue_models")
public class RevenueModel extends BaseEntity{

	@Column(nullable = false)
	private String revenueModel;

	public RevenueModel(String revenueModel) {
		super();
		this.revenueModel = revenueModel;
	}

}
