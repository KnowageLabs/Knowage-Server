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
package it.eng.spagobi.tools.glossary.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiGlTable extends SbiHibernateModel {

	private static final long serialVersionUID = 5640081056393048360L;


	private Integer tableId;

	private String label;
	

	public SbiGlTable() {
	}


	/**
	 * @param tableId
	 * @param label
	 */
	public SbiGlTable(Integer tableId, String label) {
		super();
		this.tableId = tableId;
		this.label = label;
	}


	


	/**
	 * @return the tableId
	 */
	public Integer getTableId() {
		return tableId;
	}


	/**
	 * @param tableId the tableId to set
	 */
	public void setTableId(Integer tableId) {
		this.tableId = tableId;
	}


	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}


	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	

	
}
