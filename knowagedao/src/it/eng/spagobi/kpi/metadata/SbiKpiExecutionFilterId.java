package it.eng.spagobi.kpi.metadata;

import java.io.Serializable;

public class SbiKpiExecutionFilterId implements Serializable {

	private Integer placeholderId;
	private Integer executionId;

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
		if (placeholderId == null) {
			if (other.placeholderId != null)
				return false;
		} else if (!placeholderId.equals(other.placeholderId))
			return false;
		return true;
	}

}
