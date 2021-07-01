/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2020 Engineering Ingegneria Informatica S.p.A.

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

package it.eng.knowage.functionscatalog.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

import it.eng.spagobi.services.validation.ExtendedAlphanumeric;
import it.eng.spagobi.services.validation.Xss;

public class CatalogFunctionDTO {

	private UUID id;
	private String label;
	private String name;
	private String owner;
	private String description;
	private String benchmarks;
	private String language;
	private String family;
	private String onlineScript;
	private String offlineScriptTrainModel;
	private String offlineScriptUseModel;
	private String type;
	private List<InputVariableDTO> inputVariables = new ArrayList<InputVariableDTO>();
	private List<InputColumnDTO> inputColumns = new ArrayList<InputColumnDTO>();
	private List<OutputColumnDTO> outputColumns = new ArrayList<OutputColumnDTO>();
	private List<String> keywords = new ArrayList<String>();

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	@ExtendedAlphanumeric
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ExtendedAlphanumeric
	public String getOwner() {
		return owner;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public void setOwner(String owner) {
		this.owner = owner;
	}

	@Xss
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Xss
	public String getBenchmarks() {
		return benchmarks;
	}

	public void setBenchmarks(String benchmarks) {
		this.benchmarks = benchmarks;
	}

	@ExtendedAlphanumeric
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@JsonProperty("functionFamily")
	@ExtendedAlphanumeric
	public String getFamily() {
		return family;
	}

	public void setFamily(String family) {
		this.family = family;
	}

	@Xss
	public String getOnlineScript() {
		return onlineScript;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public void setOnlineScript(String onlineScript) {
		this.onlineScript = onlineScript;
	}

	@Xss
	public String getOfflineScriptTrainModel() {
		return offlineScriptTrainModel;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public void setOfflineScriptTrainModel(String offlineScriptTrainModel) {
		this.offlineScriptTrainModel = offlineScriptTrainModel;
	}

	@Xss
	public String getOfflineScriptUseModel() {
		return offlineScriptUseModel;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public void setOfflineScriptUseModel(String offlineScriptUseModel) {
		this.offlineScriptUseModel = offlineScriptUseModel;
	}

	@ExtendedAlphanumeric
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@ExtendedAlphanumeric
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<InputVariableDTO> getInputVariables() {
		return inputVariables;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public void setInputVariables(List<InputVariableDTO> inputVariables) {
		this.inputVariables = inputVariables;
	}

	public List<InputColumnDTO> getInputColumns() {
		return inputColumns;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public void setInputColumns(List<InputColumnDTO> inputColumns) {
		this.inputColumns = inputColumns;
	}

	public List<OutputColumnDTO> getOutputColumns() {
		return outputColumns;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public void setOutputColumns(List<OutputColumnDTO> outputColumns) {
		this.outputColumns = outputColumns;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public static CatalogFunctionDTO valueOf(String json) {
		return new Gson().fromJson(json, CatalogFunctionDTO.class);
	}
}
