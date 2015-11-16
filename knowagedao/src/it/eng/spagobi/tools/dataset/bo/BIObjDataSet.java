/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.bo;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;

import java.io.Serializable;

/**
 * Defines a <code>BIObjDataSet</code> object.
 * 
 * @author Gavardi This class map the SBI_OBJ_DATA_SET table
 */
public class BIObjDataSet implements Serializable {

	private Integer biObjDsId;
	private BIObject biObject;
	private Integer dataSetId;
	private Boolean isDetail;

	public Integer getBiObjDsId() {
		return biObjDsId;
	}

	public void setBiObjDsId(Integer biObjDsId) {
		this.biObjDsId = biObjDsId;
	}

	public BIObject getBiObject() {
		return biObject;
	}

	public void setBiObject(BIObject biObject) {
		this.biObject = biObject;
	}

	public Integer getDataSetId() {
		return dataSetId;
	}

	public void setDataSetId(Integer dataSetId) {
		this.dataSetId = dataSetId;
	}

	public Boolean getIsDetail() {
		return isDetail;
	}

	public void setIsDetail(Boolean isDetail) {
		this.isDetail = isDetail;
	}

}
