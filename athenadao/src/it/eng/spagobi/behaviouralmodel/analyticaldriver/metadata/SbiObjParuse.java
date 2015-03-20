/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiObjParuse extends SbiHibernateModel {


	private SbiObjParuseId id;
	private Integer prog;
	private String filterColumn;
	private String preCondition;
	private String postCondition;
	private String logicOperator;

	// Constructors

	/**
	 * default constructor.
	 */
	public SbiObjParuse() {
	}

	/**
	 * constructor with id.
	 * 
	 * @param id the id
	 */
	public SbiObjParuse(SbiObjParuseId id) {
		this.id = id;
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
	 * @param filterColumn the new filter column
	 */
	public void setFilterColumn(String filterColumn) {
		this.filterColumn = filterColumn;
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
	 * @param logicOperator the new logic operator
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
	 * @param postCondition the new post condition
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
	 * @param preCondition the new pre condition
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
	 * @param prog the new prog
	 */
	public void setProg(Integer prog) {
		this.prog = prog;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public SbiObjParuseId getId() {
		return id;
	}

	/**
	 * Sets the id.
	 * 
	 * @param objparuseId the new id
	 */
	public void setId(SbiObjParuseId objparuseId) {
		this.id = objparuseId;
	}



}
