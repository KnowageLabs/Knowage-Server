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
import java.util.TreeSet;

import javax.annotation.Nullable;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.ParamDef;

/**
 * @author Marco Libanori
 */
@Entity
@Table(name = "SBI_CATALOG_FUNCTION")
@FilterDef(name = "organization", parameters = {
		@ParamDef(name = "organization", type = "string")
})
@Filter(name = "organization", condition = "organization like :organization")
@NamedQueries({
	@NamedQuery(name = "SbiCatalogFunction.delete", query = "DELETE FROM SbiCatalogFunction q WHERE q.functionId = :functionId")
})
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
	private Set<SbiFunctionInputColumn> inputColumns = new TreeSet<>();

	@OneToMany(mappedBy = "function", orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@PrimaryKeyJoinColumn(name = "FUNCTION_UUID", referencedColumnName = "FUNCTION_UUID")
	private Set<SbiFunctionOutputColumn> outputColumns = new TreeSet<>();

	@OneToMany(mappedBy = "function", orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@PrimaryKeyJoinColumn(name = "FUNCTION_UUID", referencedColumnName = "FUNCTION_UUID")
	private Set<SbiFunctionInputVariable> inputVariables = new TreeSet<>();

	@OneToMany(mappedBy = "function", orphanRemoval = false, fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
	@PrimaryKeyJoinColumn(name = "FUNCTION_UUID", referencedColumnName = "FUNCTION_UUID")
	private Set<SbiObjFunction> objFunctions = new TreeSet<>();

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

	public Set<SbiObjFunction> getObjFunctions() {
		return objFunctions;
	}

	public void setObjFunctions(Set<SbiObjFunction> objFunctions) {
		this.objFunctions = objFunctions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((benchmarks == null) ? 0 : benchmarks.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((family == null) ? 0 : family.hashCode());
		result = prime * result + ((functionId == null) ? 0 : functionId.hashCode());
		result = prime * result + ((inputColumns == null) ? 0 : inputColumns.hashCode());
		result = prime * result + ((inputVariables == null) ? 0 : inputVariables.hashCode());
		result = prime * result + ((keywords == null) ? 0 : keywords.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((language == null) ? 0 : language.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((objFunctions == null) ? 0 : objFunctions.hashCode());
		result = prime * result + ((offlineScriptTrain == null) ? 0 : offlineScriptTrain.hashCode());
		result = prime * result + ((offlineScriptUse == null) ? 0 : offlineScriptUse.hashCode());
		result = prime * result + ((onlineScript == null) ? 0 : onlineScript.hashCode());
		result = prime * result + ((outputColumns == null) ? 0 : outputColumns.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SbiCatalogFunction other = (SbiCatalogFunction) obj;
		if (benchmarks == null) {
			if (other.benchmarks != null)
				return false;
		} else if (!benchmarks.equals(other.benchmarks))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (family == null) {
			if (other.family != null)
				return false;
		} else if (!family.equals(other.family))
			return false;
		if (functionId == null) {
			if (other.functionId != null)
				return false;
		} else if (!functionId.equals(other.functionId))
			return false;
		if (inputColumns == null) {
			if (other.inputColumns != null)
				return false;
		} else if (!inputColumns.equals(other.inputColumns))
			return false;
		if (inputVariables == null) {
			if (other.inputVariables != null)
				return false;
		} else if (!inputVariables.equals(other.inputVariables))
			return false;
		if (keywords == null) {
			if (other.keywords != null)
				return false;
		} else if (!keywords.equals(other.keywords))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (language == null) {
			if (other.language != null)
				return false;
		} else if (!language.equals(other.language))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (objFunctions == null) {
			if (other.objFunctions != null)
				return false;
		} else if (!objFunctions.equals(other.objFunctions))
			return false;
		if (offlineScriptTrain == null) {
			if (other.offlineScriptTrain != null)
				return false;
		} else if (!offlineScriptTrain.equals(other.offlineScriptTrain))
			return false;
		if (offlineScriptUse == null) {
			if (other.offlineScriptUse != null)
				return false;
		} else if (!offlineScriptUse.equals(other.offlineScriptUse))
			return false;
		if (onlineScript == null) {
			if (other.onlineScript != null)
				return false;
		} else if (!onlineScript.equals(other.onlineScript))
			return false;
		if (outputColumns == null) {
			if (other.outputColumns != null)
				return false;
		} else if (!outputColumns.equals(other.outputColumns))
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SbiCatalogFunction [functionId=" + functionId + ", name=" + name + ", description=" + description + ", language=" + language + ", owner="
				+ owner + ", keywords=" + keywords + ", type=" + type + ", label=" + label + ", benchmarks=" + benchmarks + ", family=" + family
				+ ", onlineScript=" + onlineScript + ", offlineScriptTrain=" + offlineScriptTrain + ", offlineScriptUse=" + offlineScriptUse + ", inputColumns="
				+ inputColumns + ", outputColumns=" + outputColumns + ", inputVariables=" + inputVariables + ", objFunctions=" + objFunctions + "]";
	}

}
