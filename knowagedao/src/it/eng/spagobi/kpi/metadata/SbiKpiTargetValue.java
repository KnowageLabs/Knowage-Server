package it.eng.spagobi.kpi.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiKpiTargetValue extends SbiHibernateModel {

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

}
