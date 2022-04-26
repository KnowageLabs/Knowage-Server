package it.eng.spagobi.calendar.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import it.eng.spagobi.services.validation.ExtendedAlphanumeric;
import it.eng.spagobi.services.validation.Xss;

public class UpdateDaysRequestCalendarDTO {

	private String calendarId;

	private String calendar;

	private String calType;

	private Long calEndDay;

	private Long calStartDay;

	private String recStatus;

	@Xss
	@NotNull
	public String getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	@Xss
	@ExtendedAlphanumeric
	@NotNull
	public String getCalendar() {
		return calendar;
	}

	public void setCalendar(String calendar) {
		this.calendar = calendar;
	}

	@Xss
	@ExtendedAlphanumeric
	@NotNull
	public String getCalType() {
		return calType;
	}

	public void setCalType(String calType) {
		this.calType = calType;
	}

	@NotNull
	public Long getCalEndDay() {
		return calEndDay;
	}

	public void setCalEndDay(Long calEndDay) {
		this.calEndDay = calEndDay;
	}

	@NotNull
	public Long getCalStartDay() {
		return calStartDay;
	}

	public void setCalStartDay(Long calStartDay) {
		this.calStartDay = calStartDay;
	}

	@Size(max = 1)
	public String getRecStatus() {
		return recStatus;
	}

	public void setRecStatus(String recStatus) {
		this.recStatus = recStatus;
	}

}
