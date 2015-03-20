/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
