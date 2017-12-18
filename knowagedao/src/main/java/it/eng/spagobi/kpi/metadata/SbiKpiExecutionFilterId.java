package it.eng.spagobi.kpi.metadata;

import java.io.Serializable;

public class SbiKpiExecutionFilterId implements Serializable {

	private Integer placeholderId;
	private Integer executionId;
	private Integer kpiId;
	private Integer kpiVersion;

	public SbiKpiExecutionFilterId() {
	}

	public SbiKpiExecutionFilterId(Integer placeholderId, Integer executionId, Integer kpiId, Integer kpiVersion) {
		this.placeholderId = placeholderId;
		this.executionId = executionId;
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
		result = prime * result + ((placeholderId == null) ? 0 : placeholderId.hashCode());
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
		SbiKpiExecutionFilterId other = (SbiKpiExecutionFilterId) obj;
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
		if (placeholderId == null) {
			if (other.placeholderId != null)
				return false;
		} else if (!placeholderId.equals(other.placeholderId))
			return false;
		return true;
	}

}
