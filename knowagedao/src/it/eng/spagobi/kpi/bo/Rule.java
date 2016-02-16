package it.eng.spagobi.kpi.bo;

import java.io.Serializable;
import java.util.List;

public class Rule implements Serializable {

	private Integer id;
	private String name;
	private String definition;
	private boolean newRecord;

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

	/**
	 * @return the newRecord
	 */
	public boolean isNewRecord() {
		return newRecord;
	}

	/**
	 * @param newRecord
	 *            the newRecord to set
	 */
	public void setNewRecord(boolean newRecord) {
		this.newRecord = newRecord;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Rule && id.equals(((Rule) o).getId());
	}
}
