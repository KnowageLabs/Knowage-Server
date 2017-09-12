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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Rule implements Serializable {

	private static final long serialVersionUID = -9167429953532804049L;
	/**
	 * 
	 */
	private Integer id;
	private Integer version;
	private String name;
	private String definition;
	private Integer dataSourceId;

	private List<RuleOutput> ruleOutputs = new ArrayList<>();
	private List<Placeholder> placeholders = new ArrayList<>();

	public Rule() {
	}

	public Rule(Integer id, Integer version) {
		this.id = id;
		this.version = version;
	}

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
	 * @return the version
	 */
	public Integer getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(Integer version) {
		this.version = version;
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
	 * @return the dataSourceId
	 */
	public Integer getDataSourceId() {
		return dataSourceId;
	}

	/**
	 * @param dataSourceId
	 *            the dataSourceId to set
	 */
	public void setDataSourceId(Integer dataSourceId) {
		this.dataSourceId = dataSourceId;
	}

	/**
	 * @return the placeholders
	 */
	public List<Placeholder> getPlaceholders() {
		return placeholders;
	}

	/**
	 * @param placeholders
	 *            the placeholders to set
	 */
	public void setPlaceholders(List<Placeholder> placeholders) {
		this.placeholders = placeholders;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataSourceId == null) ? 0 : dataSourceId.hashCode());
		result = prime * result + ((definition == null) ? 0 : definition.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((placeholders == null) ? 0 : placeholders.hashCode());
		result = prime * result + ((ruleOutputs == null) ? 0 : ruleOutputs.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Rule other = (Rule) obj;
		if (dataSourceId == null) {
			if (other.dataSourceId != null)
				return false;
		} else if (!dataSourceId.equals(other.dataSourceId))
			return false;
		if (definition == null) {
			if (other.definition != null)
				return false;
		} else if (!definition.equals(other.definition))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (placeholders == null) {
			if (other.placeholders != null)
				return false;
		} else if (!placeholders.equals(other.placeholders))
			return false;
		if (ruleOutputs == null) {
			if (other.ruleOutputs != null)
				return false;
		} else if (!ruleOutputs.equals(other.ruleOutputs))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

}
