package it.eng.spagobi.kpi.metadata;

import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiKpiExecutionFilter extends SbiHibernateModel {

	private SbiKpiExecutionFilterId sbiKpiExecutionFilterId = new SbiKpiExecutionFilterId();

	private SbiKpiPlaceholder sbiKpiPlaceholder;
	private SbiKpiExecution sbiKpiExecution;
	private SbiKpiKpi sbiKpiKpi;

	private SbiDomains type;
	private Integer typeId;
	private String value;

	/**
	 * @return the sbiKpiExecutionFilterId
	 */
	public SbiKpiExecutionFilterId getSbiKpiExecutionFilterId() {
		return sbiKpiExecutionFilterId;
	}

	/**
	 * @param sbiKpiExecutionFilterId
	 *            the sbiKpiExecutionFilterId to set
	 */
	public void setSbiKpiExecutionFilterId(SbiKpiExecutionFilterId sbiKpiExecutionFilterId) {
		this.sbiKpiExecutionFilterId = sbiKpiExecutionFilterId;
	}

	/**
	 * @return the sbiKpiPlaceholder
	 */
	public SbiKpiPlaceholder getSbiKpiPlaceholder() {
		return sbiKpiPlaceholder;
	}

	/**
	 * @param sbiKpiPlaceholder
	 *            the sbiKpiPlaceholder to set
	 */
	public void setSbiKpiPlaceholder(SbiKpiPlaceholder sbiKpiPlaceholder) {
		this.sbiKpiPlaceholder = sbiKpiPlaceholder;
	}

	/**
	 * @return the sbiKpiExecution
	 */
	public SbiKpiExecution getSbiKpiExecution() {
		return sbiKpiExecution;
	}

	/**
	 * @param sbiKpiExecution
	 *            the sbiKpiExecution to set
	 */
	public void setSbiKpiExecution(SbiKpiExecution sbiKpiExecution) {
		this.sbiKpiExecution = sbiKpiExecution;
	}

	/**
	 * @return the type
	 */
	public SbiDomains getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(SbiDomains type) {
		this.type = type;
	}

	/**
	 * @return the typeId
	 */
	public Integer getTypeId() {
		return typeId;
	}

	/**
	 * @param typeId
	 *            the typeId to set
	 */
	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the sbiKpiKpi
	 */
	public SbiKpiKpi getSbiKpiKpi() {
		return sbiKpiKpi;
	}

	/**
	 * @param sbiKpiKpi
	 *            the sbiKpiKpi to set
	 */
	public void setSbiKpiKpi(SbiKpiKpi sbiKpiKpi) {
		this.sbiKpiKpi = sbiKpiKpi;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sbiKpiExecutionFilterId == null) ? 0 : sbiKpiExecutionFilterId.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SbiKpiExecutionFilter other = (SbiKpiExecutionFilter) obj;
		if (sbiKpiExecutionFilterId == null) {
			if (other.sbiKpiExecutionFilterId != null)
				return false;
		} else if (!sbiKpiExecutionFilterId.equals(other.sbiKpiExecutionFilterId))
			return false;
		return true;
	}

}
