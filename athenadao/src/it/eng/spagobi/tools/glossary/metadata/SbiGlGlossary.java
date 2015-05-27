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
