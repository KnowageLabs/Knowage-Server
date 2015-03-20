/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.config.bo;

import java.io.Serializable;

public class MeasureUnit implements Serializable{

	Integer id=null;
	String name=null;
	String scaleCd=null;
	String scaleNm=null;
	Integer scaleTypeId=null;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getScaleCd() {
		return scaleCd;
	}
	public void setScaleCd(String scaleCd) {
		this.scaleCd = scaleCd;
	}
	public String getScaleNm() {
		return scaleNm;
	}
	public void setScaleNm(String scaleNm) {
		this.scaleNm = scaleNm;
	}
	public Integer getScaleTypeId() {
		return scaleTypeId;
	}
	public void setScaleTypeId(Integer scaleTypeId) {
		this.scaleTypeId = scaleTypeId;
	}

}
