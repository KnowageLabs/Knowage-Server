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
package it.eng.spagobi.engines.kpi;

import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;

import java.util.Date;
import java.util.HashMap;

public class KpiParametrization {
	private HashMap parametersObject;
	private Date dateOfKPI;
	private Date endKpiValueDate;
	private String behaviour;
	private Date timeRangeFrom;
	private Date timeRangeTo;
	private Date dateIntervalFrom;
	private Date dateIntervalTo;
	private String visibilityParameterValues = null;//added for new GUI
	
	public KpiParametrization(Date dateOfKPI,
			Date endKpiValueDate, String behaviour, Date timeRangeFrom,
			Date timeRangeTo, Date dateIntervalFrom, Date dateIntervalTo,
			String visibilityParameterValues) {
		this.dateOfKPI = dateOfKPI;
		this.endKpiValueDate = endKpiValueDate;
		this.behaviour = behaviour;
		this.timeRangeFrom = timeRangeFrom;
		this.timeRangeTo = timeRangeTo;
		this.dateIntervalFrom = dateIntervalFrom;
		this.dateIntervalTo = dateIntervalTo;
		this.visibilityParameterValues = visibilityParameterValues;

	}

	public String getVisibilityParameterValues() {
		return visibilityParameterValues;
	}

	public void setVisibilityParameterValues(String visibilityParameterValues) {
		this.visibilityParameterValues = visibilityParameterValues;
	}

	public HashMap getParametersObject() {
		return parametersObject;
	}

	public void setParametersObject(HashMap parametersObject) {
		this.parametersObject = parametersObject;
	}

	public Date getDateOfKPI() {
		return dateOfKPI;
	}

	public void setDateOfKPI(Date dateOfKPI) {
		this.dateOfKPI = dateOfKPI;
	}

	public Date getEndKpiValueDate() {
		return endKpiValueDate;
	}

	public void setEndKpiValueDate(Date endKpiValueDate) {
		this.endKpiValueDate = endKpiValueDate;
	}

	public String getBehaviour() {
		return behaviour;
	}

	public void setBehaviour(String behaviour) {
		this.behaviour = behaviour;
	}

	public Date getTimeRangeFrom() {
		return timeRangeFrom;
	}

	public void setTimeRangeFrom(Date timeRangeFrom) {
		this.timeRangeFrom = timeRangeFrom;
	}

	public Date getTimeRangeTo() {
		return timeRangeTo;
	}

	public void setTimeRangeTo(Date timeRangeTo) {
		this.timeRangeTo = timeRangeTo;
	}

	public Date getDateIntervalFrom() {
		return dateIntervalFrom;
	}

	public void setDateIntervalFrom(Date dateIntervalFrom) {
		this.dateIntervalFrom = dateIntervalFrom;
	}

	public Date getDateIntervalTo() {
		return dateIntervalTo;
	}

	public void setDateIntervalTo(Date dateIntervalTo) {
		this.dateIntervalTo = dateIntervalTo;
	}
}
