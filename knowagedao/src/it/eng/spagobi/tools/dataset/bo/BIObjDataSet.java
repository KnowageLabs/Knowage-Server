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
