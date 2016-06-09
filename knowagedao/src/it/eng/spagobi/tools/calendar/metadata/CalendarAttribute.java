package it.eng.spagobi.tools.calendar.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class CalendarAttribute extends SbiHibernateModel {
	private Integer attributeId;
	private Integer domainId;
	private Integer calendarId;
	private CalendarAttributeDomain calendarAttributeDomain = new CalendarAttributeDomain();
	private Calendar calendar = new Calendar();
	private String recStatus;

	public Integer getAttributeId() {
		return attributeId;
	}

	public void setAttributeId(Integer attributeId) {
		this.attributeId = attributeId;
	}

	public String getRecStatus() {
		return recStatus;
	}

	public void setRecStatus(String recStatus) {
		this.recStatus = recStatus;
	}

	public Integer getDomainId() {
		return domainId;
	}

	public void setDomainId(Integer domainId) {
		this.domainId = domainId;
	}

	public Integer getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(Integer calendarId) {
		this.calendarId = calendarId;
	}

	public CalendarAttributeDomain getCalendarAttributeDomain() {
		return calendarAttributeDomain;
	}

	public void setCalendarAttributeDomain(CalendarAttributeDomain calendarAttributeDomain) {
		this.calendarAttributeDomain = calendarAttributeDomain;
	}

	public Calendar getCalendar() {
		return calendar;
	}

	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
	}

}
