package it.eng.spagobi.kpi.bo;

import it.eng.spagobi.commons.bo.Domain;

import java.io.Serializable;
import java.util.Date;

public class RuleOutput implements Serializable {

	private static final long serialVersionUID = 6239303686402587566L;
	/**
	 * 
	 */
	private Integer id;
	private String alias;
	private Integer aliasId;
	private String rule;
	private Integer ruleId;
	/**
	 * domainCd="KPI_MEASURE_CATEGORY"
	 */
	private Domain category;
	/**
	 * domainCd="TEMPORAL_LEVEL"
	 */
	private Domain hierarchy;
	/**
	 * domainCd="KPI_RULEOUTPUT_TYPE"
	 */
	private Domain type;
	/**
	 * Rule author
	 */
	private String author;
	/**
	 * Rule dateCreation
	 */
	private Date dateCreation;

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
	 * @return the alias
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * @param alias
	 *            the alias to set
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	/**
	 * @return the aliasId
	 */
	public Integer getAliasId() {
		return aliasId;
	}

	/**
	 * @param aliasId
	 *            the aliasId to set
	 */
	public void setAliasId(Integer aliasId) {
		this.aliasId = aliasId;
	}

	/**
	 * @return the rule
	 */
	public String getRule() {
		return rule;
	}

	/**
	 * @param rule
	 *            the rule to set
	 */
	public void setRule(String rule) {
		this.rule = rule;
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
	 * @return the dateCreation
	 */
	public Date getDateCreation() {
		return dateCreation;
	}

	/**
	 * @param dateCreation
	 *            the dateCreation to set
	 */
	public void setDateCreation(Date dateCreation) {
		this.dateCreation = dateCreation;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @param author
	 *            the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @return the category
	 */
	public Domain getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(Domain category) {
		this.category = category;
	}

	/**
	 * @return the hierarchy
	 */
	public Domain getHierarchy() {
		return hierarchy;
	}

	/**
	 * @param hierarchy
	 *            the hierarchy to set
	 */
	public void setHierarchy(Domain hierarchy) {
		this.hierarchy = hierarchy;
	}

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

	@Override
	public int hashCode() {
		return id == null ? super.hashCode() : id.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof RuleOutput && id != null && id.equals(((RuleOutput) o).getId());
	}
}
