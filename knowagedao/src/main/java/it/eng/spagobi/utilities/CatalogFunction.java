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
	private String owner;
	private String label;
	private String type;
	private String url;
	private boolean remote;

	private Map<String, String> inputVariables = new HashMap<String, String>();
	private List<String> inputDatasets = new ArrayList<String>();
	private Map<String, String> outputs = new HashMap<String, String>();
	List<CatalogFunctionInputFile> inputFiles = new ArrayList<CatalogFunctionInputFile>();

	private List<String> keywords = new ArrayList<String>();

	public CatalogFunction(int functionId, String name, String description, String language, String script, String owner, String label, String type,
			Map<String, String> inputVariables, List<CatalogFunctionInputFile> inputFiles, List<String> inputDatasets, Map<String, String> outputs,
			List<String> keywords, String url, boolean remote) {
		super();
		this.functionId = functionId;
		this.name = name;
		this.description = description;
		this.language = language;
		this.script = script;
		this.owner = owner;
		this.label = label;
		this.type = type;
		this.inputVariables = inputVariables;
		this.inputDatasets = inputDatasets;
		this.inputFiles = inputFiles;
		this.outputs = outputs;
		this.keywords = keywords;
		this.url = url;
		this.remote = remote;
	}

	public CatalogFunction() {
	}

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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean getRemote() {
		return remote;
	}

	public void setRemote(boolean remote) {
		this.remote = remote;
	}

	public List<CatalogFunctionInputFile> getInputFiles() {
		return inputFiles;
	}

	public void setInputFiles(List<CatalogFunctionInputFile> inputFiles) {
		this.inputFiles = inputFiles;
	}

}
