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
	private String objParFatherUrlName;

	public String getObjParFatherUrlName() {
		return objParFatherUrlName;
	}

	public void setObjParFatherUrlName(String objParFatherUrlName) {
		this.objParFatherUrlName = objParFatherUrlName;
	}

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
