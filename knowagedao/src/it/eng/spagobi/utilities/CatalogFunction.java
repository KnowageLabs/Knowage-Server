package it.eng.spagobi.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CatalogFunction {
	private int functionId;
	private String name;
	private String description;
	private String language;
	private String script;

	private Map<String, String> inputVariables = new HashMap<String, String>();
	private List<String> inputDatasets = new ArrayList<String>();
	private Map<String, String> outputs = new HashMap<String, String>();

	public int getFunctionId() {
		return functionId;
	}

	public void setFunctionId(int functionId) {
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

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public Map<String, String> getInputVariables() {
		return inputVariables;
	}

	public void setInputVariables(Map<String, String> inputVariables) {
		this.inputVariables = inputVariables;
	}

	public List<String> getInputDatasets() {
		return inputDatasets;
	}

	public void setInputDatasets(List<String> inputDatasets) {
		this.inputDatasets = inputDatasets;
	}

	public Map<String, String> getOutputs() {
		return outputs;
	}

	public void setOutputs(Map<String, String> outputs) {
		this.outputs = outputs;
	}

}
