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
package it.eng.spagobi.kpi.config.bo;

import java.io.Serializable;
import java.util.Date;

public class KpiInstance implements Serializable{
	
	Integer kpiInstanceId = null;
	Integer periodicityId = null;// kpiInstPeriodicyId
	Integer chartTypeId = null;
	Integer thresholdId = null;
	Integer kpiId = null ;// kpiId related to the kpiInstance
	String scaleCode = null;
	String scaleName = null;	
	Date d = null;	
	Double weight = null; 
	Double target = null;
	boolean saveKpiHistory = false; // if is true the value of kpiInstance
                                    //will store in the kpiInstance History table
	public Integer getPeriodicityId() {
		return periodicityId;
	}

	public void setPeriodicityId(Integer periodicityId) {
		this.periodicityId = periodicityId;
	}

	public Integer getChartTypeId() {
		return chartTypeId;
	}

	public void setChartTypeId(Integer chartTypeId) {
		this.chartTypeId = chartTypeId;
	}

	public Integer getThresholdId() {
		return thresholdId;
	}

	public void setThresholdId(Integer thresholdId) {
		this.thresholdId = thresholdId;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}

	public KpiInstance() {
		super();
	}

	public Integer getKpiInstanceId() {
		return kpiInstanceId;
	}

	public void setKpiInstanceId(Integer kpiInstanceId) {
		this.kpiInstanceId = kpiInstanceId;
	}

	public Integer getKpi() {
		return kpiId;
	}

	public void setKpi(Integer kpiId) {
		this.kpiId = kpiId;
	}


	public Date getD() {
		return d;
	}

	public void setD(Date d) {
		this.d = d;
	}

	public Double getTarget() {
		return target;
	}

	public void setTarget(Double target) {
		this.target = target;
	}

	public String getScaleCode() {
		return scaleCode;
	}

	public void setScaleCode(String scaleCode) {
		this.scaleCode = scaleCode;
	}

	public String getScaleName() {
		return scaleName;
	}

	public void setScaleName(String scaleName) {
		this.scaleName = scaleName;
	}

	public boolean isSaveKpiHistory() {
		return saveKpiHistory;
	}

	public void setSaveKpiHistory(boolean saveKpiHistory) {
		this.saveKpiHistory = saveKpiHistory;
	}
	

}
