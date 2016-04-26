package it.eng.spagobi.kpi.bo;

import java.util.Date;

// TODO: remove the old class it.eng.spagobi.kpi.bo.config.KpiValue
public class KpiValue implements Cloneable {

	private int id;
	private int kpiId;
	private int kpiVersion;
	private String logicalKey;
	private Date timeRun;
	private double computedValue;
	private Double manualValue;
	private String theDay;
	private String theWeek;
	private String theMonth;
	private String theQuarter;
	private String theYear;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getKpiId() {
		return kpiId;
	}

	public void setKpiId(int kpiId) {
		this.kpiId = kpiId;
	}

	public int getKpiVersion() {
		return kpiVersion;
	}

	public void setKpiVersion(int kpiVersion) {
		this.kpiVersion = kpiVersion;
	}

	public String getLogicalKey() {
		return logicalKey;
	}

	public void setLogicalKey(String logicalKey) {
		this.logicalKey = logicalKey;
	}

	public Date getTimeRun() {
		return timeRun;
	}

	public void setTimeRun(Date timeRun) {
		this.timeRun = timeRun;
	}

	public double getComputedValue() {
		return computedValue;
	}

	public void setComputedValue(double computedValue) {
		this.computedValue = computedValue;
	}

	public Double getManualValue() {
		return manualValue;
	}

	public void setManualValue(Double manualValue) {
		this.manualValue = manualValue;
	}

	public String getTheDay() {
		return theDay;
	}

	public void setTheDay(String theDay) {
		this.theDay = theDay;
	}

	public String getTheWeek() {
		return theWeek;
	}

	public void setTheWeek(String theWeek) {
		this.theWeek = theWeek;
	}

	public String getTheMonth() {
		return theMonth;
	}

	public void setTheMonth(String theMonth) {
		this.theMonth = theMonth;
	}

	public String getTheQuarter() {
		return theQuarter;
	}

	public void setTheQuarter(String theQuarter) {
		this.theQuarter = theQuarter;
	}

	public String getTheYear() {
		return theYear;
	}

	public void setTheYear(String theYear) {
		this.theYear = theYear;
	}

}
