package it.eng.spagobi.calendar.dto;

import java.util.ArrayList;
import java.util.List;

public class UpdateDaysRequestDTO {
	private Integer idCalComposition;

	private Integer isHoliday;

	private String pubHoliday;

	private String recStatus;

	private String calendarId;

	private String timeId;

	private UpdateDaysRequestCalendarDTO calendar;

	private UpdateDaysRequestTimeByDayDTO timeByDay;

	private final List<Object> listOfAttributes = new ArrayList<>();

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

	public String getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	public String getTimeId() {
		return timeId;
	}

	public void setTimeId(String timeId) {
		this.timeId = timeId;
	}

	public UpdateDaysRequestCalendarDTO getCalendar() {
		return calendar;
	}

	public void setCalendar(UpdateDaysRequestCalendarDTO calendar) {
		this.calendar = calendar;
	}

	public UpdateDaysRequestTimeByDayDTO getTimeByDay() {
		return timeByDay;
	}

	public void setTimeByDay(UpdateDaysRequestTimeByDayDTO timeByDay) {
		this.timeByDay = timeByDay;
	}

	public List<Object> getListOfAttributes() {
		return listOfAttributes;
	}

}
