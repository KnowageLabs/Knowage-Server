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

public class KpiRel implements Serializable{
	
	private Integer kpiRelId =null;
	private Integer kpiFatherId = null;
	private Integer kpiChildId =null;
	private String parameter = null;
	private Kpi kpiChild =null;
	
	public Integer getKpiRelId() {
		return kpiRelId;
	}
	public void setKpiRelId(Integer kpiRelId) {
		this.kpiRelId = kpiRelId;
	}
	public Integer getKpiFatherId() {
		return kpiFatherId;
	}
	public void setKpiFatherId(Integer kpiFatherId) {
		this.kpiFatherId = kpiFatherId;
	}
	public Integer getKpiChildId() {
		return kpiChildId;
	}
	public void setKpiChildId(Integer kpiChildId) {
		this.kpiChildId = kpiChildId;
	}
	public String getParameter() {
		return parameter;
	}
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public Kpi getKpiChild() {
		return kpiChild;
	}
	public void setKpiChild(Kpi kpiChild) {
		this.kpiChild = kpiChild;
	}

}
