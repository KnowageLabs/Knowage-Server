package it.eng.spagobi.engines.datamining.common;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.engines.datamining.common.utils.DataMiningConstants;
import it.eng.spagobi.engines.datamining.model.DataMiningCommand;
import it.eng.spagobi.engines.datamining.model.DataMiningDataset;
import it.eng.spagobi.engines.datamining.model.DataMiningScript;
import it.eng.spagobi.engines.datamining.model.Output;
import it.eng.spagobi.engines.datamining.model.Variable;
import it.eng.spagobi.engines.datamining.template.DataMiningTemplate;
import it.eng.spagobi.functions.dao.ICatalogFunctionDAO;
import it.eng.spagobi.functions.metadata.SbiCatalogFunction;
import it.eng.spagobi.functions.metadata.SbiFunctionInputDataset;
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
import org.json.JSONObject;

public class FunctionExecutionUtils {

	static protected Logger logger = Logger.getLogger(FunctionExecutionUtils.class);

	public static DataMiningTemplate initializeTemplateByFunctionId(int functionId) {
		SbiCatalogFunction function = null;
		ICatalogFunctionDAO fcDAO = null;
		try {
			fcDAO = DAOFactory.getCatalogFunctionDAO();
		} catch (EMFUserError e1) {
			throw new SpagoBIRuntimeException("Error getting catalog function DAO", e1);
		}
		function = fcDAO.getCatalogFunctionById(functionId);
		DataMiningTemplate template = getDataMiningTemplate(function);
		return template;

	}

	public static DataMiningTemplate getTemplateWithReplacingValues(int functionId, String body, Map<String, Map<String, String>> functionIOMaps) {
		logger.debug("IN");
		DataMiningTemplate template;
		try {
			ICatalogFunctionDAO fcDAO = DAOFactory.getCatalogFunctionDAO();
			SbiCatalogFunction function = fcDAO.getCatalogFunctionById(functionId);
			template = getTemplateWithReplacingValuesFromFunction(function, body, functionIOMaps);
		} catch (EMFUserError e1) {
			throw new SpagoBIRuntimeException("Error getting catalog function DAO", e1);
		}
		logger.debug("OUT");
		return template;

	}

	private static DataMiningTemplate getTemplateWithReplacingValuesFromFunction(SbiCatalogFunction function, String body,
			Map<String, Map<String, String>> functionIOMaps) {
		// Maps contain values to use instead of function values
		DataMiningTemplate template = null;
		try {
			template = new DataMiningTemplate();
			template.setLanguage(function.getLanguage());

			Set<SbiFunctionInputDataset> datasets = function.getSbiFunctionInputDatasets();
			List<DataMiningDataset> dataminingDatasets = new ArrayList<DataMiningDataset>();

			Map<String, String> variablesInMap = functionIOMaps.get(DataMiningConstants.VARIABLES_IN);
			Map<String, String> datasetsInMap = functionIOMaps.get(DataMiningConstants.DATASETS_IN);
			Map<String, String> datasetsOutMap = functionIOMaps.get(DataMiningConstants.DATASETS_OUT);
			Map<String, String> textOutMap = functionIOMaps.get(DataMiningConstants.TEXT_OUT);
			Map<String, String> imageOutMap = functionIOMaps.get(DataMiningConstants.IMAGE_OUT);

			for (SbiFunctionInputDataset dataset : datasets) {
				DataMiningDataset d = new DataMiningDataset();
				IDataSetDAO dsDAO = DAOFactory.getDataSetDAO();
				int dsId = dataset.getId().getDsId();
				IDataSet iDataset = dsDAO.loadDataSetById(dsId);// *
				String labelDemoDS = iDataset.getLabel();
				if (datasetsInMap.containsKey(labelDemoDS)) // map element format: <demoLabel:replacingLabel>
				{
					String datasetHavingReplacingDSlabel = datasetsInMap.get(labelDemoDS);
					JSONObject dsHavingReplacingDSlabel = new JSONObject(datasetHavingReplacingDSlabel);
					String replacingDSlabel = dsHavingReplacingDSlabel.getString("label");

					if (replacingDSlabel != null && (!replacingDSlabel.equals(""))) { // if a replacing dataset isn't specified, associate the demo dataset
						IDataSet ds = dsDAO.loadDataSetByLabel(replacingDSlabel);
						if (ds != null) // if a replacing dataset isn't specified, associate the demo dataset
						{
							iDataset = ds;
						}
					}
				}

				d.setLabel(iDataset.getLabel());
				d.setSpagobiLabel(iDataset.getLabel()); // Important! used label is spagobiLabel!
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

			DataMiningCommand c = new DataMiningCommand();
			c.setLabel("CatalogCommand");
			c.setName("CatalogCommand");
			c.setScriptName("CatalogScript");

			List<Variable> vars = new ArrayList<Variable>();
			List<Output> outs = new ArrayList<Output>();

			for (SbiFunctionInputVariable v : variables) {
				Variable var = new Variable();
				String varName = v.getId().getVarName();
				var.setName(varName);
				String varValue = "";
				if (variablesInMap.containsKey(varName)) // map element format: <demoVarName:replacingVALUE>
				{
					String replacingVariableValue = variablesInMap.get(varName);
					if (!replacingVariableValue.equals("") && replacingVariableValue != null) // if a replacing dataset isn't specified, associate the demo
																								// dataset
					{
						varValue = replacingVariableValue;
					} else {
						varValue = v.getVarValue();
					}
				} else // variable not present in input map, use demo variable
				{
					varValue = v.getVarValue();
				}

				var.setValue(varValue);
				vars.add(var);
			}

			HashMap<String, String> mapImageAndTextOut = new HashMap<String, String>();
			mapImageAndTextOut.putAll(textOutMap);
			mapImageAndTextOut.putAll(imageOutMap);

			for (SbiFunctionOutput o : outputs) {
				Output out = new Output();
				String label = o.getId().getLabel();
				String oldLabel = o.getId().getLabel(); // old label is the value of the dataframe variable containing dataset value in script!!
				if (datasetsOutMap.containsKey(label)) {
					String replacingDatasetOutLabel = datasetsOutMap.get(label);
					if (!replacingDatasetOutLabel.equals("") && replacingDatasetOutLabel != null) {
						label = replacingDatasetOutLabel;
					}

				} else if (mapImageAndTextOut.containsKey(label)) {
					String replacingOutLabel = mapImageAndTextOut.get(label);
					if (!replacingOutLabel.equals("") && replacingOutLabel != null) {
						label = replacingOutLabel;
					}
				}
				out.setOuputLabel(label);
				out.setOutputName(label); // Name=label
				out.setOutputValue(oldLabel); // added, before it was label --> it's dataset's name in the script
				IDomainDAO domainsDAO = DAOFactory.getDomainDAO();
				String type = domainsDAO.loadDomainById(o.getOutType()).getValueName();
				out.setOutputType(type);
				out.setOutputMode("auto"); // TODO: ??? can't figure out what auto means...
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

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error creating a template to instantiate datamining engine, replacing demo input and outputs with new ones.", e);
		}

		return template;

	}

	private static DataMiningTemplate getDataMiningTemplate(SbiCatalogFunction function) {
		DataMiningTemplate template = null;
		IDataSetDAO dsDAO = null;
		try {
			dsDAO = DAOFactory.getDataSetDAO();

			template = new DataMiningTemplate();
			template.setLanguage(function.getLanguage());

			Set<SbiFunctionInputDataset> datasets = function.getSbiFunctionInputDatasets();
			List<DataMiningDataset> dataminingDatasets = new ArrayList<DataMiningDataset>();

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

				d.setFileName(confObj.getString("fileName"));
				d.setOptions("sep='" + confObj.getString("csvDelimiter") + "'");
				d.setReadType(confObj.getString("fileType").toLowerCase());
				dataminingDatasets.add(d);
			}
			template.setDatasets(dataminingDatasets);

			Set<SbiFunctionInputVariable> variables = function.getSbiFunctionInputVariables();
			Set<SbiFunctionOutput> outputs = function.getSbiFunctionOutputs();

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
		} catch (Exception e) {
			e.printStackTrace();
		}
		return template;
	}

}
