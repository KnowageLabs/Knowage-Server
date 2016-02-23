package it.eng.spagobi.kpi.bo;

import java.math.BigDecimal;

public class ThresholdValue {

	private Integer id;
	private int position;
	private String label;
	private String color;
	private Integer severityId;
	private String severity;

	private BigDecimal minValue;
	private boolean includeMin;
	private BigDecimal maxValue;
	private boolean includeMax;

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the color
	 */
	public String getColor() {
		return color;
	}

	/**
	 * @param color
	 *            the color to set
	 */
	public void setColor(String color) {
		this.color = color;
	}

	/**
	 * @return the severityId
	 */
	public Integer getSeverityId() {
		return severityId;
	}

	/**
	 * @param severityId
	 *            the severityId to set
	 */
	public void setSeverityId(Integer severityId) {
		this.severityId = severityId;
	}

	/**
	 * @return the severity
	 */
	public String getSeverity() {
		return severity;
	}

	/**
	 * @param severity
	 *            the severity to set
	 */
	public void setSeverity(String severity) {
		this.severity = severity;
	}

	/**
	 * @return the minValue
	 */
	public BigDecimal getMinValue() {
		return minValue;
	}

	/**
	 * @param minValue
	 *            the minValue to set
	 */
	public void setMinValue(BigDecimal minValue) {
		this.minValue = minValue;
	}

	/**
	 * @return the includeMin
	 */
	public boolean isIncludeMin() {
		return includeMin;
	}

	/**
	 * @param includeMin
	 *            the includeMin to set
	 */
	public void setIncludeMin(boolean includeMin) {
		this.includeMin = includeMin;
	}

	/**
	 * @return the maxValue
	 */
	public BigDecimal getMaxValue() {
		return maxValue;
	}

	/**
	 * @param maxValue
	 *            the maxValue to set
	 */
	public void setMaxValue(BigDecimal maxValue) {
		this.maxValue = maxValue;
	}

	/**
	 * @return the includeMax
	 */
	public boolean isIncludeMax() {
		return includeMax;
	}

	/**
	 * @param includeMax
	 *            the includeMax to set
	 */
	public void setIncludeMax(boolean includeMax) {
		this.includeMax = includeMax;
	}

}
