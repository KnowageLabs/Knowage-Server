package it.eng.spagobi.engines.datamining.common;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.engines.datamining.bo.DataMiningResult;
import it.eng.spagobi.engines.datamining.common.utils.DataMiningConstants;
import it.eng.spagobi.engines.datamining.model.DataMiningCommand;
import it.eng.spagobi.engines.datamining.model.DataMiningDataset;
import it.eng.spagobi.engines.datamining.model.DataMiningFile;
import it.eng.spagobi.engines.datamining.model.DataMiningScript;
import it.eng.spagobi.engines.datamining.model.Output;
import it.eng.spagobi.engines.datamining.model.Variable;
import it.eng.spagobi.engines.datamining.template.DataMiningTemplate;
import it.eng.spagobi.functions.metadata.SbiCatalogFunction;
import it.eng.spagobi.functions.metadata.SbiFunctionInputDataset;
import it.eng.spagobi.functions.metadata.SbiFunctionInputFile;
import it.eng.spagobi.functions.metadata.SbiFunctionInputVariable;
import it.eng.spagobi.functions.metadata.SbiFunctionOutput;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FunctionExecutionUtils {

	static protected Logger logger = Logger.getLogger(FunctionExecutionUtils.class);

	@SuppressWarnings("unchecked")
	public static DataMiningTemplate getDataMiningTemplate(SbiCatalogFunction function) {
		DataMiningTemplate template = null;
		IDataSetDAO dsDAO = null;
		try {
			dsDAO = DAOFactory.getDataSetDAO();

			template = new DataMiningTemplate();
			template.setLanguage(function.getLanguage());

			Set<SbiFunctionInputDataset> datasets = function.getSbiFunctionInputDatasets();
			List<DataMiningDataset> dataminingDatasets = new ArrayList<DataMiningDataset>();
			List<DataMiningFile> dataminingFiles = new ArrayList<DataMiningFile>();

			for (SbiFunctionInputDataset dataset : datasets) {
				DataMiningDataset d = new DataMiningDataset();
				int dsId = dataset.getId().getDsId();
				IDataSet iDataset = dsDAO.loadDataSetById(dsId);
				// Controllo se dataset è di tipo file, del tipo previsto (e.g. csv)
				d.setLabel(iDataset.getLabel());
				d.setSpagobiLabel(iDataset.getLabel());
				d.setCanUpload(true);
				d.setName(iDataset.getName());
				d.setType(DataMiningConstants.DATASET_TYPE_DATASET); // the dataminingEngine differences spagoBI datasets from file datasets created when
																		// executing a document
				JSONObject confObj = new JSONObject(iDataset.getConfiguration());

				if (confObj.has("fileName")) {
					d.setFileName(confObj.getString("fileName"));
					d.setOptions("sep='" + confObj.getString("csvDelimiter") + "'");
					d.setReadType(confObj.getString("fileType").toLowerCase());
				}
				dataminingDatasets.add(d);
			}
			template.setDatasets(dataminingDatasets);

			Set<SbiFunctionInputVariable> variables = function.getSbiFunctionInputVariables();
			Set<SbiFunctionOutput> outputs = function.getSbiFunctionOutputs();
			Set<SbiFunctionInputFile> files = function.getSbiFunctionInputFiles();

			DataMiningCommand c = new DataMiningCommand();
			c.setLabel("CatalogCommand");
			c.setName("CatalogCommand");
			c.setScriptName("CatalogScript");

			List<Variable> vars = new ArrayList<Variable>();
			List<Output> outs = new ArrayList<Output>();

			for (SbiFunctionInputVariable v : variables) {
				Variable var = new Variable();
				var.setName(v.getId().getVarName());
				var.setValue(v.getVarValue());
				vars.add(var);
			}

			for (SbiFunctionInputFile f : files) {
				DataMiningFile file = new DataMiningFile();
				file.setAlias(f.getAlias());
				file.setContent(f.getContent());
				file.setFileName(f.getId().getFileName());
				dataminingFiles.add(file);
			}

			for (SbiFunctionOutput o : outputs) {
				Output out = new Output();
				out.setOuputLabel(o.getId().getLabel());
				out.setOutputName(o.getId().getLabel()); // Name=label
				IDomainDAO domainsDAO = DAOFactory.getDomainDAO();
				String type = domainsDAO.loadDomainById(o.getOutType()).getValueName();
				out.setOutputType(type);
				out.setOutputMode("auto");
				out.setOutputName(o.getId().getLabel());
				out.setOutputValue(o.getId().getLabel());
				outs.add(out);
			}
			c.setVariables(vars);
			c.setOutputs(outs);

			List<DataMiningCommand> commands = new ArrayList<DataMiningCommand>();
			commands.add(c);
			template.setCommands(commands);

			List<DataMiningScript> dataMiningScripts = new ArrayList<DataMiningScript>();
			String scriptCode = function.getScript();
			DataMiningScript script = new DataMiningScript();
			script.setName("CatalogScript");
			script.setCode(scriptCode);
			dataMiningScripts.add(script);
			template.setScripts(dataMiningScripts);
			template.setFiles(dataminingFiles);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return template;
	}

	public static boolean isResponseCompliant(SbiCatalogFunction function, JSONArray response) {
		try {
			for (int i = 0; i < response.length(); i++) {
				JSONObject result;
				result = response.getJSONObject(i);
				if (!result.has(DataMiningConstants.RESULT_CONTENT_FIELD) || !result.has(DataMiningConstants.RESULT_TYPE_FIELD)
						|| !result.has(DataMiningConstants.RESULT_NAME_FIELD)) {
					return false;
				}
				if (result.getString(DataMiningConstants.RESULT_TYPE_FIELD).equalsIgnoreCase("File")) {
					if (!result.getJSONObject(DataMiningConstants.RESULT_CONTENT_FIELD).has("base64")
							|| !result.getJSONObject(DataMiningConstants.RESULT_CONTENT_FIELD).has("filename")) {
						return false;
					}
				}
			}
		} catch (JSONException e) {
			logger.error("Exception while using JSONArray response [" + response.toString() + "]", e);
			return false;
		}
		return true;
	}

	public static Map<String, String> getReplacementsDatasetMap(String body) {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> datasetsInMap = new HashMap<String, String>();
		try {

			JSONArray replacements = new JSONArray(body);
			for (int i = 0; i < replacements.length(); i++) {
				JSONObject object = replacements.getJSONObject(i);
				JSONObject items = object.getJSONObject("items");
				String type = object.getString("type");
				switch (type) {
				case DataMiningConstants.DATASETS_IN:
					datasetsInMap = mapper.readValue(items.toString(), new TypeReference<Map<String, String>>() {
					});
					break;
				}
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while reading dataset and variables replacements from body: ", e);
		}
		return datasetsInMap;
	}

	public static Map<String, String> getReplacementsVariablesMap(String body) {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> variablesInMap = new HashMap<String, String>();

		try {

			JSONArray replacements = new JSONArray(body);
			for (int i = 0; i < replacements.length(); i++) {
				JSONObject object = replacements.getJSONObject(i);
				JSONObject items = object.getJSONObject("items");
				String type = object.getString("type");
				switch (type) {
				case DataMiningConstants.VARIABLES_IN:
					variablesInMap = mapper.readValue(items.toString(), new TypeReference<Map<String, String>>() {
					});
					break;
				}
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while reading dataset and variables replacements from body: ", e);
		}
		return variablesInMap;
	}

	public static Map<String, Map<String, String>> getReplacementsFilesMap(String body) {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Map<String, String>> filesInMap = new HashMap<String, Map<String, String>>();
		try {

			JSONArray replacements = new JSONArray(body);
			for (int i = 0; i < replacements.length(); i++) {
				JSONObject object = replacements.getJSONObject(i);
				JSONObject items = object.getJSONObject("items");
				String type = object.getString("type");
				switch (type) {
				case DataMiningConstants.FILES_IN:
					// {"f":{"file":{},"base64":"data:;base64,CBZb3UgbWF5IG9idGFpbiBhIGNvcHkgb2YgdGhlIExpY2Vuc2UgYXQKIwojICAgICBodHRwOi8vd3d3LmFwYWNoZS5vcmcvbGljZW5zZXMvTElDRU5TRS0yLjAKIwojIFVubGVzcyByZXF1aXJlZCBieSBhcHBsaWNhYmxlIGxhdyBvciBhZ3JlZWQgdG8gaW4gd3JpdGluZywgc29mdHdhcmUKIyBkaXN0cmlidXRlZCB1bmRlciB0aGUgTGljZW5zZSBpcyBkaXN0cmlidXRlZCBvbiBhbiAiQVMgSVMiIEJBU0lTLAojIFdJVEhPVVQgV0FSUkFOVElFUyBPUiBDT05ESVRJT05TIE9GIEFOWSBLSU5ELCBlaXRoZXIgZXhwcmVzcyBvciBpbXBsaWVkLgojIFNlZSB0aGUgTGljZW5zZSBmb3IgdGhlIHNwZWNpZmljIGxhbmd1YWdlIGdvdmVybmluZyBwZXJtaXNzaW9ucyBhbmQKIyBsaW1pdGF0aW9ucyB1bmRlciB0aGUgTGljZW5zZS4KCiMgLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0KIyBTY3JpcHQgdG8gZGlnZXN0IHBhc3N3b3JkIHVzaW5nIHRoZSBhbGdvcml0aG0gc3BlY2lmaWVkCiMgLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0KCiMgQmV0dGVyIE9TLzQwMCBkZXRlY3Rpb246IHNlZSBCdWd6aWxsYSAzMTEzMgpvczQwMD1mYWxzZQpjYXNlICJgdW5hbWVgIiBpbgpPUzQwMCopIG9zNDAwPXRydWU7Owplc2FjCgojIHJlc29sdmUgbGlua3MgLSAkMCBtYXkgYmUgYSBzb2Z0bGluawpQUkc9IiQwIgoKd2hpbGUgWyAtaCAiJFBSRyIgXSA7IGRvCiAgbHM9YGxzIC1sZCAiJFBSRyJgCiAgbGluaz1gZXhwciAiJGxzIiA6ICcuKi0+IFwoLipcKSQnYAogIGlmIGV4cHIgIiRsaW5rIiA6ICcvLionID4gL2Rldi9udWxsOyB0aGVuCiAgICBQUkc9IiRsaW5rIgogIGVsc2UKICAgIFBSRz1gZGlybmFtZSAiJFBSRyJgLyIkbGluayIKICBmaQpkb25lCgpQUkdESVI9YGRpcm5hbWUgIiRQUkciYApFWEVDVVRBQkxFPXRvb2wtd3JhcHBlci5zaAoKIyBDaGVjayB0aGF0IHRhcmdldCBleGVjdXRhYmxlIGV4aXN0cwppZiAkb3M0MDA7IHRoZW4KICAjIC14IHdpbGwgT25seSB3b3JrIG9uIHRoZSBvczQwMCBpZiB0aGUgZmlsZXMgYXJlOgogICMgMS4gb3duZWQgYnkgdGhlIHVzZXIKICAjIDIuIG93bmVkIGJ5IHRoZSBQUklNQVJZIGdyb3VwIG9mIHRoZSB1c2VyCiAgIyB0aGlzIHdpbGwgbm90IHdvcmsgaWYgdGhlIHVzZXIgYmVsb25ncyBpbiBzZWNvbmRhcnkgZ3JvdXBzCiAgZXZhbAplbHNlCiAgaWYgWyAhIC14ICIkUFJHRElSIi8iJEVYRUNVVEFCTEUiIF07IHRoZW4KICAgIGVjaG8gIkNhbm5vdCBmaW5kICRQUkdESVIvJEVYRUNVVEFCTEUiCiAgICBlY2hvICJUaGUgZmlsZSBpcyBhYnNlbnQgb3IgZG9lcyBub3QgaGF2ZSBleGVjdXRlIHBlcm1pc3Npb24iCiAgICBlY2hvICJUaGlzIGZpbGUgaXMgbmVlZGVkIHRvIHJ1biB0aGlzIHByb2dyYW0iCiAgICBleGl0IDEKICBmaQpmaQoKZXhlYyAiJFBSR0RJUiIvIiRFWEVDVVRBQkxFIiAtc2VydmVyIG9yZy5hcGFjaGUuY2F0YWxpbmEucmVhbG0uUmVhbG1CYXNlICIkQCIK","fileName":"digest.sh"}}
					Iterator<String> iter = items.keys();
					while (iter.hasNext()) {
						String key = iter.next();
						items.getJSONObject(key).remove("file");
					}
					filesInMap = mapper.readValue(items.toString(), new TypeReference<Map<String, Map<String, String>>>() {
					});
					break;
				}
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while reading files replacements from body: ", e);
		}
		return filesInMap;
	}

	@SuppressWarnings("unchecked")
	public static void substituteWithReplacingValues(SbiCatalogFunction function, Map<String, String> variablesInMap, Map<String, String> datasetsInMap,
			Map<String, Map<String, String>> filesInMap) {
		logger.debug("IN");
		try {

			logger.debug("Initializing function with POSTed contents");
			logger.debug("Initializing dataset input type");
			IDataSetDAO dsDAO = DAOFactory.getDataSetDAO();
			Set<SbiFunctionInputDataset> defaultDataSets = function.getSbiFunctionInputDatasets();
			for (SbiFunctionInputDataset defaultDataSet : defaultDataSets) {
				int dsIdToBeReplaced = defaultDataSet.getId().getDsId();
				logger.debug("Getting label for input dataset with Id [" + dsIdToBeReplaced + "]");
				IDataSet dataSetToBeReplaced = dsDAO.loadDataSetById(dsIdToBeReplaced);
				if (dataSetToBeReplaced == null) {
					throw new SpagoBIRuntimeException("Impossible to find a dataset with id [" + dsIdToBeReplaced + "]. Please check if it has been deleted.");
				}
				String dsLabel = datasetsInMap.get(dataSetToBeReplaced.getLabel());
				if (dsLabel != null) {
					logger.debug("The request has provided a new dataset [" + dsLabel + "] to be used...");
					IDataSet dataSet = dsDAO.loadDataSetByLabel(dsLabel);
					if (dataSet == null) {
						throw new SpagoBIRuntimeException("Impossible to find a dataset with label [" + dsLabel + "]. Please check if it has been deleted.");
					}
					defaultDataSet.getId().setDsId(dataSet.getId());
				}
			}

			logger.debug("Initializing variable input type");
			Set<SbiFunctionInputVariable> defaultVariables = function.getSbiFunctionInputVariables();
			for (SbiFunctionInputVariable defaultVariable : defaultVariables) {
				String varToBeReplaced = defaultVariable.getId().getVarName();
				String value = variablesInMap.get(varToBeReplaced);
				if (value != null) {
					logger.debug("The request has provided a new value [" + value + "] to be used for variable named [" + varToBeReplaced + "]...");
					defaultVariable.setVarValue(value);
				}
			}

			logger.debug("Initializing file input type");
			Set<SbiFunctionInputFile> defaultFiles = function.getSbiFunctionInputFiles();
			for (SbiFunctionInputFile defaultFile : defaultFiles) {
				String fileToBeReplaced = defaultFile.getAlias();
				Map<String, String> file = filesInMap.get(fileToBeReplaced);
				if (file != null) {
					logger.debug("The request has provided a new file content for [" + fileToBeReplaced + "]...");
					defaultFile.getId().setFileName(file.get("filename"));
					defaultFile.setContent(file.get("base64").getBytes());
				}
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while replacing default input with the provided ones.", e);
		}
	}

	public static JSONArray buildDataminingResponse(List<DataMiningResult> dataminingExecutionResults) throws JSONException {
		JSONArray response = new JSONArray();
		for (DataMiningResult r : dataminingExecutionResults) {
			if (r.getOutputType() == null) {
				throw new SpagoBIRuntimeException("DataMining execution error:" + r.getError());
			}
			boolean isImage = r.getOutputType().equalsIgnoreCase(DataMiningConstants.IMAGE_OUTPUT);
			JSONObject o = new JSONObject();
			o.put(DataMiningConstants.RESULT_TYPE_FIELD, r.getOutputType());
			o.put(DataMiningConstants.RESULT_CONTENT_FIELD, r.getResult());
			o.put(DataMiningConstants.RESULT_NAME_FIELD, isImage ? r.getPlotName() : r.getVariablename());
			response.put(o);
		}
		return response;
	}

	public static void adjustBodyContentsForRemoteExecution(String body) {
		// TODO Check if there are datasets and serialize them as files....
	}

	public static String getBodyRequestForRemoteExecution(SbiCatalogFunction function, UserProfile userProfile) throws JSONException, EMFUserError {
		JSONArray body = new JSONArray();

		JSONObject datasetsIn = new JSONObject();
		datasetsIn.put("type", DataMiningConstants.DATASETS_IN);
		JSONObject items = new JSONObject();
		Set<SbiFunctionInputDataset> datasets = function.getSbiFunctionInputDatasets();
		for (SbiFunctionInputDataset dataset : datasets) {
			IDataSetDAO dataSetDAO = DAOFactory.getDataSetDAO();
			dataSetDAO.setUserProfile(userProfile);
			IDataSet ds = dataSetDAO.loadDataSetById(dataset.getId().getDsId());
			if (ds == null) {
				throw new SpagoBIRuntimeException("Impossible to find dataset with id [" + dataset.getId().getDsId() + "].");
			}
			String label = ds.getLabel();
			items.put(label, label);
		}
		datasetsIn.put("items", items);

		JSONObject variablesIn = new JSONObject();
		variablesIn.put("type", DataMiningConstants.VARIABLES_IN);
		items = new JSONObject();
		Set<SbiFunctionInputVariable> variables = function.getSbiFunctionInputVariables();
		for (SbiFunctionInputVariable variable : variables) {
			items.put(variable.getId().getVarName(), variable.getVarValue());
		}
		variablesIn.put("items", items);

		JSONObject filesIn = new JSONObject();
		filesIn.put("type", DataMiningConstants.FILES_IN);
		items = new JSONObject();
		Set<SbiFunctionInputFile> files = function.getSbiFunctionInputFiles();
		for (SbiFunctionInputFile file : files) {
			JSONObject jsonFile = new JSONObject();
			jsonFile.put("filename", file.getId().getFileName());
			jsonFile.put("base64", new String(file.getContent()));
			items.put(file.getAlias(), jsonFile);
		}
		filesIn.put("items", items);

		body.put(datasetsIn);
		body.put(variablesIn);
		body.put(filesIn);

		return body.toString();
	}
}
