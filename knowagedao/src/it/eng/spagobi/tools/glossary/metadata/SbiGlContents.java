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

public class SbiGlContents extends SbiHibernateModel {

	private static final long serialVersionUID = 1090211665647922609L;

	private Integer contentId;
	private String contentCd;
	private String contentNm;
	private String contentDs;

	private Integer depth;

	private SbiGlGlossary glossary;
	private SbiGlContents parent;

	private Integer parentId;
	private Integer glossaryId;


	public SbiGlContents() {
		// TODO Auto-generated constructor stub
	}

	public SbiGlContents(Integer contentId) {
		super();
		this.contentId = contentId;
	}

	/**
	 * @param contentId
	 * @param contentCd
	 * @param contentNm
	 * @param contentDs
	 * @param depth
	 * @param glossary
	 * @param parent
	 * @param sequence
	 */
	public SbiGlContents(Integer contentId, String contentCd, String contentNm, String contentDs, Integer depth, SbiGlGlossary glossary, SbiGlContents parent) {
		super();
		this.contentId = contentId;
		this.contentCd = contentCd;
		this.contentNm = contentNm;
		this.contentDs = contentDs;
		this.depth = depth;
		this.glossary = glossary;
		this.parent = parent;
	}

	/**
	 * @return the contentId
	 */
	public Integer getContentId() {
		return contentId;
	}

	/**
	 * @param contentId
	 *            the contentId to set
	 */
	public void setContentId(Integer contentId) {
		this.contentId = contentId;
	}

	/**
	 * @return the contentCd
	 */
	public String getContentCd() {
		return contentCd;
	}

	/**
	 * @param contentCd
	 *            the contentCd to set
	 */
	public void setContentCd(String contentCd) {
		this.contentCd = contentCd;
	}

	/**
	 * @return the contentNm
	 */
	public String getContentNm() {
		return contentNm;
	}

	/**
	 * @param contentNm
	 *            the contentNm to set
	 */
	public void setContentNm(String contentNm) {
		this.contentNm = contentNm;
	}

	/**
	 * @return the contentDs
	 */
	public String getContentDs() {
		return contentDs;
	}

	/**
	 * @param contentDs
	 *            the contentDs to set
	 */
	public void setContentDs(String contentDs) {
		this.contentDs = contentDs;
	}

	/**
	 * @return the depth
	 */
	public Integer getDepth() {
		return depth;
	}

	/**
	 * @param depth
	 *            the depth to set
	 */
	public void setDepth(Integer depth) {
		this.depth = depth;
	}

	/**
	 * @return the glossary
	 */
	public SbiGlGlossary getGlossary() {
		return glossary;
	}

	/**
	 * @param glossary
	 *            the glossary to set
	 */
	public void setGlossary(SbiGlGlossary glossary) {
		this.glossary = glossary;
	}

	/**
	 * @return the parent
	 */
	public SbiGlContents getParent() {
		return parent;
	}

	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(SbiGlContents parent) {
		this.parent = parent;
	}




	/**
	 * @return the parentId
	 */
	public Integer getParentId() {
		return parentId;
	}

	/**
	 * @param parentId
	 *            the parentId to set
	 */
	public void setParentId(Integer parentId) {
		this.parentId = parentId;
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

	@Override
	public String toString() {
		return "SbiGlContents [contentId=" + contentId + ", contentCd="
				+ contentCd + ", contentNm=" + contentNm + ", contentDs="
				+ contentDs + ", depth=" + depth + "]";
	}
}
