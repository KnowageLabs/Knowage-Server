package it.eng.spagobi.kpi.bo;

import java.util.Date;

// TODO: remove the old class it.eng.spagobi.kpi.bo.config.KpiValue
public class KpiValue implements Cloneable {

	private int id;
	private int kpiId;
	private int kpiVersion;
	private String logicalKey;
	private Date timeRun;
	private float value;
	private String valueDay;
	private String valueWeek;
	private String valueMonth;
	private String valueQ;
	private String valueYear;

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

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}

	public String getValueDay() {
		return valueDay;
	}

	public void setValueDay(String valueDay) {
		this.valueDay = valueDay;
	}

	public String getValueWeek() {
		return valueWeek;
	}

	public void setValueWeek(String valueWeek) {
		this.valueWeek = valueWeek;
	}

	public String getValueMonth() {
		return valueMonth;
	}

	public void setValueMonth(String valueMonth) {
		this.valueMonth = valueMonth;
	}

	public String getValueQ() {
		return valueQ;
	}

	public void setValueQ(String valueQ) {
		this.valueQ = valueQ;
	}

	public String getValueYear() {
		return valueYear;
	}

	public void setValueYear(String valueYear) {
		this.valueYear = valueYear;
	}

}
