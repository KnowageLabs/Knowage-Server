package it.eng.spagobi.kpi.bo;

public class Placeholder extends Alias {

	private Integer ruleId;

	public Placeholder() {
	}

	public Placeholder(Integer id, String name) {
		super(id, name);
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

}
