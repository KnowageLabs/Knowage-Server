package it.eng.spagobi.kpi.metadata;

import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class SbiKpiTarget extends SbiHibernateModel {
	private int targetId;
	private String name;
	private SbiDomains category;
	private Date startValidity;
	private Date endValidity;

	private Set<SbiKpiTargetValue> sbiKpiTargetValues = new HashSet<>(0);

	/**
	 * @return the targetId
	 */
	public int getTargetId() {
		return targetId;
	}

	/**
	 * @param targetId
	 *            the targetId to set
	 */
	public void setTargetId(int targetId) {
		this.targetId = targetId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the category
	 */
	public SbiDomains getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(SbiDomains category) {
		this.category = category;
	}

	/**
	 * @return the startValidity
	 */
	public Date getStartValidity() {
		return startValidity;
	}

	/**
	 * @param startValidity
	 *            the startValidity to set
	 */
	public void setStartValidity(Date startValidity) {
		this.startValidity = startValidity;
	}

	/**
	 * @return the endValidity
	 */
	public Date getEndValidity() {
		return endValidity;
	}

	/**
	 * @param endValidity
	 *            the endValidity to set
	 */
	public void setEndValidity(Date endValidity) {
		this.endValidity = endValidity;
	}

	/**
	 * @return the sbiKpiTargetValues
	 */
	public Set<SbiKpiTargetValue> getSbiKpiTargetValues() {
		return sbiKpiTargetValues;
	}

	/**
	 * @param sbiKpiTargetValues
	 *            the sbiKpiTargetValues to set
	 */
	public void setSbiKpiTargetValues(Set<SbiKpiTargetValue> sbiKpiTargetValues) {
		this.sbiKpiTargetValues = sbiKpiTargetValues;
	}

}
