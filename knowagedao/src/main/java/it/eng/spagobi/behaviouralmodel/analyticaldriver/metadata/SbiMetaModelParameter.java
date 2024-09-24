package it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.tools.catalogue.metadata.SbiMetaModel;

public class SbiMetaModelParameter extends SbiHibernateModel {

	// Fields

	private Integer metaModelParId;
	private SbiMetaModel sbiMetaModel;
	private SbiParameters sbiParameter;
	private Short reqFl;
	private Short modFl;
	private Short viewFl;
	private Short multFl;
	private String label;
	private String parurlNm;
	private Integer prog;
	private Integer priority;
	private Integer colSpan;
	private Integer ThickPerc;

	// Constructors

	/**
	 * default constructor.
	 */
	public SbiMetaModelParameter() {
		this.metaModelParId = -1;

	}

	/**
	 * constructor with id.
	 *
	 * @param objParId
	 *            the obj par id
	 */
	public SbiMetaModelParameter(Integer objParId) {
		this.metaModelParId = objParId;
	}

	// Property accessors

	/**
	 * Gets the label.
	 *
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label.
	 *
	 * @param label
	 *            the new label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Gets the mod fl.
	 *
	 * @return the mod fl
	 */
	public Short getModFl() {
		return modFl;
	}

	/**
	 * Sets the mod fl.
	 *
	 * @param modFl
	 *            the new mod fl
	 */
	public void setModFl(Short modFl) {
		this.modFl = modFl;
	}

	/**
	 * Gets the mult fl.
	 *
	 * @return the mult fl
	 */
	public Short getMultFl() {
		return multFl;
	}

	/**
	 * Sets the mult fl.
	 *
	 * @param multFl
	 *            the new mult fl
	 */
	public void setMultFl(Short multFl) {
		this.multFl = multFl;
	}

	/**
	 * Gets the parurl nm.
	 *
	 * @return the parurl nm
	 */
	public String getParurlNm() {
		return parurlNm;
	}

	/**
	 * Sets the parurl nm.
	 *
	 * @param parurlNm
	 *            the new parurl nm
	 */
	public void setParurlNm(String parurlNm) {
		this.parurlNm = parurlNm;
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

	/**
	 * Gets the req fl.
	 *
	 * @return the req fl
	 */
	public Short getReqFl() {
		return reqFl;
	}

	/**
	 * Sets the req fl.
	 *
	 * @param reqFl
	 *            the new req fl
	 */
	public void setReqFl(Short reqFl) {
		this.reqFl = reqFl;
	}

	/**
	 * Gets the sbi parameter.
	 *
	 * @return the sbi parameter
	 */
	public SbiParameters getSbiParameter() {
		return sbiParameter;
	}

	/**
	 * Sets the sbi parameter.
	 *
	 * @param sbiParameter
	 *            the new sbi parameter
	 */
	public void setSbiParameter(SbiParameters sbiParameter) {
		this.sbiParameter = sbiParameter;
	}

	/**
	 * Gets the view fl.
	 *
	 * @return the view fl
	 */
	public Short getViewFl() {
		return viewFl;
	}

	/**
	 * Sets the view fl.
	 *
	 * @param viewFl
	 *            the new view fl
	 */
	public void setViewFl(Short viewFl) {
		this.viewFl = viewFl;
	}

	/**
	 * Gets the priority.
	 *
	 * @return the priority
	 */
	public Integer getPriority() {
		return priority;
	}

	/**
	 * Sets the priority.
	 *
	 * @param priority
	 *            the new priority
	 */
	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Integer getColSpan() {
		return colSpan;
	}

	public void setColSpan(Integer colSpan) {
		this.colSpan = colSpan;
	}

	public Integer getThickPerc() {
		return ThickPerc;
	}

	public void setThickPerc(Integer thickPerc) {
		ThickPerc = thickPerc;
	}

	public SbiMetaModel getSbiMetaModel() {
		return sbiMetaModel;
	}

	public void setSbiMetaModel(SbiMetaModel sbiMetaModel) {
		this.sbiMetaModel = sbiMetaModel;
	}

	public Integer getMetaModelParId() {
		return metaModelParId;
	}

	private void setMetaModelParId(Integer metaModelParId) {
		this.metaModelParId = metaModelParId;
	}

}
