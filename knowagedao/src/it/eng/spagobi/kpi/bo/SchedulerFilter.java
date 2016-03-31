package it.eng.spagobi.kpi.bo;

import it.eng.spagobi.commons.bo.Domain;

public class SchedulerFilter {

	private Integer executionId;
	private Integer placeholderId;
	private String placeholderName;
	private String kpiName;
	private Domain type;
	private String value;

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

}
