package it.eng.spagobi.tools.crossnavigation.metadata;

import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiOutputParameter extends SbiHibernateModel {

	private static final long serialVersionUID = -5492568111739841068L;
	/**
	 * 
	 */
	private Integer id;
	private Integer biobjId;
	private Integer parameterTypeId;
	private String label;
	private SbiObjects sbiObject;
	private SbiDomains parameterType;

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the biobjId
	 */
	public Integer getBiobjId() {
		return biobjId;
	}

	/**
	 * @param biobjId
	 *            the biobjId to set
	 */
	public void setBiobjId(Integer biobjId) {
		this.biobjId = biobjId;
	}

	/**
	 * @return the biobj
	 */
	public SbiObjects getSbiObject() {
		return sbiObject;
	}

	/**
	 * @param biobj
	 *            the biobj to set
	 */
	public void setSbiObject(SbiObjects sbiObject) {
		this.sbiObject = sbiObject;
	}

	/**
	 * @return the parameterTypeId
	 */
	public Integer getParameterTypeId() {
		return parameterTypeId;
	}

	/**
	 * @param parameterTypeId
	 *            the parameterTypeId to set
	 */
	public void setParameterTypeId(Integer parameterTypeId) {
		this.parameterTypeId = parameterTypeId;
	}

	/**
	 * @return the parameterType
	 */
	public SbiDomains getParameterType() {
		return parameterType;
	}

	/**
	 * @param parameterType
	 *            the parameterType to set
	 */
	public void setParameterType(SbiDomains parameterType) {
		this.parameterType = parameterType;
	}

}
