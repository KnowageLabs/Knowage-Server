package it.eng.spagobi.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.eng.spagobi.functions.metadata.IInputVariable;
import it.eng.spagobi.functions.metadata.IOutputColumn;

public class CatalogFunction {

	private String name;
	private String description;
	private String benchmarks;
	private String language;
	private String family;
	private String onlineScript;
	private String offlineScriptTrain;
	private String offlineScriptUse;
	private String owner;
	private String label;
	private String type;
	private Map<String, IInputVariable> inputVariables = new HashMap<String, IInputVariable>();
	private Map<String, String> inputColumns = new HashMap<String, String>();
	private Map<String, IOutputColumn> outputColumns = new HashMap<String, IOutputColumn>();

	private List<String> keywords = new ArrayList<String>();

	public CatalogFunction() {
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

	public Map<String, IInputVariable> getInputVariables() {
		return inputVariables;
	}

	public void setInputVariables(Map<String, IInputVariable> inputVariables) {
		this.inputVariables = inputVariables;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public Map<String, String> getInputColumns() {
		return inputColumns;
	}

	public void setInputColumns(Map<String, String> inputColumns) {
		this.inputColumns = inputColumns;
	}

	public Map<String, IOutputColumn> getOutputColumns() {
		return outputColumns;
	}

	public void setOutputColumns(Map<String, IOutputColumn> outputColumns) {
		this.outputColumns = outputColumns;
	}
}
