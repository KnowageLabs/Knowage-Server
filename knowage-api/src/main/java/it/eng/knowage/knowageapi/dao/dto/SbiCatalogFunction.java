/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.knowageapi.dao.dto;

import java.util.Set;

import javax.annotation.Nullable;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.ParamDef;

import it.eng.knowage.knowageapi.dao.listener.TenantListener;

@Entity
@Table(name = "SBI_CATALOG_FUNCTION")
@EntityListeners(TenantListener.class)
@FilterDef(name = "organization", parameters = {
		@ParamDef(name = "organization", type = "string")
})
@Filter(name = "organization", condition = "organization like :organization")
public class SbiCatalogFunction extends AbstractEntity {

	@Id
	@Column(name = "FUNCTION_UUID", nullable = false)
	@GeneratedValue(generator = "FUNCTION_UUID_GENERATOR")
	@GenericGenerator(name = "FUNCTION_UUID_GENERATOR", strategy = "org.hibernate.id.UUIDGenerator")
	@NotNull
	private String functionId;

	@Column(name = "NAME")
	@Size(max = 100)
	@NotNull
	private String name;

	@Column(name = "DESCRIPTION")
	@Lob
	@Basic(fetch = FetchType.EAGER)
	@NotNull
	private String description;

	@Column(name = "LANGUAGE")
	@Size(max = 100)
	@NotNull
	private String language;

	@Column(name = "OWNER")
	@Size(max = 50)
	@NotNull
	private String owner;

	@Column(name = "KEYWORDS")
	@Size(max = 255)
	@Nullable
	private String keywords;

	@Column(name = "TYPE")
	@Size(max = 50)
	@Nullable
	private String type;

	@Column(name = "LABEL")
	@Size(max = 50)
	@NotNull
	private String label;

	@Column(name = "BENCHMARKS")
	@Size(max = 4000)
	@Nullable
	private String benchmarks;

	@Column(name = "FAMILY")
	@Size(max = 30)
	@Nullable
	private String family;

	@Column(name = "ONLINE_SCRIPT")
	@Size(max = 4000)
	@Nullable
	private String onlineScript;

	@Column(name = "OFFLINE_SCRIPT_TRAIN")
	@Size(max = 4000)
	@Nullable
	private String offlineScriptTrain;

	@Column(name = "OFFLINE_SCRIPT_USE")
	@Size(max = 4000)
	@Nullable
	private String offlineScriptUse;

	@OneToMany(mappedBy = "function", orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@PrimaryKeyJoinColumn(name = "FUNCTION_UUID", referencedColumnName = "FUNCTION_UUID")
	private Set<SbiFunctionInputColumn> inputColumns;

	@OneToMany(mappedBy = "function", orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@PrimaryKeyJoinColumn(name = "FUNCTION_UUID", referencedColumnName = "FUNCTION_UUID")
	private Set<SbiFunctionOutputColumn> outputColumns;

	@OneToMany(mappedBy = "function", orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@PrimaryKeyJoinColumn(name = "FUNCTION_UUID", referencedColumnName = "FUNCTION_UUID")
	private Set<SbiFunctionInputVariable> inputVariables;

//	@OneToMany(cascade = CascadeType.ALL, orphanRemoval=true)
//	@JoinColumn(name = "FUNCTION_UUID")
//	private Set<SbiObjFunction> objFunctions;

	public String getFunctionId() {
		return functionId;
	}

	public void setFunctionId(String functionId) {
		this.functionId = functionId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getBenchmarks() {
		return benchmarks;
	}

	public void setBenchmarks(String benchmarks) {
		this.benchmarks = benchmarks;
	}

	public String getFamily() {
		return family;
	}

	public void setFamily(String family) {
		this.family = family;
	}

	public String getOnlineScript() {
		return onlineScript;
	}

	public void setOnlineScript(String onlineScript) {
		this.onlineScript = onlineScript;
	}

	public String getOfflineScriptTrain() {
		return offlineScriptTrain;
	}

	public void setOfflineScriptTrain(String offlineScriptTrain) {
		this.offlineScriptTrain = offlineScriptTrain;
	}

	public String getOfflineScriptUse() {
		return offlineScriptUse;
	}

	public void setOfflineScriptUse(String offlineScriptUse) {
		this.offlineScriptUse = offlineScriptUse;
	}

	public Set<SbiFunctionInputColumn> getInputColumns() {
		return inputColumns;
	}

	public void setInputColumns(Set<SbiFunctionInputColumn> inputColumns) {
		this.inputColumns = inputColumns;
	}

	public Set<SbiFunctionOutputColumn> getOutputColumns() {
		return outputColumns;
	}

	public void setOutputColumns(Set<SbiFunctionOutputColumn> outputColumns) {
		this.outputColumns = outputColumns;
	}

	public Set<SbiFunctionInputVariable> getInputVariables() {
		return inputVariables;
	}

	public void setInputVariables(Set<SbiFunctionInputVariable> inputVariables) {
		this.inputVariables = inputVariables;
	}

}
