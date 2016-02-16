package it.eng.spagobi.kpi.model.bo;

import java.util.List;

public class Rule {

	private Integer id;
	private String name;
	private String definition;

	private List<RuleOutput> ruleOutputs;

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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the definition
	 */
	public String getDefinition() {
		return definition;
	}

	/**
	 * @param definition
	 *            the definition to set
	 */
	public void setDefinition(String definition) {
		this.definition = definition;
	}

	/**
	 * @return the ruleOutputs
	 */
	public List<RuleOutput> getRuleOutputs() {
		return ruleOutputs;
	}

	/**
	 * @param ruleOutputs
	 *            the ruleOutputs to set
	 */
	public void setRuleOutputs(List<RuleOutput> ruleOutputs) {
		this.ruleOutputs = ruleOutputs;
	}

}
