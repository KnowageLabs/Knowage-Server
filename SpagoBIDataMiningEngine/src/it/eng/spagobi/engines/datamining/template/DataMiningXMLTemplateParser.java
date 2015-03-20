/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.datamining.template;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.datamining.model.DataMiningCommand;
import it.eng.spagobi.engines.datamining.model.DataMiningDataset;
import it.eng.spagobi.engines.datamining.model.DataMiningScript;
import it.eng.spagobi.engines.datamining.model.Output;
import it.eng.spagobi.engines.datamining.model.Variable;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author Monica Franceschini
 */
public class DataMiningXMLTemplateParser implements IDataMiningTemplateParser {

	public static String TAG_ROOT = "DATA_MINING";
	public static String TAG_SCRIPTS = "SCRIPTS";
	public static String TAG_SCRIPT = "SCRIPT";
	public static String TAG_DATASETS = "DATASETS";
	public static String TAG_DATASET = "DATASET";
	public static String TAG_OUTPUTS = "OUTPUTS";
	public static String TAG_OUTPUT = "OUTPUT";
	public static String TAG_COMMANDS = "COMMANDS";
	public static String TAG_COMMAND = "COMMAND";

	public static String SCRIPT_ATTRIBUTE_MODE = "mode";
	public static String SCRIPT_ATTRIBUTE_NAME = "name";
	public static String SCRIPT_ATTRIBUTE_DATASETS = "datasets";
	public static String SCRIPT_ATTRIBUTE_LABEL = "label";
	public static String SCRIPT_ATTRIBUTE_LIBRARIES = "libraries";

	public static String COMMAND_ATTRIBUTE_SCRIPTNAME = "scriptName";
	public static String COMMAND_ATTRIBUTE_NAME = "name";
	public static String COMMAND_ATTRIBUTE_OUTPUTS = "outputs";
	public static String COMMAND_ATTRIBUTE_LABEL = "label";
	public static String COMMAND_ATTRIBUTE_MODE = "mode";
	public static String COMMAND_ATTRIBUTE_ACTION = "action";

	public static String DATASET_ATTRIBUTE_READTYPE = "readType";
	public static String DATASET_ATTRIBUTE_NAME = "name";
	public static String DATASET_ATTRIBUTE_SPAGOBILABEL = "spagobiLabel";
	public static String DATASET_ATTRIBUTE_TYPE = "type";
	public static String DATASET_ATTRIBUTE_MODE = "mode";
	public static String DATASET_ATTRIBUTE_LABEL = "label";
	public static String DATASET_ATTRIBUTE_DEFAULT = "default";
	public static String DATASET_ATTRIBUTE_CANUPLOAD = "canUpload";

	public static final String DATASET_TYPE_FILE = "file";
	public static final String DATASET_TYPE_SPAGOBI_DS = "spagobi_ds";

	public static String OUTPUT_ATTRIBUTE_TYPE = "type";
	public static String OUTPUT_ATTRIBUTE_NAME = "name";
	public static String OUTPUT_ATTRIBUTE_DATATYPE = "dataType";
	public static String OUTPUT_ATTRIBUTE_VALUE = "value";
	public static String OUTPUT_ATTRIBUTE_MODE = "mode";
	public static String OUTPUT_ATTRIBUTE_LABEL = "label";
	public static String OUTPUT_ATTRIBUTE_FUNCTION = "function";

	public static String PROP_PARAMETER_NAME = "name";
	public static String PROP_PARAMETER_ALIAS = "as";
	public static String TAG_PARAMETERS = "PARAMETERS";
	public static String TAG_PARAMETER = "PARAMETER";
	
	public static String TAG_VARIABLES = "VARIABLES";
	public static String TAG_VARIABLE = "VARIABLE";
	public static String VARIABLE_ATTRIBUTE_NAME = "name";
	public static String VARIABLE_ATTRIBUTE_DEFAULT = "default";

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(DataMiningXMLTemplateParser.class);

	public DataMiningTemplate parse(Object template) {
		Assert.assertNotNull(template, "Input parameter [template] cannot be null");
		Assert.assertTrue(template instanceof SourceBean, "Input parameter [template] cannot be of type [" + template.getClass().getName() + "]");
		return parse((SourceBean) template);
	}

	private DataMiningTemplate parse(SourceBean template) {

		DataMiningTemplate toReturn = null;

		try {
			logger.debug("Starting template parsing....");

			toReturn = new DataMiningTemplate();

			SourceBean scriptsSB = (SourceBean) template.getAttribute(TAG_SCRIPTS);
			if (scriptsSB != null) {

				List<DataMiningScript> scripts = new ArrayList<DataMiningScript>();
				List<SourceBean> scriptListSB = scriptsSB.getAttributeAsList(TAG_SCRIPT);
				if (scriptListSB != null && scriptListSB.size() != 0) {
					for (Iterator iterator = scriptListSB.iterator(); iterator.hasNext();) {
						SourceBean scriptSB = (SourceBean) iterator.next();
						DataMiningScript script = new DataMiningScript();
						String text = scriptSB.getCharacters();
						script.setCode(text);
						String name = (String) scriptSB.getAttribute(SCRIPT_ATTRIBUTE_NAME);
						if (name != null) {
							script.setName(name);
						}
						String ds = (String) scriptSB.getAttribute(SCRIPT_ATTRIBUTE_DATASETS);
						if (ds != null) {
							script.setDatasets(ds);
						}
						String libraries = (String) scriptSB.getAttribute(SCRIPT_ATTRIBUTE_LIBRARIES);
						if (libraries != null) {
							script.setLibraries(libraries);
						}
						String mode = (String) scriptSB.getAttribute(SCRIPT_ATTRIBUTE_MODE);
						if (mode != null) {
							script.setMode(mode);
						}
						String label = (String) scriptSB.getAttribute(SCRIPT_ATTRIBUTE_LABEL);
						script.setLabel(label);

						scripts.add(script);
					}
					toReturn.setScripts(scripts);
				}
			}
			SourceBean datasetsSB = (SourceBean) template.getAttribute(TAG_DATASETS);
			if (datasetsSB != null) {

				List<DataMiningDataset> datasets = new ArrayList<DataMiningDataset>();
				List<SourceBean> datasetListSB = datasetsSB.getAttributeAsList(TAG_DATASET);
				if (datasetListSB != null && datasetListSB.size() != 0) {
					for (Iterator iterator = datasetListSB.iterator(); iterator.hasNext();) {
						SourceBean datasetSB = (SourceBean) iterator.next();
						DataMiningDataset ftds = new DataMiningDataset();
						logger.debug("dataset: " + datasetSB);
						Assert.assertNotNull(datasetSB, "Template is missing " + TAG_DATASET + " tag");

						String label = (String) datasetSB.getAttribute(DATASET_ATTRIBUTE_LABEL);
						ftds.setLabel(label);
						String datasetName = (String) datasetSB.getAttribute(DATASET_ATTRIBUTE_NAME);
						if (datasetName != null) {
							ftds.setName(datasetName);
						}
						String mode = (String) datasetSB.getAttribute(DATASET_ATTRIBUTE_MODE);
						if (mode != null) {
							ftds.setMode(mode);
						}
						String datasetType = (String) datasetSB.getAttribute(DATASET_ATTRIBUTE_TYPE);
						if (datasetType != null) {
							ftds.setType(datasetType);
							if (datasetType.equalsIgnoreCase(DATASET_TYPE_FILE)) {
								String datasetReadType = (String) datasetSB.getAttribute(DATASET_ATTRIBUTE_READTYPE);
								if (datasetReadType != null) {
									ftds.setReadType(datasetReadType);
								}
								String options = datasetSB.getCharacters();
								if (options != null) {
									ftds.setOptions(options);

								}
								String defaultds = (String) datasetSB.getAttribute(DATASET_ATTRIBUTE_DEFAULT);
								if (defaultds != null) {
									ftds.setDefaultDS(defaultds);
								}
								String canUpload = (String) datasetSB.getAttribute(DATASET_ATTRIBUTE_CANUPLOAD);
								if (canUpload != null && canUpload.equals("true")) {
									ftds.setCanUpload(true);
								}else{
									ftds.setCanUpload(false);
								}
							} else if (datasetType.equalsIgnoreCase(DATASET_TYPE_SPAGOBI_DS)) {
								String dsLabel = (String) datasetSB.getAttribute(DATASET_ATTRIBUTE_SPAGOBILABEL);
								if (dsLabel != null) {
									ftds.setSpagobiLabel(dsLabel);
								}
							}
						}

						datasets.add(ftds);
					}
					toReturn.setDatasets(datasets);
				}
			}
			SourceBean commandsSB = (SourceBean) template.getAttribute(TAG_COMMANDS);
			if (commandsSB != null) {

				List<DataMiningCommand> commands = new ArrayList<DataMiningCommand>();
				List<SourceBean> commandListSB = commandsSB.getAttributeAsList(TAG_COMMAND);
				if (commandListSB != null && commandListSB.size() != 0) {
					for (Iterator iterator = commandListSB.iterator(); iterator.hasNext();) {
						SourceBean commandSB = (SourceBean) iterator.next();
						DataMiningCommand command = new DataMiningCommand();

						String commandName = (String) commandSB.getAttribute(COMMAND_ATTRIBUTE_NAME);
						if (commandName != null) {
							command.setName(commandName);
						}
						String commandMode = (String) commandSB.getAttribute(COMMAND_ATTRIBUTE_MODE);
						command.setMode(commandMode);

						String commandLabel = (String) commandSB.getAttribute(COMMAND_ATTRIBUTE_LABEL);
						command.setLabel(commandLabel);

						String scriptName = (String) commandSB.getAttribute(COMMAND_ATTRIBUTE_SCRIPTNAME);
						if (scriptName != null) {
							command.setScriptName(scriptName);
						}
						String commandAction = (String) commandSB.getAttribute(COMMAND_ATTRIBUTE_ACTION);
						if (commandAction != null && !commandAction.equals("")) {
							command.setAction(commandAction);
						}
						SourceBean outputsSB = (SourceBean) commandSB.getAttribute(TAG_OUTPUTS);
						if (outputsSB != null) {

							List<Output> outputs = new ArrayList<Output>();
							List<SourceBean> outputListSB = outputsSB.getAttributeAsList(TAG_OUTPUT);
							if (outputListSB != null && outputListSB.size() != 0) {
								for (Iterator iterator2 = outputListSB.iterator(); iterator2.hasNext();) {
									SourceBean outputSB = (SourceBean) iterator2.next();
									Output out = new Output();
									String outputType = (String) outputSB.getAttribute(OUTPUT_ATTRIBUTE_TYPE);
									out.setOutputType(outputType);
									String outputName = (String) outputSB.getAttribute(OUTPUT_ATTRIBUTE_NAME);
									out.setOutputName(outputName);
									String outputDataType = (String) outputSB.getAttribute(OUTPUT_ATTRIBUTE_DATATYPE);
									out.setOutputDataType(outputDataType);
									String outputValue = (String) outputSB.getAttribute(OUTPUT_ATTRIBUTE_VALUE);
									out.setOutputValue(outputValue);
									String outputMode = (String) outputSB.getAttribute(OUTPUT_ATTRIBUTE_MODE);
									out.setOutputMode(outputMode);
									String outputLabel = (String) outputSB.getAttribute(OUTPUT_ATTRIBUTE_LABEL);
									out.setOuputLabel(outputLabel);
									String outputFunction = (String) outputSB.getAttribute(OUTPUT_ATTRIBUTE_FUNCTION);
									out.setOutputFunction(outputFunction);
									
									SourceBean varSB = (SourceBean) outputSB.getAttribute(TAG_VARIABLES);
									if (varSB != null) {

										List<Variable> variables = new ArrayList<Variable>();
										List<SourceBean> variablesListSB = varSB.getAttributeAsList(TAG_VARIABLE);
										if (variablesListSB != null && variablesListSB.size() != 0) {
											for (Iterator iterator3 = variablesListSB.iterator(); iterator3.hasNext();) {
												SourceBean variableSB = (SourceBean) iterator3.next();
												Variable var= new Variable();
												String name = (String) variableSB.getAttribute(VARIABLE_ATTRIBUTE_NAME);
												var.setName(name);
												String def = (String) variableSB.getAttribute(VARIABLE_ATTRIBUTE_DEFAULT);
												var.setDefaultVal(def);

												variables.add(var);
											}
											out.setVariables(variables);
										}
									}
									outputs.add(out);
								}
								command.setOutputs(outputs);
							}
						}

						SourceBean varSB = (SourceBean) commandSB.getAttribute(TAG_VARIABLES);
						if (varSB != null) {

							List<Variable> variables = new ArrayList<Variable>();
							List<SourceBean> variablesListSB = varSB.getAttributeAsList(TAG_VARIABLE);
							if (variablesListSB != null && variablesListSB.size() != 0) {
								for (Iterator iterator2 = variablesListSB.iterator(); iterator2.hasNext();) {
									SourceBean variableSB = (SourceBean) iterator2.next();
									Variable var= new Variable();
									String name = (String) variableSB.getAttribute(VARIABLE_ATTRIBUTE_NAME);
									var.setName(name);
									String def = (String) variableSB.getAttribute(VARIABLE_ATTRIBUTE_DEFAULT);
									var.setDefaultVal(def);

									variables.add(var);
								}
								command.setVariables(variables);
							}
						}
						commands.add(command);
					}

				}
				toReturn.setCommands(commands);
			}

			List<DataMiningTemplate.Parameter> parameters = new ArrayList<DataMiningTemplate.Parameter>();
			SourceBean parametersSB = (SourceBean) template.getAttribute(TAG_PARAMETERS);
			if (parametersSB != null) {

				List<SourceBean> parListSB = parametersSB.getAttributeAsList(TAG_PARAMETER);
				if (parListSB != null && parListSB.size() != 0) {
					for (Iterator iterator = parListSB.iterator(); iterator.hasNext();) {
						SourceBean parameterSB = (SourceBean) iterator.next();
						logger.debug("Found " + TAG_PARAMETER + " definition :" + parameterSB);
						String name = (String) parameterSB.getAttribute(PROP_PARAMETER_NAME);
						String alias = (String) parameterSB.getAttribute(PROP_PARAMETER_ALIAS);

						DataMiningTemplate.Parameter parameter = toReturn.new Parameter();
						parameter.setName(name);
						parameter.setAlias(alias);
						parameters.add(parameter);
					}
					toReturn.setParameters(parameters);
				}
			}
			// read user profile for profiled data access
			// setProfilingUserAttributes(template, toReturn);

			logger.debug("Template parsed succesfully");
		} catch (Exception e) {
			logger.error("Impossible to parse template [" + template.toString() + "]", e);
			throw new DataMiningTemplateParseException(e);
		} finally {
			logger.debug("OUT");
		}

		return toReturn;
	}

	/*
	 * private void setProfilingUserAttributes(SourceBean template,
	 * DataMiningTemplate toReturn) { SourceBean dataAccessSB = (SourceBean)
	 * template.getAttribute( TAG_DATA_ACCESS ); logger.debug(TAG_DATA_ACCESS +
	 * ": " + dataAccessSB); List<String> attributes = new ArrayList<String>();
	 * if (dataAccessSB != null) { List attributesSB =
	 * dataAccessSB.getAttributeAsList(TAG_USER_ATTRIBUTE); Iterator it =
	 * attributesSB.iterator(); while (it.hasNext()) { SourceBean attributeSB =
	 * (SourceBean) it.next(); logger.debug("Found " + TAG_USER_ATTRIBUTE +
	 * " definition :" + attributeSB); String name = (String)
	 * attributeSB.getAttribute(PROP_USER_ATTRIBUTE_NAME);
	 * Assert.assertNotNull(name, "Missing [" + PROP_PARAMETER_NAME +
	 * "] attribute in user profile attribute"); attributes.add(name); } }
	 * toReturn.setProfilingUserAttributes(attributes); }
	 */

	private static String getBeanValue(String tag, SourceBean bean) {
		String field = null;
		SourceBean fieldBean = null;
		fieldBean = (SourceBean) bean.getAttribute(tag);
		if (fieldBean != null) {
			field = fieldBean.getCharacters();
			if (field == null) {
				field = "";
			}
		}
		return field;
	}

}
