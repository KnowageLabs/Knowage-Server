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

public class KpiInstPeriod implements Serializable{

	Integer id;
	Integer kpiInstId;
	Integer periodicityId;
	Boolean defaultValue;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getKpiInstId() {
		return kpiInstId;
	}
	public void setKpiInstId(Integer kpiInstId) {
		this.kpiInstId = kpiInstId;
	}
	public Integer getPeriodicityId() {
		return periodicityId;
	}
	public void setPeriodicityId(Integer periodicityId) {
		this.periodicityId = periodicityId;
	}
	public Boolean getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(Boolean defaultValue) {
		this.defaultValue = defaultValue;
	}
	
}


