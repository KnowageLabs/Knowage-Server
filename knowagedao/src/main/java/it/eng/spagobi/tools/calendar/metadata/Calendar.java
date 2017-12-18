package it.eng.spagobi.tools.calendar.metadata;

import java.util.Date;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class Calendar extends SbiHibernateModel {
	private Integer calendarId;
	private String calendar;
	private String calType;
	private Date calStartDay;
	private Date calEndDay;
	private String recStatus;

	public Integer getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(Integer calendarId) {
		this.calendarId = calendarId;
	}

	public String getCalendar() {
		return calendar;
	}

	public void setCalendar(String calendar) {
		this.calendar = calendar;
	}

	public String getCalType() {
		return calType;
	}

	public void setCalType(String calType) {
		this.calType = calType;
	}

	public Date getCalStartDay() {
		return calStartDay;
	}

	public void setCalStartDay(Date calStartDay) {
		this.calStartDay = calStartDay;
	}

	public Date getCalEndDay() {
		return calEndDay;
	}

	public void setCalEndDay(Date calEndDay) {
		this.calEndDay = calEndDay;
	}

	public String getRecStatus() {
		return recStatus;
	}

	public void setRecStatus(String recStatus) {
		this.recStatus = recStatus;
	}

}
