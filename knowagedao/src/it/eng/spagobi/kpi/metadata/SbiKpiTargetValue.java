package it.eng.spagobi.kpi.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiKpiTargetValue extends SbiHibernateModel {

	private SbiKpiTarget sbiKpiTarget;
	private SbiKpiKpi sbiKpiKpi;

	private Double value;

	private Integer kpiId;
	private Integer kpiVersion;
	private Integer targetId;

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

}
