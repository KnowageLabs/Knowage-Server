package it.eng.spagobi.kpi.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiKpiTargetValue extends SbiHibernateModel {

	private SbiKpiTargetValueId sbiKpiTargetValueId = new SbiKpiTargetValueId();
	private SbiKpiTarget sbiKpiTarget;
	private SbiKpiKpi sbiKpiKpi;

	private Double value;

	/**
	 * @return the sbiKpiTarget
	 */
	public SbiKpiTarget getSbiKpiTarget() {
		return sbiKpiTarget;
	}

	/**
	 * @param sbiKpiTarget
	 *            the sbiKpiTarget to set
	 */
	public void setSbiKpiTarget(SbiKpiTarget sbiKpiTarget) {
		this.sbiKpiTarget = sbiKpiTarget;
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

	/**
	 * @return the value
	 */
	public Double getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(Double value) {
		this.value = value;
	}

	/**
	 * @return the sbiKpiTargetValueId
	 */
	public SbiKpiTargetValueId getSbiKpiTargetValueId() {
		return sbiKpiTargetValueId;
	}

	/**
	 * @param sbiKpiTargetValueId
	 *            the sbiKpiTargetValueId to set
	 */
	public void setSbiKpiTargetValueId(SbiKpiTargetValueId sbiKpiTargetValueId) {
		this.sbiKpiTargetValueId = sbiKpiTargetValueId;
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
		result = prime * result + ((sbiKpiTargetValueId == null) ? 0 : sbiKpiTargetValueId.hashCode());
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
		SbiKpiTargetValue other = (SbiKpiTargetValue) obj;
		if (sbiKpiTargetValueId == null) {
			if (other.sbiKpiTargetValueId != null)
				return false;
		} else if (!sbiKpiTargetValueId.equals(other.sbiKpiTargetValueId))
			return false;
		return true;
	}

}
