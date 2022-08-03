package it.eng.spagobi.kpi.metadata;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import it.eng.spagobi.commons.dao.dto.SbiCategory;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiKpiTarget extends SbiHibernateModel {
	private Integer targetId;
	private String name;
	private SbiCategory category;
	private Date startValidity;
	private Date endValidity;

	private Set<SbiKpiTargetValue> sbiKpiTargetValues = new HashSet<>(0);

	/**
	 * @return the targetId
	 */
	public Integer getTargetId() {
		return targetId;
	}

	/**
	 * @param targetId the targetId to set
	 */
	public void setTargetId(Integer targetId) {
		this.targetId = targetId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the category
	 */
	public SbiCategory getCategory() {
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(SbiCategory category) {
		this.category = category;
	}

	/**
	 * @return the startValidity
	 */
	public Date getStartValidity() {
		return startValidity;
	}

	/**
	 * @param startValidity the startValidity to set
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
	 * @param endValidity the endValidity to set
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
	 * @param sbiKpiTargetValues the sbiKpiTargetValues to set
	 */
	public void setSbiKpiTargetValues(Set<SbiKpiTargetValue> sbiKpiTargetValues) {
		this.sbiKpiTargetValues = sbiKpiTargetValues;
	}

	@Override
	public int hashCode() {
		return targetId != null ? targetId.hashCode() : super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof SbiKpiTarget && targetId != null && targetId.equals(((SbiKpiTarget) obj).getTargetId()) || super.equals(obj);
	}
}
