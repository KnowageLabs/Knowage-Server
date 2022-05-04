package it.eng.spagobi.calendar.dto;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import it.eng.spagobi.services.validation.ExtendedAlphanumeric;
import it.eng.spagobi.services.validation.Xss;

@JsonIgnoreProperties({ "realDateGenerated", "splittedCalendar" })
public class SaveCalendarRequestDTO {

	private String calendar;

	private String calType;

	private Long calEndDay;

	private Long calStartDay;

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

}
