/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.spagobi.engines.datamining.compute;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.bo.DataMiningResult;
import it.eng.spagobi.engines.datamining.common.utils.DataMiningConstants;
import it.eng.spagobi.engines.datamining.model.Output;
import it.eng.spagobi.engines.datamining.model.Variable;
import it.eng.spagobi.hdfs.Hdfs;
import it.eng.spagobi.hdfs.HdfsUtilities;
import it.eng.spagobi.tools.dataset.bo.FileDataSet;
import it.eng.spagobi.tools.dataset.bo.HdfsDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.utils.DatasetMetadataParser;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.jpy.PyLib;
import org.jpy.PyModule;
import org.json.JSONArray;
import org.json.JSONObject;

public class PythonOutputExecutor {
	static private Logger logger = Logger.getLogger(PythonOutputExecutor.class);

	private static final String OUTPUT_PLOT_EXTENSION = "png";
	private static final String OUTPUT_PLOT_IMG = "png";
	private static String STORE_TO_HDFS = SpagoBIConstants.CONFIG_STORE_TO_HDFS;

	DataMiningEngineInstance dataminingInstance;
	IEngUserProfile profile;

	public PythonOutputExecutor(DataMiningEngineInstance dataminingInstance, IEngUserProfile profile) {
		this.dataminingInstance = dataminingInstance;
		this.profile = profile;
	}

	protected DataMiningResult evalOutput(Output out, PythonScriptExecutor scriptExecutor, String documentLabel, String userId) throws Exception {
		logger.debug("IN");
		// output -->if image and function --> execute function then prepare
		// output
		// output -->if script --> execute script then prepare output
		int resPythonExecution = 1;
		String codeToExec = null;

		DataMiningResult res = new DataMiningResult();
		if (!PyLib.isPythonRunning()) {
			res.setError("No Python instance found");
			return res;
		}
		List<Variable> variables = out.getVariables();
		logger.debug("Got variables list");
		// replace in function and in value attributes
		String function = out.getOutputFunction();
		if (function != null && variables != null && !variables.isEmpty()) {
			function = DataMiningUtils.replaceVariables(variables, function);
			logger.debug("Replaced variables in output function");
		}
		String outVal = out.getOutputValue();
		if (outVal != null && variables != null && !variables.isEmpty()) {
			outVal = DataMiningUtils.replaceVariables(variables, outVal);
			logger.debug("Replaced variables in output value");
		}

		if (out.getOutputType().equalsIgnoreCase(DataMiningConstants.IMAGE_OUTPUT) && out.getOutputName() != null) {
			logger.debug("Image output");

			// re.parseAndEval(getPlotFilePath(plotName));
			String strDir = DataMiningUtils.getUserResourcesPath(profile) + DataMiningConstants.DATA_MINING_TEMP_PATH_SUFFIX;
			File tempDir = new File(strDir);

			// if the directory does not exist, create it
			if (!tempDir.exists()) {
				logger.debug("creating directory: " + strDir);
				boolean result = false;

				try {
					tempDir.mkdirs();
					result = true;
				} catch (SecurityException se) {
					// handle it
				}
				if (result) {
					logger.debug(strDir + " created");
				}
			}

			// In case the system where Datamining engine is running isn't a X server (doesn't have $DISPLAY variable initialized)
			codeToExec = "import matplotlib\n" + "matplotlib.use('Agg')\n";
			PyLib.execScript(codeToExec);

			res.setVariablename(outVal);// could be multiple value
										// comma separated
			String plotName = out.getOutputName();

			codeToExec = "import os\n" + "os.chdir(r'" + strDir + "')\n";
			resPythonExecution = PyLib.execScript(codeToExec);

			if (resPythonExecution < 0) {
				logger.error("Python engine error \n" + "Technical details:\n" + "PythonOutputExecutor.java:\n" + codeToExec + "EXECUTION FAILED\n"
						+ "See log file for other details\n");
				throw new SpagoBIEngineRuntimeException("Python engine error \n" + "Technical details:\n" + "PythonOutputExecutor.java:\n" + codeToExec
						+ "EXECUTION FAILED\n" + "See log file for other details\n");
				// res.setError(e.getMessage());
				// return res;
			}

			logger.debug("Plot file name " + plotName);

			// function recalling a function inside the main script (auto)
			// to produce an image result
			if (function == null) {
				codeToExec = "temporaryPlotVariableToPrintOnFile=" + out.getOuputLabel() + "\n" + "temporaryPlotVariableToPrintOnFile.savefig('" + plotName
						+ "." + OUTPUT_PLOT_EXTENSION + "')\n";
				resPythonExecution = PyLib.execScript(codeToExec);
				if (resPythonExecution < 0) {
					SpagoBIRuntimeException e = new SpagoBIRuntimeException("Python engine error \n" + "Technical details:\n" + "PythonOutputExecutor.java:\n"
							+ codeToExec + "EXECUTION FAILED\n" + "See log file for other details\n");
					e.printStackTrace();
					logger.error(e);
					res.setError(e.getMessage());
					return res;
				}
			} else if (function != null) {
				if (outVal == null || outVal.equals("")) {
					codeToExec = "temporaryPlotVariableToPrintOnFile=" + function + "\n" + "temporaryPlotVariableToPrintOnFile.savefig('" + plotName + "."
							+ OUTPUT_PLOT_EXTENSION + "')\n";
					resPythonExecution = PyLib.execScript(codeToExec);
					if (resPythonExecution < 0) {
						logger.error("Python engine error \n" + "Technical details:\n" + "PythonOutputExecutor.java:\n" + codeToExec + "EXECUTION FAILED\n"
								+ "See log file for other details\n");
						throw new SpagoBIEngineRuntimeException("Python engine error \n" + "Technical details:\n" + "PythonOutputExecutor.java:\n" + codeToExec
								+ "EXECUTION FAILED\n" + "See log file for other details\n");
					}
				} else {
					codeToExec = "temporaryPlotVariableToPrintOnFile=" + function + "(" + outVal + ")\n" + "temporaryPlotVariableToPrintOnFile.savefig('"
							+ plotName + "." + OUTPUT_PLOT_EXTENSION + "')\n";
					resPythonExecution = PyLib.execScript(codeToExec);
					if (resPythonExecution < 0) {
						logger.error("Python engine error \n" + "Technical details:\n" + "PythonOutputExecutor.java:\n" + codeToExec + "EXECUTION FAILED\n"
								+ "See log file for other details\n");
						throw new SpagoBIEngineRuntimeException("Python engine error \n" + "Technical details:\n" + "PythonOutputExecutor.java:\n" + codeToExec
								+ "EXECUTION FAILED\n" + "See log file for other details\n");
					}

				}
			}
			logger.debug("Evaluated dev.off()");
			res.setOutputType(out.getOutputType());
			String resImg = getPlotImageAsBase64(out.getOutputName());
			res.setPlotName(plotName);
			if (resImg != null && !resImg.equals("")) {
				res.setResult(resImg);
				scriptExecutor.deleteTemporarySourceScript(DataMiningUtils.getUserResourcesPath(profile) + DataMiningConstants.DATA_MINING_TEMP_PATH_SUFFIX
						+ plotName + "." + OUTPUT_PLOT_EXTENSION);
				logger.debug("Deleted temp image");
			}

		} else if (out.getOutputType().equalsIgnoreCase(DataMiningConstants.TEXT_OUTPUT) && outVal != null && out.getOutputName() != null) {
			logger.debug("Text output");

			if (function != null && function.length() > 0) {
				if (outVal == null || outVal.equals("")) {
					outVal = out.getOuputLabel();
					res.setVariablename(outVal);// could be multiple value

					codeToExec = outVal + "=" + function + "\n" + outVal + "=str(" + outVal + ")\n";
					resPythonExecution = PyLib.execScript(codeToExec); // to get output as a String
					if (resPythonExecution < 0) {

						logger.error("Python engine error \n" + "Technical details:\n" + "PythonOutputExecutor.java:\n" + codeToExec + "EXECUTION FAILED\n"
								+ "See log file for other details\n");
						throw new SpagoBIEngineRuntimeException("Python engine error \n" + "Technical details:\n" + "PythonOutputExecutor.java:\n" + codeToExec
								+ "EXECUTION FAILED\n" + "See log file for other details\n");
					}

					String pythonResult = PyModule.getMain().getAttribute(outVal).getStringValue();
					res.setOutputType(out.getOutputType());
					res.setResult("" + pythonResult);
				} else {
					// res.setVariablename(outVal);// could be multiple value
					String noArgFunctionExecuted = function + "(" + outVal + ")";
					res.setVariablename(noArgFunctionExecuted);
					codeToExec = outVal + "=" + function + "(" + outVal + ")" + "\n" + outVal + "=str(" + outVal + ")";
					resPythonExecution = PyLib.execScript(codeToExec); // to get output as a String
					if (resPythonExecution < 0) {
						logger.error("Python engine error \n" + "Technical details:\n" + "PythonOutputExecutor.java:\n" + codeToExec + "EXECUTION FAILED\n"
								+ "See log file for other details\n");
						throw new SpagoBIEngineRuntimeException("Python engine error \n" + "Technical details:\n" + "PythonOutputExecutor.java:\n" + codeToExec
								+ "EXECUTION FAILED\n" + "See log file for other details\n");
					}

					String pythonResult = PyModule.getMain().getAttribute(outVal).getStringValue();
					res.setOutputType(out.getOutputType());
					res.setResult("" + pythonResult);

				}

			} else { // function="" or no function (simple case)
				res.setVariablename(outVal);// could be multiple value
				codeToExec = outVal + "=str(" + outVal + ")";
				resPythonExecution = PyLib.execScript(codeToExec); // to get output as a String
				if (resPythonExecution < 0) {
					logger.error("Python engine error \n" + "Technical details:\n" + "PythonOutputExecutor.java:\n" + codeToExec + "EXECUTION FAILED\n"
							+ "See log file for other details\n");
					throw new SpagoBIEngineRuntimeException("Python engine error \n" + "Technical details:\n" + "PythonOutputExecutor.java:\n" + codeToExec
							+ "EXECUTION FAILED\n" + "See log file for other details\n");
				}
				String pythonResult = PyModule.getMain().getAttribute(outVal).getStringValue();
				res.setOutputType(out.getOutputType());
				res.setResult("" + pythonResult);
			}

			// comma separated

			logger.debug("Evaluated result");
		} else if ((out.getOutputType().equalsIgnoreCase(DataMiningConstants.DATASET) || out.getOutputType().equalsIgnoreCase(DataMiningConstants.SPAGOBI_DS)
				|| out.getOutputType().equalsIgnoreCase("SpagoBI Dataset") || out.getOutputType().equalsIgnoreCase("Dataset"))
				&& outVal != null && out.getOutputName() != null) {
			logger.debug("Dataset output");
			String pythonResult = null;
			CreateDatasetResult creationResult = null;

			if (function != null && function.length() > 0) {
				if (outVal == null || outVal.equals("")) {
					outVal = out.getOuputLabel();
					res.setVariablename(outVal);// could be multiple value
					creationResult = createAndPersistDatasetProductByFunction(profile, outVal, out, function, userId, documentLabel);
					if (creationResult.getPythonExecutionError() < 0) {
						logger.error("Python engine error \n" + "Technical details:\n" + "PythonOutputExecutor.java:\n"
								+ creationResult.getPythonExecutionCodeWithError() + "EXECUTION FAILED\n" + "See log file for other details\n");
						throw new SpagoBIEngineRuntimeException("Python engine error \n" + "Technical details:\n" + "PythonOutputExecutor.java:\n"
								+ creationResult.getPythonExecutionCodeWithError() + "EXECUTION FAILED\n" + "See log file for other details\n");
					}

					codeToExec = outVal + "=" + outVal + ".to_json()\n";
					resPythonExecution = PyLib.execScript(codeToExec); // to get output as a String
					if (resPythonExecution < 0) {
						logger.error("Python engine error \n" + "Technical details:\n" + "PythonOutputExecutor.java:\n" + codeToExec + "EXECUTION FAILED\n"
								+ "See log file for other details\n");
						throw new SpagoBIEngineRuntimeException("Python engine error \n" + "Technical details:\n" + "PythonOutputExecutor.java:\n" + codeToExec
								+ "EXECUTION FAILED\n" + "See log file for other details\n");
					}

					pythonResult = PyModule.getMain().getAttribute(outVal).getStringValue();
				} else {
					// res.setVariablename(outVal);// could be multiple value
					String noArgFunctionExecuted = function + "(" + outVal + ")";
					res.setVariablename(noArgFunctionExecuted);
					creationResult = createAndPersistDatasetProductByFunction(profile, outVal, out, noArgFunctionExecuted, userId, documentLabel);

					if (creationResult.getPythonExecutionError() < 0) {
						logger.error("Python engine error \n" + "Technical details:\n" + "PythonOutputExecutor.java:\n"
								+ creationResult.getPythonExecutionCodeWithError() + "EXECUTION FAILED\n" + "See log file for other details\n");
						throw new SpagoBIEngineRuntimeException("Python engine error \n" + "Technical details:\n" + "PythonOutputExecutor.java:\n"
								+ creationResult.getPythonExecutionCodeWithError() + "EXECUTION FAILED\n" + "See log file for other details\n");
					}

					// PyLib.execScript(outVal + "=" + function + "(" + outVal + ")");
					// PyLib.execScript(outVal + "=" + outVal + ".to_json()\n"); // to get output as a String
					pythonResult = PyModule.getMain().getAttribute(outVal).getStringValue();
				}

			} else { // function="" or no function (simple case)
				res.setVariablename(outVal);// could be multiple value
				creationResult = createAndPersistDataset(profile, outVal, out, userId, documentLabel);
				if (creationResult.getPythonExecutionError() < 0) {
					logger.error("Python engine error \n" + "Technical details:\n" + "PythonOutputExecutor.java:\n"
							+ creationResult.getPythonExecutionCodeWithError() + "EXECUTION FAILED\n" + "See log file for other details\n");
					throw new SpagoBIEngineRuntimeException("Python engine error \n" + "Technical details:\n" + "PythonOutputExecutor.java:\n"
							+ creationResult.getPythonExecutionCodeWithError() + "EXECUTION FAILED\n" + "See log file for other details\n");
				}
				codeToExec = outVal + "=" + outVal + ".to_json()\n";
				resPythonExecution = PyLib.execScript(outVal + "=" + outVal + ".to_json()\n"); // to get output as a String
				if (resPythonExecution < 0) {
					logger.error("Python engine error \n" + "Technical details:\n" + "PythonOutputExecutor.java:\n" + codeToExec + "EXECUTION FAILED\n"
							+ "See log file for other details\n");
					throw new SpagoBIEngineRuntimeException("Python engine error \n" + "Technical details:\n" + "PythonOutputExecutor.java:\n" + codeToExec
							+ "EXECUTION FAILED\n" + "See log file for other details\n");
				}

				pythonResult = PyModule.getMain().getAttribute(outVal).getStringValue();
			}

			res.setOutputType("Dataset"); // or DataMiningConstants.DATASET
			// res.setResult("" + pythonResult); //return Json
			// res.setResult("SpagoBi dataset saved, visible from Data Set section in Document Browser, with label :" + creationResult.getDatasetlabel());
			res.setResult(creationResult.getDatasetlabel()); // returns only the label
			logger.debug("Evaluated result");

		}
		logger.debug("OUT");
		return res;
	}

	private String getPlotFilePath(String plotName) throws IOException {
		logger.debug("IN");
		String path = null;
		if (plotName != null && !plotName.equals("")) {
			String filePath = DataMiningUtils.getUserResourcesPath(profile).replaceAll("\\\\", "/");
			path = OUTPUT_PLOT_IMG + "(\"" + filePath + DataMiningConstants.DATA_MINING_TEMP_FOR_SCRIPT + plotName + "." + OUTPUT_PLOT_EXTENSION + "\") ";
		}
		logger.debug("OUT");
		return path;
	}

	public String getPlotImageAsBase64(String plotName) throws IOException {
		logger.debug("IN");
		String fileImg = DataMiningUtils.getUserResourcesPath(profile) + DataMiningConstants.DATA_MINING_TEMP_PATH_SUFFIX + plotName + "."
				+ OUTPUT_PLOT_EXTENSION;
		logger.warn(fileImg);
		BufferedImage img = null;
		String imgstr = null;

		try {
			File imgFromPlot = new File(fileImg);
			if (!imgFromPlot.exists()) {
				logger.warn("Image not produced!");
				return imgstr;
			}
			img = ImageIO.read(imgFromPlot);
			imgstr = encodeToString(img, OUTPUT_PLOT_EXTENSION);
		} catch (IOException ioe) {
			throw ioe;
		}
		logger.debug("OUT");
		return imgstr;

	}

	private static String encodeToString(BufferedImage image, String type) {
		logger.debug("IN");
		String imageString = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			ImageIO.write(image, type, bos);
			byte[] imageBytes = bos.toByteArray();

			Base64 encoder = new Base64();
			imageString = encoder.encodeBase64String(imageBytes);

			bos.close();
		} catch (IOException e) {
			logger.error(e);
		}
		logger.debug("OUT");
		return imageString;
	}

	private static CreateDatasetResult createAndPersistDataset(IEngUserProfile profile, String outVal, Output out, String userId, String documentLabel)
			throws Exception {
		logger.debug("IN");

		int resPythonExecution = 1;
		String codeToExec = null;

		CreateDatasetResult createDatasetResult = new CreateDatasetResult();
		String spagoBiDatasetname = userId + "_" + documentLabel + "_" + out.getOuputLabel();
		IConfigDAO configsDAO = DAOFactory.getSbiConfigDAO();
		Config conf = configsDAO.loadConfigParametersByLabel(STORE_TO_HDFS);
		boolean storeToHDFS = Boolean.valueOf(conf.getValueCheck()).booleanValue();
		FileDataSet dataSet;
		if (storeToHDFS) {
			dataSet = new HdfsDataSet();
			dataSet.setPersistedHDFS(true);
		} else {
			dataSet = new FileDataSet();
		}
		String path = getDatasetsDirectoryPath(); // E.G. C:\Users\piovani\apache-tomcat-7.0.67-Trunk\resources\DEFAULT_TENANT\dataset\files
		String resPath = DAOConfig.getResourcePath(); // E.G. C:\Users\piovani\apache-tomcat-7.0.67-Trunk\resources\DEFAULT_TENANT\
		dataSet.setResourcePath(resPath);

		JSONObject configurationObj = new JSONObject();

		configurationObj.put(DataSetConstants.FILE_TYPE, "CSV");
		configurationObj.put(DataSetConstants.CSV_FILE_DELIMITER_CHARACTER, ",");
		configurationObj.put(DataSetConstants.CSV_FILE_QUOTE_CHARACTER, "'");
		configurationObj.put(DataSetConstants.FILE_NAME, spagoBiDatasetname + ".csv");
		configurationObj.put("encoding", "UTF-8");
		configurationObj.put(DataSetConstants.XSL_FILE_SKIP_ROWS, DataSetConstants.XSL_FILE_SKIP_ROWS);
		configurationObj.put(DataSetConstants.XSL_FILE_LIMIT_ROWS, DataSetConstants.XSL_FILE_LIMIT_ROWS);
		configurationObj.put(DataSetConstants.XSL_FILE_SHEET_NUMBER, DataSetConstants.XSL_FILE_SHEET_NUMBER);

		String confString = configurationObj.toString();
		dataSet.setConfiguration(confString);

		codeToExec = "import os\n" + "import pandas\n" + "os.chdir(r'" + path + "')\n" + outVal + "=" + "pandas.DataFrame(" + outVal + ")\n" + outVal
				+ ".to_csv('" + spagoBiDatasetname + ".csv'" + ",index=False , quotechar=\"'\")\n";

		resPythonExecution = PyLib.execScript(codeToExec);
		if (resPythonExecution < 0) {
			createDatasetResult.setPythonExecutionError(resPythonExecution);
			createDatasetResult.setPythonExecutionCodeWithError(codeToExec);
			return createDatasetResult;
		}

		// dataSet.setFileName(outVal + ".csv");
		dataSet.setFileName(spagoBiDatasetname + ".csv");
		dataSet.setFileType("CSV");
		dataSet.setDsType(DataSetConstants.DS_FILE);

		dataSet.setLabel(spagoBiDatasetname);
		dataSet.setName(spagoBiDatasetname);
		dataSet.setDescription("Dataset created from execution of document " + documentLabel + " by user " + userId);
		dataSet.setOwner(profile.getUserUniqueIdentifier().toString());

		// ------------Metadata setting------------

		dataSet.loadData();
		IDataStore dataStore = dataSet.getDataStore();
		JSONArray metadataArray = new JSONArray();

		IMetaData metaData = dataStore.getMetaData();
		for (int i = 0; i < metaData.getFieldCount(); i++) {
			IFieldMetaData ifmd = metaData.getFieldMeta(i);
			for (int j = 0; j < metadataArray.length(); j++) {
				if (ifmd.getName().equals((metadataArray.getJSONObject(j)).getString("name"))) {
					if ("MEASURE".equals((metadataArray.getJSONObject(j)).getString("fieldType"))) {
						ifmd.setFieldType(IFieldMetaData.FieldType.MEASURE);
					} else {
						ifmd.setFieldType(IFieldMetaData.FieldType.ATTRIBUTE);
					}
					break;
				}
			}
		}
		IMetaData currentMetadata = dataStore.getMetaData();
		DatasetMetadataParser dsp = new DatasetMetadataParser();
		String dsMetadata = dsp.metadataToXML(currentMetadata);
		dataSet.setDsMetadata(dsMetadata);

		// ----------------------------------------

		IDataSetDAO dataSetDAO = DAOFactory.getDataSetDAO();
		dataSetDAO.setUserProfile(profile);

		logger.debug("check if dataset with label " + spagoBiDatasetname + " is already present");

		if (storeToHDFS) {
			moveToHdfs((HdfsDataSet) dataSet);
		}
		// check label is already present; insert or modify dependently
		IDataSet iDataSet = dataSetDAO.loadDataSetByLabel(spagoBiDatasetname);

		// loadActiveDataSetByLabel(label);
		if (iDataSet != null) {
			logger.debug("a dataset with label " + spagoBiDatasetname + " is already present: modify it");
			dataSet.setId(iDataSet.getId());
			dataSetDAO.modifyDataSet(dataSet);
		} else {
			logger.debug("No dataset with label " + spagoBiDatasetname + " is already present: insert it");
			dataSetDAO.insertDataSet(dataSet);

		}
		createDatasetResult.setDatasetlabel(spagoBiDatasetname);
		return createDatasetResult;

	}

	private static CreateDatasetResult createAndPersistDatasetProductByFunction(IEngUserProfile profile, String outVal, Output out, String function,
			String userId, String documentLabel) throws Exception {
		logger.debug("IN");
		String codeToExec = null;
		int resPythonExecution = 1;
		CreateDatasetResult creationResult = new CreateDatasetResult();
		DataMiningResult res = new DataMiningResult();

		IConfigDAO configsDAO = DAOFactory.getSbiConfigDAO();
		Config conf = configsDAO.loadConfigParametersByLabel(STORE_TO_HDFS);
		boolean storeToHDFS = Boolean.valueOf(conf.getValueCheck()).booleanValue();
		FileDataSet dataSet;
		if (storeToHDFS) {
			dataSet = new HdfsDataSet();
			dataSet.setPersistedHDFS(true);
		} else {
			dataSet = new FileDataSet();
		}

		String path = getDatasetsDirectoryPath(); // E.G. C:\Users\piovani\apache-tomcat-7.0.67-Trunk\resources\DEFAULT_TENANT\dataset\files
		String resPath = DAOConfig.getResourcePath(); // E.G. C:\Users\piovani\apache-tomcat-7.0.67-Trunk\resources\DEFAULT_TENANT\
		dataSet.setResourcePath(resPath);

		String spagoBiDatasetname = userId + "_" + documentLabel + "_" + out.getOuputLabel();
		JSONObject configurationObj = new JSONObject();
		// configurationObj.put("fileType", "CSV");
		// configurationObj.put("csvDelimiter", ",");
		// configurationObj.put("csvQuote", "'"); // Alternativa "\""
		// configurationObj.put("fileName", spagoBiDatasetname + ".csv");
		// configurationObj.put("encoding", "UTF-8");

		configurationObj.put(DataSetConstants.FILE_TYPE, "CSV");
		configurationObj.put(DataSetConstants.CSV_FILE_DELIMITER_CHARACTER, ",");
		configurationObj.put(DataSetConstants.CSV_FILE_QUOTE_CHARACTER, "'");
		configurationObj.put(DataSetConstants.FILE_NAME, spagoBiDatasetname + ".csv");
		configurationObj.put("encoding", "UTF-8");
		configurationObj.put(DataSetConstants.XSL_FILE_SKIP_ROWS, DataSetConstants.XSL_FILE_SKIP_ROWS);
		configurationObj.put(DataSetConstants.XSL_FILE_LIMIT_ROWS, DataSetConstants.XSL_FILE_LIMIT_ROWS);
		configurationObj.put(DataSetConstants.XSL_FILE_SHEET_NUMBER, DataSetConstants.XSL_FILE_SHEET_NUMBER);

		String confString = configurationObj.toString();
		dataSet.setConfiguration(confString);

		codeToExec = "import os\n" + "import pandas\n" + "os.chdir(r'" + path + "')\n" + outVal + "=" + "pandas.DataFrame(" + function + ")\n" + outVal + "="
				+ "pandas.DataFrame(" + function + ")\n" + outVal + ".to_csv('" + spagoBiDatasetname + ".csv'" + ",index=False, quotechar=\"'\")\n";

		resPythonExecution = PyLib.execScript(codeToExec);

		if (resPythonExecution < 0) {
			creationResult.setPythonExecutionError(resPythonExecution);
			creationResult.setPythonExecutionCodeWithError(codeToExec);
			return creationResult;
		}

		dataSet.setFileName(spagoBiDatasetname + ".csv");
		dataSet.setFileType("CSV");
		dataSet.setDsType(DataSetConstants.DS_FILE);

		String label = out.getOuputLabel();
		dataSet.setLabel(spagoBiDatasetname);
		dataSet.setName(spagoBiDatasetname);
		dataSet.setDescription("Dataset created from execution of document " + documentLabel + " by user " + userId);
		dataSet.setOwner(profile.getUserUniqueIdentifier().toString());

		// ------------Metadata setting------------

		dataSet.loadData();
		IDataStore dataStore = dataSet.getDataStore();
		JSONArray metadataArray = new JSONArray();

		IMetaData metaData = dataStore.getMetaData();
		for (int i = 0; i < metaData.getFieldCount(); i++) {
			IFieldMetaData ifmd = metaData.getFieldMeta(i);
			for (int j = 0; j < metadataArray.length(); j++) {
				if (ifmd.getName().equals((metadataArray.getJSONObject(j)).getString("name"))) {
					if ("MEASURE".equals((metadataArray.getJSONObject(j)).getString("fieldType"))) {
						ifmd.setFieldType(IFieldMetaData.FieldType.MEASURE);
					} else {
						ifmd.setFieldType(IFieldMetaData.FieldType.ATTRIBUTE);
					}
					break;
				}
			}
		}
		IMetaData currentMetadata = dataStore.getMetaData();
		DatasetMetadataParser dsp = new DatasetMetadataParser();
		String dsMetadata = dsp.metadataToXML(currentMetadata);
		dataSet.setDsMetadata(dsMetadata);

		// ----------------------------------------

		IDataSetDAO dataSetDAO = DAOFactory.getDataSetDAO();
		dataSetDAO.setUserProfile(profile);

		logger.debug("check if dataset with label " + spagoBiDatasetname + " is already present");

		if (storeToHDFS) {
			moveToHdfs((HdfsDataSet) dataSet);
		}
		// check label is already present; insert or modify dependently
		IDataSet iDataSet = dataSetDAO.loadDataSetByLabel(spagoBiDatasetname);

		// loadActiveDataSetByLabel(label);
		if (iDataSet != null) {
			logger.debug("a dataset with label " + spagoBiDatasetname + " is already present: modify it");
			dataSet.setId(iDataSet.getId());
			dataSetDAO.modifyDataSet(dataSet);
		} else {
			logger.debug("No dataset with label " + spagoBiDatasetname + " is already present: insert it");
			dataSetDAO.insertDataSet(dataSet);

		}
		creationResult.setDatasetlabel(spagoBiDatasetname);
		return creationResult;

	}

	public static String getDatasetsDirectoryPath() {

		String datasetDirPath = DAOConfig.getResourcePath();
		datasetDirPath += File.separatorChar + "dataset" + File.separatorChar + "files";

		File file = new File(datasetDirPath);
		if (!file.exists()) {
			if (file.mkdirs()) {
				logger.debug("Directory is created!");
			} else {
				logger.debug("Failed to create directory!");
			}
		}

		return datasetDirPath.replace("\\", "/");
	}

	public static void moveToHdfs(HdfsDataSet dataSet) {
		Hdfs hdfs = dataSet.getHdfs();
		String sep = HdfsUtilities.getHdfsSperator();
		String resourcePath = hdfs.getWorkingDirectory();
		String dstPath = resourcePath + sep + "dataset" + sep + "files" + sep + dataSet.getFileName();
		hdfs.moveFromLocalFile(getDatasetsDirectoryPath() + File.separator + dataSet.getFileName(), dstPath);
		dataSet.setResourcePath(resourcePath);
	}

}
