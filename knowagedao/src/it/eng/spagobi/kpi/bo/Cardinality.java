package it.eng.spagobi.kpi.bo;

import java.util.List;

public class Cardinality {

	private Integer ruleId;
	private String ruleName;
	private String measureName;
	private List<String> attributs;

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
	 * @return the attributs
	 */
	public List<String> getAttributs() {
		return attributs;
	}

	/**
	 * @param attributs
	 *            the attributs to set
	 */
	public void setAttributs(List<String> attributs) {
		this.attributs = attributs;
	}

}
