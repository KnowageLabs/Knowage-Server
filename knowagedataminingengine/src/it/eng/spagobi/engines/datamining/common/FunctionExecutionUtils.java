package it.eng.spagobi.engines.datamining.common;

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
				d.setType("Dataset"); // or DataMiningConstants.DATASET_OUTPUT or DataMiningConstants.SPAGOBI_DS_OUTPUT, the dataminingEngine differences
										// spagoBI datasets from file datasets created when executing a document
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
				if (!result.has("result") || !result.has("resultType") || !result.has("resultName")) {
					return false;
				}
				// TODO: add check for FILE
			}
		} catch (JSONException e) {
			logger.error("Exception while using JSONArray response [" + response.toString() + "]", e);
			return false;
		}
		return true;
	}

	public static String getRequestBody(SbiCatalogFunction function) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public static void substituteWithReplacingValues(SbiCatalogFunction function, String body) {
		logger.debug("IN");
		try {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, String> variablesInMap = new HashMap<String, String>();
			Map<String, String> datasetsInMap = new HashMap<String, String>();
			Map<String, Map<String, String>> filesInMap = new HashMap<String, Map<String, String>>();

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
				case DataMiningConstants.DATASETS_IN:
					datasetsInMap = mapper.readValue(items.toString(), new TypeReference<Map<String, String>>() {
					});
					break;
				case DataMiningConstants.FILES_IN:
					filesInMap = mapper.readValue(items.toString(), new TypeReference<Map<String, String>>() {
					});
					break;
				}
			}

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
			boolean isImage = r.getOutputType().equalsIgnoreCase(DataMiningConstants.IMAGE_OUTPUT);
			JSONObject o = new JSONObject();
			o.put(DataMiningConstants.RESULT_TYPE_FIELD, r.getOutputType());
			o.put(DataMiningConstants.RESULT_CONTENT_FIELD, r.getResult());
			o.put(DataMiningConstants.RESULT_NAME_FIELD, isImage ? r.getPlotName() : r.getVariablename());
			response.put(o);
		}
		return response;
	}
}
