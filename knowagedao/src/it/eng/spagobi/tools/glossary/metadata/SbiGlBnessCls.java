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

public class SbiGlBnessCls extends SbiHibernateModel {

	private static final long serialVersionUID = 5640081056393048360L;

	private Integer bcId;

	private String datamart;

	private String unique_identifier;

	public SbiGlBnessCls() {
	}

	/**
	 * @param bcId
	 * @param label
	 */
	public SbiGlBnessCls(Integer bcId, String unique_identifier, String datamart) {
		super();
		this.bcId = bcId;
		this.unique_identifier = unique_identifier;
		this.datamart = datamart;
	}

	/**
	 * @return the bcId
	 */
	public Integer getBcId() {
		return bcId;
	}

	/**
	 * @param bcId
	 *            the bcId to set
	 */
	public void setBcId(Integer bcId) {
		this.bcId = bcId;
	}

	/**
	 * @return the unique_identifier
	 */
	public String getUnique_identifier() {
		return unique_identifier;
	}

	/**
	 * @param unique_identifier
	 *            the unique_identifier to set
	 */
	public void setUnique_identifier(String unique_identifier) {
		this.unique_identifier = unique_identifier;
	}

	/**
	 * @return the datamart
	 */
	public String getDatamart() {
		return datamart;
	}

	/**
	 * @param datamart
	 *            the datamart to set
	 */
	public void setDatamart(String datamart) {
		this.datamart = datamart;
	}

}
