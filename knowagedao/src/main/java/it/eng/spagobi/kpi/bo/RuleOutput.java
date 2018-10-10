/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.kpi.bo;

import it.eng.spagobi.commons.bo.Domain;

import java.io.Serializable;
import java.util.Date;

public class RuleOutput implements Serializable, Comparable<RuleOutput> {

	private static final long serialVersionUID = 6239303686402587566L;

	private Integer id;
	private String alias;
	private Integer aliasId;
	private String rule;
	private Integer ruleId;
	private Integer ruleVersion;

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
	
	public RuleOutput() {
	}

	public RuleOutput(Integer id) {
		this.id = id;
	}
	
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

	/**
	 * @return the ruleVersion
	 */
	public Integer getRuleVersion() {
		return ruleVersion;
	}

	/**
	 * @param ruleVersion
	 *            the ruleVersion to set
	 */
	public void setRuleVersion(Integer ruleVersion) {
		this.ruleVersion = ruleVersion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
		result = prime * result + ((aliasId == null) ? 0 : aliasId.hashCode());
		result = prime * result + ((author == null) ? 0 : author.hashCode());
		result = prime * result + ((category == null) ? 0 : category.hashCode());
		result = prime * result + ((dateCreation == null) ? 0 : dateCreation.hashCode());
		result = prime * result + ((hierarchy == null) ? 0 : hierarchy.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((rule == null) ? 0 : rule.hashCode());
		result = prime * result + ((ruleId == null) ? 0 : ruleId.hashCode());
		result = prime * result + ((ruleVersion == null) ? 0 : ruleVersion.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof RuleOutput))
			return false;
		RuleOutput other = (RuleOutput) obj;
		if (alias == null) {
			if (other.alias != null)
				return false;
		} else if (!alias.equals(other.alias))
			return false;
		if (aliasId == null) {
			if (other.aliasId != null)
				return false;
		} else if (!aliasId.equals(other.aliasId))
			return false;
		if (author == null) {
			if (other.author != null)
				return false;
		} else if (!author.equals(other.author))
			return false;
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (dateCreation == null) {
			if (other.dateCreation != null)
				return false;
		} else if (!dateCreation.equals(other.dateCreation))
			return false;
		if (hierarchy == null) {
			if (other.hierarchy != null)
				return false;
		} else if (!hierarchy.equals(other.hierarchy))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (rule == null) {
			if (other.rule != null)
				return false;
		} else if (!rule.equals(other.rule))
			return false;
		if (ruleId == null) {
			if (other.ruleId != null)
				return false;
		} else if (!ruleId.equals(other.ruleId))
			return false;
		if (ruleVersion == null) {
			if (other.ruleVersion != null)
				return false;
		} else if (!ruleVersion.equals(other.ruleVersion))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public int compareTo(RuleOutput rule) {
		return alias.compareToIgnoreCase(rule.alias);
	}
}
