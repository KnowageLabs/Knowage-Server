package it.eng.spagobi.kpi.bo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Cardinality implements Serializable {

	private Integer ruleId;
	private int ruleVersion;
	private String ruleName;
	private String measureName;
	private Map<String, Boolean> attributes = new HashMap<String, Boolean>();

	public Cardinality() {
	}

	public Cardinality(String name) {
		measureName = name;
	}

	/**
	 * @return the ruleId
	 */
	public Integer getRuleId() {
		return ruleId;
	}

	/**
	 * @param ruleId
	 *            the ruleId to set
	 */
	public void setRuleId(Integer ruleId) {
		this.ruleId = ruleId;
	}

	/**
	 * @return the ruleName
	 */
	public String getRuleName() {
		return ruleName;
	}

	/**
	 * @param ruleName
	 *            the ruleName to set
	 */
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	/**
	 * @return the measureName
	 */
	public String getMeasureName() {
		return measureName;
	}

	/**
	 * @param measureName
	 *            the measureName to set
	 */
	public void setMeasureName(String measureName) {
		this.measureName = measureName;
	}

	/**
	 * @return the attributes
	 */
	public Map<String, Boolean> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes
	 *            the attributes to set
	 */
	public void setAttributes(Map<String, Boolean> attributes) {
		this.attributes = attributes;
	}

	/**
	 * @return the ruleVersion
	 */
	public int getRuleVersion() {
		return ruleVersion;
	}

	/**
	 * @param ruleVersion
	 *            the ruleVersion to set
	 */
	public void setRuleVersion(int ruleVersion) {
		this.ruleVersion = ruleVersion;
	}

	@Override
	public int hashCode() {
		return measureName != null ? measureName.hashCode() : super.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Cardinality && measureName != null ? measureName.equals(((Cardinality) o).getMeasureName()) : super.equals(o);
	}
}