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

public class SbiGlGlossary extends SbiHibernateModel {

	private static final long serialVersionUID = -3797430382029205179L;

	private Integer glossaryId;

	private String glossaryCd;
	private String glossaryNm;
	private String glossaryDs;

	public SbiGlGlossary() {
	}

	/**
	 * @param glossaryId
	 * @param glossaryCd
	 * @param glossaryNm
	 * @param glossaryDs
	 */
	public SbiGlGlossary(Integer glossaryId, String glossaryCd, String glossaryNm, String glossaryDs) {
		super();
		this.glossaryId = glossaryId;
		this.glossaryCd = glossaryCd;
		this.glossaryNm = glossaryNm;
		this.glossaryDs = glossaryDs;
	}

	/**
	 * @return the glossaryId
	 */
	public Integer getGlossaryId() {
		return glossaryId;
	}

	/**
	 * @param glossaryId
	 *            the glossaryId to set
	 */
	public void setGlossaryId(Integer glossaryId) {
		this.glossaryId = glossaryId;
	}

	/**
	 * @return the glossaryCd
	 */
	public String getGlossaryCd() {
		return glossaryCd;
	}

	/**
	 * @param glossaryCd
	 *            the glossaryCd to set
	 */
	public void setGlossaryCd(String glossaryCd) {
		this.glossaryCd = glossaryCd;
	}

	/**
	 * @return the glossaryNm
	 */
	public String getGlossaryNm() {
		return glossaryNm;
	}

	/**
	 * @param glossaryNm
	 *            the glossaryNm to set
	 */
	public void setGlossaryNm(String glossaryNm) {
		this.glossaryNm = glossaryNm;
	}

	/**
	 * @return the glossaryDs
	 */
	public String getGlossaryDs() {
		return glossaryDs;
	}

	/**
	 * @param glossaryDs
	 *            the glossaryDs to set
	 */
	public void setGlossaryDs(String glossaryDs) {
		this.glossaryDs = glossaryDs;
	}

}
