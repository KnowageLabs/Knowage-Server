/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.kpi.metadata;

import java.util.Date;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiKpiValue extends SbiHibernateModel implements java.io.Serializable {

	private static final long serialVersionUID = 8517408854421461194L;

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
	private char state;
	private String manualNote;

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

	public char getState() {
		return state;
	}

	public void setState(char state) {
		this.state = state;
	}

	public String getManualNote() {
		return manualNote;
	}

	public void setManualNote(String manualNote) {
		this.manualNote = manualNote;
	}

}
