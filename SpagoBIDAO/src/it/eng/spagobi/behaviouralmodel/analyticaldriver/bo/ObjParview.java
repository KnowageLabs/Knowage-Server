/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.behaviouralmodel.analyticaldriver.bo;

import java.io.Serializable;

/**
 * Defines a Business Intelligence object
 */
public class ObjParview implements Serializable {

	private Integer objParId;
    private Integer objParFatherId;
    private Integer prog;
    private String operation;
    private String compareValue;
    private String viewLabel;
    
    
    
	public Integer getObjParId() {
		return objParId;
	}
	public void setObjParId(Integer objParId) {
		this.objParId = objParId;
	}
	public Integer getObjParFatherId() {
		return objParFatherId;
	}
	public void setObjParFatherId(Integer objParFatherId) {
		this.objParFatherId = objParFatherId;
	}
	public Integer getProg() {
		return prog;
	}
	public void setProg(Integer prog) {
		this.prog = prog;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public String getCompareValue() {
		return compareValue;
	}
	public void setCompareValue(String compareValue) {
		this.compareValue = compareValue;
	}
	public String getViewLabel() {
		return viewLabel;
	}
	public void setViewLabel(String viewLabel) {
		this.viewLabel = viewLabel;
	}
    


}
