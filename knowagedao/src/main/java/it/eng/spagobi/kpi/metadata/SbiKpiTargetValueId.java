package it.eng.spagobi.kpi.metadata;

import java.io.Serializable;

public class SbiKpiTargetValueId implements Serializable {

	private Integer targetId;
	private Integer kpiId;
	private Integer kpiVersion;

	public SbiKpiTargetValueId() {
	}

	public SbiKpiTargetValueId(Integer targetId, Integer kpiId, Integer kpiVersion) {
		this.kpiVersion = kpiVersion;
		this.kpiId = kpiId;
		this.targetId = targetId;
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
	 * @return the targetId
	 */
	public Integer getTargetId() {
		return targetId;
	}

	/**
	 * @param targetId
	 *            the targetId to set
	 */
	public void setTargetId(Integer targetId) {
		this.targetId = targetId;
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
		result = prime * result + ((kpiId == null) ? 0 : kpiId.hashCode());
		result = prime * result + ((kpiVersion == null) ? 0 : kpiVersion.hashCode());
		result = prime * result + ((targetId == null) ? 0 : targetId.hashCode());
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
		SbiKpiTargetValueId other = (SbiKpiTargetValueId) obj;
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
		if (targetId == null) {
			if (other.targetId != null)
				return false;
		} else if (!targetId.equals(other.targetId))
			return false;
		return true;
	}

}
