package com.stc.inspireu.models;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "calendar_events")
public class CalendarEvent extends BaseEntity{

	@Column
	private Date sessionStart;

	@Column
	private Date sessionEnd;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "startupId")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Startup startup;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "slotId")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Slot slot;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "oneToOneMeetingId")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private OneToOneMeeting oneToOneMeeting;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "trainingSessionId")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private TrainingSession trainingSession;

	@Column
	private String sessionStartTime;

	@Column
	private String sessionEndTime;

	@Column
	private Boolean isRecurring;
}
