package it.eng.spagobi.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CatalogFunction {
	private int functionId;
	private String name;
	private String language;
	private String script;

	private Map<String, String> sbiFunctionInputVariables = new HashMap<String, String>();
	private List<String> sbiFunctionInputDatasets = new ArrayList<String>();
	private Map<String, String> sbiFunctionOutput = new HashMap<String, String>();

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

	public Map<String, String> getSbiFunctionInputVariables() {
		return sbiFunctionInputVariables;
	}

	public void setSbiFunctionInputVariables(Map<String, String> sbiFunctionInputVariables) {
		this.sbiFunctionInputVariables = sbiFunctionInputVariables;
	}

	public List<String> getSbiFunctionInputDatasets() {
		return sbiFunctionInputDatasets;
	}

	public void setSbiFunctionInputDatasets(List<String> sbiFunctionInputDatasets) {
		this.sbiFunctionInputDatasets = sbiFunctionInputDatasets;
	}

	public Map<String, String> getSbiFunctionOutput() {
		return sbiFunctionOutput;
	}

	public void setSbiFunctionOutput(Map<String, String> sbiFunctionOutput) {
		this.sbiFunctionOutput = sbiFunctionOutput;
	}

}
