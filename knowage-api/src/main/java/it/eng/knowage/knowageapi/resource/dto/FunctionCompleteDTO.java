/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.knowage.knowageapi.resource.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Marco Libanori
 */
public class FunctionCompleteDTO {

	private UUID id;
	private String name;
	private String benchmark;
	private String description;
	private String family;
	private final List<FunctionInputColumnDTO> inputColumns = new ArrayList<>();
	private final List<FunctionInputVariableDTO> inputVariables = new ArrayList<>();
	private final List<String> tags = new ArrayList<>();
	private String label;
	private String language;
	private String offlineScriptTrain;
	private String offlineScriptUse;
	private String onlineScript;
	private final List<FunctionOutputColumnDTO> outputColumns = new ArrayList<>();
	private String owner;
	private String type;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBenchmark() {
		return benchmark;
	}

	public void setBenchmark(String benchmark) {
		this.benchmark = benchmark;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFamily() {
		return family;
	}

	public void setFamily(String family) {
		this.family = family;
	}

	public List<FunctionInputColumnDTO> getInputColumns() {
		return inputColumns;
	}

	public List<FunctionInputVariableDTO> getInputVariables() {
		return inputVariables;
	}

	public List<String> getTags() {
		return tags;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
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

	public String getOnlineScript() {
		return onlineScript;
	}

	public void setOnlineScript(String onlineScript) {
		this.onlineScript = onlineScript;
	}

	public List<FunctionOutputColumnDTO> getOutputColumns() {
		return outputColumns;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FunctionCompleteDTO other = (FunctionCompleteDTO) obj;
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
		return true;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

}
