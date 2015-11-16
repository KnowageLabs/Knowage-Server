/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.config.bo;

import java.io.Serializable;

public class KpiDocuments implements Serializable{

	Integer kpiDocId;
	Integer KpiId;
	Integer biObjId;
	String biObjLabel;
	
	public Integer getKpiDocId() {
		return kpiDocId;
	}
	public void setKpiDocId(Integer kpiDocId) {
		this.kpiDocId = kpiDocId;
	}
	public Integer getKpiId() {
		return KpiId;
	}
	public void setKpiId(Integer kpiId) {
		KpiId = kpiId;
	}
	public Integer getBiObjId() {
		return biObjId;
	}
	public void setBiObjId(Integer biObjId) {
		this.biObjId = biObjId;
	}
	public String getBiObjLabel() {
		return biObjLabel;
	}
	public void setBiObjLabel(String biObjLabel) {
		this.biObjLabel = biObjLabel;
	}
	

	
}
