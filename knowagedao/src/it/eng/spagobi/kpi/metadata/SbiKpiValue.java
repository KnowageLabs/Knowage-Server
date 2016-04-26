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
