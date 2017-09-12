package it.eng.spagobi.kpi.bo;

public class TargetValue {

	private Integer kpiId;
	private Integer kpiVersion;
	private Integer targetId;

	private Kpi kpi;

	private Double value;

	public TargetValue() {
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

	/**
	 * @return the kpi
	 */
	public Kpi getKpi() {
		return kpi;
	}

	/**
	 * @param kpi
	 *            the kpi to set
	 */
	public void setKpi(Kpi kpi) {
		this.kpi = kpi;
	}

}
