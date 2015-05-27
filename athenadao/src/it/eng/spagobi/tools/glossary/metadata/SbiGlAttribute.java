package it.eng.spagobi.tools.glossary.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiGlAttribute extends SbiHibernateModel {

	private static final long serialVersionUID = 4603207731778259115L;

	private Integer attributeId;

	private String attributeCd;
	private String attributeNm;
	private String attributeDs;

	private Integer mandatoryFl;

	private String type;
	private String domain;
	private String format;
	private String displayTp;
	private String order;

	public SbiGlAttribute() {
	}

	/**
	 * @param attributeId
	 * @param attributeCd
	 * @param attributeNm
	 * @param attributeDs
	 * @param mandatoryFl
	 * @param type
	 * @param domain
	 * @param format
	 * @param displayTp
	 * @param order
	 */
	public SbiGlAttribute(Integer attributeId, String attributeCd, String attributeNm, String attributeDs, Integer mandatoryFl, String type, String domain,
			String format, String displayTp, String order) {
		super();
		this.attributeId = attributeId;
		this.attributeCd = attributeCd;
		this.attributeNm = attributeNm;
		this.attributeDs = attributeDs;
		this.mandatoryFl = mandatoryFl;
		this.type = type;
		this.domain = domain;
		this.format = format;
		this.displayTp = displayTp;
		this.order = order;
	}

	/**
	 * @return the attributeId
	 */
	public Integer getAttributeId() {
		return attributeId;
	}

	/**
	 * @param attributeId
	 *            the attributeId to set
	 */
	public void setAttributeId(Integer attributeId) {
		this.attributeId = attributeId;
	}

	/**
	 * @return the attributeCd
	 */
	public String getAttributeCd() {
		return attributeCd;
	}

	/**
	 * @param attributeCd
	 *            the attributeCd to set
	 */
	public void setAttributeCd(String attributeCd) {
		this.attributeCd = attributeCd;
	}

	/**
	 * @return the attributeNm
	 */
	public String getAttributeNm() {
		return attributeNm;
	}

	/**
	 * @param attributeNm
	 *            the attributeNm to set
	 */
	public void setAttributeNm(String attributeNm) {
		this.attributeNm = attributeNm;
	}

	/**
	 * @return the attributeDs
	 */
	public String getAttributeDs() {
		return attributeDs;
	}

	/**
	 * @param attributeDs
	 *            the attributeDs to set
	 */
	public void setAttributeDs(String attributeDs) {
		this.attributeDs = attributeDs;
	}

	/**
	 * @return the mandatoryFl
	 */
	public Integer getMandatoryFl() {
		return mandatoryFl;
	}

	/**
	 * @param mandatoryFl
	 *            the mandatoryFl to set
	 */
	public void setMandatoryFl(Integer mandatoryFl) {
		this.mandatoryFl = mandatoryFl;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * @param domain
	 *            the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * @return the format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * @param format
	 *            the format to set
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * @return the displayTp
	 */
	public String getDisplayTp() {
		return displayTp;
	}

	/**
	 * @param displayTp
	 *            the displayTp to set
	 */
	public void setDisplayTp(String displayTp) {
		this.displayTp = displayTp;
	}

	/**
	 * @return the order
	 */
	public String getOrder() {
		return order;
	}

	/**
	 * @param order
	 *            the order to set
	 */
	public void setOrder(String order) {
		this.order = order;
	}

}
