package it.eng.spagobi.kpi.bo;

import it.eng.spagobi.commons.bo.Domain;

import java.io.Serializable;

public class SchedulerFilter implements Serializable {

	private Integer executionId;
	private String placeholderName;
	private Integer placeholderId;
	private String kpiName;
	private Integer kpiId;
	private Integer kpiVersion;
	/**
	 * domainCd = KPI_PLACEHOLDER_TYPE
	 */
	private Domain type;
	private String value;

	public SchedulerFilter() {
	}

	public SchedulerFilter(Integer executionId, String placeholderName, Integer kpiId, Integer kpiVersion) {
		this.executionId = executionId;
		this.placeholderName = placeholderName;
		this.kpiId = kpiId;
		this.kpiVersion = kpiVersion;
	}

	/**
	 * @return the placeholderId
	 */
	public Integer getPlaceholderId() {
		return placeholderId;
	}

	/**
	 * @param placeholderId
	 *            the placeholderId to set
	 */
	public void setPlaceholderId(Integer placeholderId) {
		this.placeholderId = placeholderId;
	}

	/**
	 * @return the type
	 */
	public Domain getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(Domain type) {
		this.type = type;
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
	 * @return the executionId
	 */
	public Integer getExecutionId() {
		return executionId;
	}

	/**
	 * @param executionId
	 *            the executionId to set
	 */
	public void setExecutionId(Integer executionId) {
		this.executionId = executionId;
	}

	/**
	 * @return the placeholderName
	 */
	public String getPlaceholderName() {
		return placeholderName;
	}

	/**
	 * @param placeholderName
	 *            the placeholderName to set
	 */
	public void setPlaceholderName(String placeholderName) {
		this.placeholderName = placeholderName;
	}

	/**
	 * @return the kpiName
	 */
	public String getKpiName() {
		return kpiName;
	}

	/**
	 * @param kpiName
	 *            the kpiName to set
	 */
	public void setKpiName(String kpiName) {
		this.kpiName = kpiName;
	}

	/**
	 * @return the kpiId
	 */
	public Integer getKpiId() {
		return kpiId;
	}

	/**
	 * @param kpiId
	 *            the kpiId to set
	 */
	public void setKpiId(Integer kpiId) {
		this.kpiId = kpiId;
	}

	/**
	 * @return the kpiVersion
	 */
	public Integer getKpiVersion() {
		return kpiVersion;
	}

	/**
	 * @param kpiVersion
	 *            the kpiVersion to set
	 */
	public void setKpiVersion(Integer kpiVersion) {
		this.kpiVersion = kpiVersion;
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
		result = prime * result + ((executionId == null) ? 0 : executionId.hashCode());
		result = prime * result + ((kpiId == null) ? 0 : kpiId.hashCode());
		result = prime * result + ((kpiVersion == null) ? 0 : kpiVersion.hashCode());
		result = prime * result + ((placeholderName == null) ? 0 : placeholderName.hashCode());
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
		SchedulerFilter other = (SchedulerFilter) obj;
		if (executionId == null) {
			if (other.executionId != null)
				return false;
		} else if (!executionId.equals(other.executionId))
			return false;
		if (kpiId == null) {
			if (other.kpiId != null)
				return false;
		} else if (!kpiId.equals(other.kpiId))
			return false;
		if (kpiVersion == null) {
			if (other.kpiVersion != null)
				return false;
		} else if (!kpiVersion.equals(other.kpiVersion))
			return false;
		if (placeholderName == null) {
			if (other.placeholderName != null)
				return false;
		} else if (!placeholderName.equals(other.placeholderName))
			return false;
		return true;
	}

}
