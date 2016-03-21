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

	private boolean enableVersioning;

	private List<RuleOutput> ruleOutputs = new ArrayList<>();
	private List<Placeholder> placeholders = new ArrayList<>();

	public Rule() {
	}

	public Rule(Integer id) {
		this.id = id;
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

	/**
	 * @return the enableVersioning
	 */
	public boolean isEnableVersioning() {
		return enableVersioning;
	}

	/**
	 * @param enableVersioning
	 *            the enableVersioning to set
	 */
	public void setEnableVersioning(boolean enableVersioning) {
		this.enableVersioning = enableVersioning;
	}

	@Override
	public int hashCode() {
		return id == null ? super.hashCode() : id.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Rule && id != null && id.equals(((Rule) o).getId());
	}
}
