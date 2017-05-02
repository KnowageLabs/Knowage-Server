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
public class ObjParuse implements Serializable {

	private Integer objParId;
	private Integer paruseId;
	private Integer objParFatherId;
	private Integer prog;
	private String filterColumn;
	private String filterOperation;
	private String preCondition;
	private String postCondition;
	private String logicOperator;
	private String objParFatherUrlName;

	public String getObjParFatherUrlName() {
		return objParFatherUrlName;
	}

	public void setObjParFatherUrlName(String objParFatherUrlName) {
		this.objParFatherUrlName = objParFatherUrlName;
	}

	/**
	 * Gets the filter column.
	 *
	 * @return the filter column
	 */
	public String getFilterColumn() {
		return filterColumn;
	}

	/**
	 * Sets the filter column.
	 *
	 * @param filterColumn
	 *            the new filter column
	 */
	public void setFilterColumn(String filterColumn) {
		this.filterColumn = filterColumn;
	}

	/**
	 * Gets the filter operation.
	 *
	 * @return the filter operation
	 */
	public String getFilterOperation() {
		return filterOperation;
	}

	/**
	 * Sets the filter operation.
	 *
	 * @param filterOperation
	 *            the new filter operation
	 */
	public void setFilterOperation(String filterOperation) {
		this.filterOperation = filterOperation;
	}

	/**
	 * Gets the obj par father id.
	 *
	 * @return the obj par father id
	 */
	public Integer getObjParFatherId() {
		return objParFatherId;
	}

	/**
	 * Sets the obj par father id.
	 *
	 * @param objParFatherId
	 *            the new obj par father id
	 */
	public void setObjParFatherId(Integer objParFatherId) {
		this.objParFatherId = objParFatherId;
	}

	/**
	 * Gets the obj par id.
	 *
	 * @return the obj par id
	 */
	public Integer getObjParId() {
		return objParId;
	}

	/**
	 * Sets the obj par id.
	 *
	 * @param objParId
	 *            the new obj par id
	 */
	public void setObjParId(Integer objParId) {
		this.objParId = objParId;
	}

	/**
	 * Gets the paruse id.
	 *
	 * @return the paruse id
	 */
	public Integer getParuseId() {
		return paruseId;
	}

	/**
	 * Sets the paruse id.
	 *
	 * @param paruseId
	 *            the new paruse id
	 */
	public void setParuseId(Integer paruseId) {
		this.paruseId = paruseId;
	}

	/**
	 * Gets the logic operator.
	 *
	 * @return the logic operator
	 */
	public String getLogicOperator() {
		return logicOperator;
	}

	/**
	 * Sets the logic operator.
	 *
	 * @param logicOperator
	 *            the new logic operator
	 */
	public void setLogicOperator(String logicOperator) {
		this.logicOperator = logicOperator;
	}

	/**
	 * Gets the post condition.
	 *
	 * @return the post condition
	 */
	public String getPostCondition() {
		return postCondition;
	}

	/**
	 * Sets the post condition.
	 *
	 * @param postCondition
	 *            the new post condition
	 */
	public void setPostCondition(String postCondition) {
		this.postCondition = postCondition;
	}

	/**
	 * Gets the pre condition.
	 *
	 * @return the pre condition
	 */
	public String getPreCondition() {
		return preCondition;
	}

	/**
	 * Sets the pre condition.
	 *
	 * @param preCondition
	 *            the new pre condition
	 */
	public void setPreCondition(String preCondition) {
		this.preCondition = preCondition;
	}

	/**
	 * Gets the prog.
	 *
	 * @return the prog
	 */
	public Integer getProg() {
		return prog;
	}

	/**
	 * Sets the prog.
	 *
	 * @param prog
	 *            the new prog
	 */
	public void setProg(Integer prog) {
		this.prog = prog;
	}

}
