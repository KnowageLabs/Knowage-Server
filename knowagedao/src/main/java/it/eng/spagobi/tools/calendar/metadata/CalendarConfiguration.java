package it.eng.spagobi.tools.calendar.metadata;

import java.util.Set;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class CalendarConfiguration extends SbiHibernateModel {
	private Integer idCalComposition;
	private Integer isHoliday;
	private String pubHoliday;
	private String recStatus;

	private Integer calendarId;
	private Integer timeId;

	private Calendar calendar;
	private TimeByDay timeByDay;

	private Set<CalendarAttribute> listOfAttributes;

	public Set<CalendarAttribute> getListOfAttributes() {
		return listOfAttributes;
	}

	public void setListOfAttributes(Set<CalendarAttribute> listOfAttributes) {
		this.listOfAttributes = listOfAttributes;
	}

	public Integer getIdCalComposition() {
		return idCalComposition;
	}

	public void setIdCalComposition(Integer idCalComposition) {
		this.idCalComposition = idCalComposition;
	}

	public Integer getIsHoliday() {
		return isHoliday;
	}

	public void setIsHoliday(Integer isHoliday) {
		this.isHoliday = isHoliday;
	}

	public String getPubHoliday() {
		return pubHoliday;
	}

	public void setPubHoliday(String pubHoliday) {
		this.pubHoliday = pubHoliday;
	}

	public String getRecStatus() {
		return recStatus;
	}

	public void setRecStatus(String recStatus) {
		this.recStatus = recStatus;
	}

	public Integer getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(Integer calendarId) {
		this.calendarId = calendarId;
	}

	public Integer getTimeId() {
		return timeId;
	}

	public void setTimeId(Integer timeId) {
		this.timeId = timeId;
	}

	public Calendar getCalendar() {
		return calendar;
	}

	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
	}

	public TimeByDay getTimeByDay() {
		return timeByDay;
	}

	public void setTimeByDay(TimeByDay timeByDay) {
		this.timeByDay = timeByDay;
	}

}
