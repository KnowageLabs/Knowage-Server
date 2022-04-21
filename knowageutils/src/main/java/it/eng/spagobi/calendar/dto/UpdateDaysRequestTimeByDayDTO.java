package it.eng.spagobi.calendar.dto;

/**
 * TODO Is this some "date object" from JS? Simplify it!
 */
public class UpdateDaysRequestTimeByDayDTO {

	private String timeId; // TODO Map it as a number (typical value is like "20201224")
	private String timeDate; // TODO Map it as a number (typical value is like "1608764400000")
	private String dayDesc;
	private String dayName;
	private String dayOfWeek; // TODO Map it as a number (typical value is like "5")
	private String dayOfMonth; // TODO Map it as a number (typical value is like "24")
	private String dayOfYear; // TODO Map it as a number (typical value is like "359")
	private String monthId;
	private String monthDesc;
	private String monthOfQuarter; // TODO Map it as a number (typical value is like "3")
	private String monthOfYear; // TODO Map it as a number (typical value is like "12")
	private String monthName;
	private String endOfMonth; // TODO Map it as a number (typical value is like "1609369200000")
	private String daysInMonth; // TODO Map it as a number (typical value is like "31")
	private String quarterId;
	private String quarterDesc;
	private String quarterOfYear; // TODO Map it as a number (typical value is like "4")
	private String endOfQuarter; // TODO Map it as a number (typical value is like "1609369200000")
	private String daysInQuarter; // TODO Map it as a number (typical value is like "92")
	private String semesterId;
	private String semesterDesc;
	private String semesterOfYear; // TODO Map it as a number (typical value is like "2")
	private String endOfSemester; // TODO Map it as a number (typical value is like "1609369200000")
	private String daysInSemester; // TODO Map it as a number (typical value is like "183")
	private String yearId; // TODO Map it as a number (typical value is like "2020")
	private String daysInYear; // TODO Map it as a number (typical value is like "366")
	private String endOfYear; // TODO Map it as a number (typical value is like "1609369200000")
	private String daysInWeek; // TODO Map it as a number (typical value is like "7")
	private String calWeekId;
	private String calWeekDesc;
	private String calWeekOfYear; // TODO Map it as a number (typical value is like "52")
	private String endOfCalWeek; // TODO Map it as a number (typical value is like "1609196400000")
	private String isoWeekId;
	private String isoWeekDesc;
	private String isoWeekOfYear; // TODO Map it as a number (typical value is like "52")
	private String endOfIsoWeek; // TODO Map it as a number (typical value is like "1609023600000")

	public String getTimeId() {
		return timeId;
	}

	public void setTimeId(String timeId) {
		this.timeId = timeId;
	}

	public String getTimeDate() {
		return timeDate;
	}

	public void setTimeDate(String timeDate) {
		this.timeDate = timeDate;
	}

	public String getDayDesc() {
		return dayDesc;
	}

	public void setDayDesc(String dayDesc) {
		this.dayDesc = dayDesc;
	}

	public String getDayName() {
		return dayName;
	}

	public void setDayName(String dayName) {
		this.dayName = dayName;
	}

	public String getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public String getDayOfMonth() {
		return dayOfMonth;
	}

	public void setDayOfMonth(String dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}

	public String getDayOfYear() {
		return dayOfYear;
	}

	public void setDayOfYear(String dayOfYear) {
		this.dayOfYear = dayOfYear;
	}

	public String getMonthId() {
		return monthId;
	}

	public void setMonthId(String monthId) {
		this.monthId = monthId;
	}

	public String getMonthDesc() {
		return monthDesc;
	}

	public void setMonthDesc(String monthDesc) {
		this.monthDesc = monthDesc;
	}

	public String getMonthOfQuarter() {
		return monthOfQuarter;
	}

	public void setMonthOfQuarter(String monthOfQuarter) {
		this.monthOfQuarter = monthOfQuarter;
	}

	public String getMonthOfYear() {
		return monthOfYear;
	}

	public void setMonthOfYear(String monthOfYear) {
		this.monthOfYear = monthOfYear;
	}

	public String getMonthName() {
		return monthName;
	}

	public void setMonthName(String monthName) {
		this.monthName = monthName;
	}

	public String getEndOfMonth() {
		return endOfMonth;
	}

	public void setEndOfMonth(String endOfMonth) {
		this.endOfMonth = endOfMonth;
	}

	public String getDaysInMonth() {
		return daysInMonth;
	}

	public void setDaysInMonth(String daysInMonth) {
		this.daysInMonth = daysInMonth;
	}

	public String getQuarterId() {
		return quarterId;
	}

	public void setQuarterId(String quarterId) {
		this.quarterId = quarterId;
	}

	public String getQuarterDesc() {
		return quarterDesc;
	}

	public void setQuarterDesc(String quarterDesc) {
		this.quarterDesc = quarterDesc;
	}

	public String getQuarterOfYear() {
		return quarterOfYear;
	}

	public void setQuarterOfYear(String quarterOfYear) {
		this.quarterOfYear = quarterOfYear;
	}

	public String getEndOfQuarter() {
		return endOfQuarter;
	}

	public void setEndOfQuarter(String endOfQuarter) {
		this.endOfQuarter = endOfQuarter;
	}

	public String getDaysInQuarter() {
		return daysInQuarter;
	}

	public void setDaysInQuarter(String daysInQuarter) {
		this.daysInQuarter = daysInQuarter;
	}

	public String getSemesterId() {
		return semesterId;
	}

	public void setSemesterId(String semesterId) {
		this.semesterId = semesterId;
	}

	public String getSemesterDesc() {
		return semesterDesc;
	}

	public void setSemesterDesc(String semesterDesc) {
		this.semesterDesc = semesterDesc;
	}

	public String getSemesterOfYear() {
		return semesterOfYear;
	}

	public void setSemesterOfYear(String semesterOfYear) {
		this.semesterOfYear = semesterOfYear;
	}

	public String getEndOfSemester() {
		return endOfSemester;
	}

	public void setEndOfSemester(String endOfSemester) {
		this.endOfSemester = endOfSemester;
	}

	public String getDaysInSemester() {
		return daysInSemester;
	}

	public void setDaysInSemester(String daysInSemester) {
		this.daysInSemester = daysInSemester;
	}

	public String getYearId() {
		return yearId;
	}

	public void setYearId(String yearId) {
		this.yearId = yearId;
	}

	public String getDaysInYear() {
		return daysInYear;
	}

	public void setDaysInYear(String daysInYear) {
		this.daysInYear = daysInYear;
	}

	public String getEndOfYear() {
		return endOfYear;
	}

	public void setEndOfYear(String endOfYear) {
		this.endOfYear = endOfYear;
	}

	public String getDaysInWeek() {
		return daysInWeek;
	}

	public void setDaysInWeek(String daysInWeek) {
		this.daysInWeek = daysInWeek;
	}

	public String getCalWeekId() {
		return calWeekId;
	}

	public void setCalWeekId(String calWeekId) {
		this.calWeekId = calWeekId;
	}

	public String getCalWeekDesc() {
		return calWeekDesc;
	}

	public void setCalWeekDesc(String calWeekDesc) {
		this.calWeekDesc = calWeekDesc;
	}

	public String getCalWeekOfYear() {
		return calWeekOfYear;
	}

	public void setCalWeekOfYear(String calWeekOfYear) {
		this.calWeekOfYear = calWeekOfYear;
	}

	public String getEndOfCalWeek() {
		return endOfCalWeek;
	}

	public void setEndOfCalWeek(String endOfCalWeek) {
		this.endOfCalWeek = endOfCalWeek;
	}

	public String getIsoWeekId() {
		return isoWeekId;
	}

	public void setIsoWeekId(String isoWeekId) {
		this.isoWeekId = isoWeekId;
	}

	public String getIsoWeekDesc() {
		return isoWeekDesc;
	}

	public void setIsoWeekDesc(String isoWeekDesc) {
		this.isoWeekDesc = isoWeekDesc;
	}

	public String getIsoWeekOfYear() {
		return isoWeekOfYear;
	}

	public void setIsoWeekOfYear(String isoWeekOfYear) {
		this.isoWeekOfYear = isoWeekOfYear;
	}

	public String getEndOfIsoWeek() {
		return endOfIsoWeek;
	}

	public void setEndOfIsoWeek(String endOfIsoWeek) {
		this.endOfIsoWeek = endOfIsoWeek;
	}

}
