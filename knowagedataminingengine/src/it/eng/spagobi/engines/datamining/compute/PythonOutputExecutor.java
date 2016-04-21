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
import it.eng.spagobi.commons.dao.DAOConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.bo.DataMiningResult;
import it.eng.spagobi.engines.datamining.common.utils.DataMiningConstants;
import it.eng.spagobi.engines.datamining.model.Output;
import it.eng.spagobi.engines.datamining.model.Variable;
import it.eng.spagobi.tools.dataset.bo.FileDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;

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
import org.json.JSONObject;

public class PythonOutputExecutor {
	static private Logger logger = Logger.getLogger(PythonOutputExecutor.class);

	private static final String OUTPUT_PLOT_EXTENSION = "png";
	private static final String OUTPUT_PLOT_IMG = "png";

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
			res.setVariablename(outVal);// could be multiple value
										// comma separated
			String plotName = out.getOutputName();
			// re.parseAndEval(getPlotFilePath(plotName));
			String strDir = DataMiningUtils.getUserResourcesPath(profile) + DataMiningConstants.DATA_MINING_TEMP_PATH_SUFFIX;
			resPythonExecution = PyLib.execScript("import os\n" + "os.chdir(r'" + strDir + "')\n");

			if (resPythonExecution < 0) {
				res.setError("Python error");
				return res;
			}

			logger.debug("Plot file name " + plotName);

			// function recalling a function inside the main script (auto)
			// to produce an image result

			if (outVal == null || outVal.equals("")) {
				PyLib.execScript("temporaryPlotVariableToPrintOnFile=" + function + "\n");
				resPythonExecution = PyLib.execScript("temporaryPlotVariableToPrintOnFile.savefig('" + plotName + "." + OUTPUT_PLOT_EXTENSION + "')\n");
				if (resPythonExecution < 0) {
					res.setError("Python error");
					return res;
				}
			} else {
				PyLib.execScript("temporaryPlotVariableToPrintOnFile=" + function + "(" + outVal + ")\n");
				resPythonExecution = PyLib.execScript("temporaryPlotVariableToPrintOnFile.savefig('" + plotName + "." + OUTPUT_PLOT_EXTENSION + "')\n");
				if (resPythonExecution < 0) {
					res.setError("Python error");
					return res;
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
			/*
			 * } }
			 */

		} else if (out.getOutputType().equalsIgnoreCase(DataMiningConstants.TEXT_OUTPUT) && outVal != null && out.getOutputName() != null) {
			logger.debug("Text output");

			if (function != null && function.length() > 0) {
				if (outVal == null || outVal.equals("")) {
					outVal = out.getOuputLabel();
					res.setVariablename(outVal);// could be multiple value

					PyLib.execScript(outVal + "=" + function);
					resPythonExecution = PyLib.execScript(outVal + "=str(" + outVal + ")"); // to get output as a String
					if (resPythonExecution < 0) {
						res.setError("Python error");
						return res;
					}

					String pythonResult = PyModule.getMain().getAttribute(outVal).getStringValue();
					res.setOutputType(out.getOutputType());
					res.setResult("" + pythonResult);
				} else {
					// res.setVariablename(outVal);// could be multiple value
					String noArgFunctionExecuted = function + "(" + outVal + ")";
					res.setVariablename(noArgFunctionExecuted);

					PyLib.execScript(outVal + "=" + function + "(" + outVal + ")");
					resPythonExecution = PyLib.execScript(outVal + "=str(" + outVal + ")"); // to get output as a String
					if (resPythonExecution < 0) {
						res.setError("Python error");
						return res;
					}

					String pythonResult = PyModule.getMain().getAttribute(outVal).getStringValue();
					res.setOutputType(out.getOutputType());
					res.setResult("" + pythonResult);

				}

			} else { // function="" or no function (simple case)
				res.setVariablename(outVal);// could be multiple value
				resPythonExecution = PyLib.execScript(outVal + "=str(" + outVal + ")"); // to get output as a String
				if (resPythonExecution < 0) {
					res.setError("Python error");
					return res;
				}
				String pythonResult = PyModule.getMain().getAttribute(outVal).getStringValue();
				res.setOutputType(out.getOutputType());
				res.setResult("" + pythonResult);
			}

			// comma separated

			logger.debug("Evaluated result");
		} else if (out.getOutputType().equalsIgnoreCase(DataMiningConstants.SPAGOBI_DS_OUTPUT) && outVal != null && out.getOutputName() != null) {
			logger.debug("SpagoBI output");
			String pythonResult = null;

			if (function != null && function.length() > 0) {
				if (outVal == null || outVal.equals("")) {
					outVal = out.getOuputLabel();
					res.setVariablename(outVal);// could be multiple value
					resPythonExecution = createAndPersistDatasetProductByFunction(profile, outVal, out, function, userId, documentLabel);
					if (resPythonExecution < 0) {
						res.setError("Python error");
						return res;
					}

					resPythonExecution = PyLib.execScript(outVal + "=" + outVal + ".to_json()\n"); // to get output as a String
					if (resPythonExecution < 0) {
						res.setError("Python error");
						return res;
					}

					pythonResult = PyModule.getMain().getAttribute(outVal).getStringValue();
				} else {
					// res.setVariablename(outVal);// could be multiple value
					String noArgFunctionExecuted = function + "(" + outVal + ")";
					res.setVariablename(noArgFunctionExecuted);
					resPythonExecution = createAndPersistDatasetProductByFunction(profile, outVal, out, noArgFunctionExecuted, userId, documentLabel);

					if (resPythonExecution < 0) {
						res.setError("Python error");
						return res;
					}

					// PyLib.execScript(outVal + "=" + function + "(" + outVal + ")");
					// PyLib.execScript(outVal + "=" + outVal + ".to_json()\n"); // to get output as a String
					pythonResult = PyModule.getMain().getAttribute(outVal).getStringValue();
				}

			} else { // function="" or no function (simple case)
				res.setVariablename(outVal);// could be multiple value
				resPythonExecution = createAndPersistDataset(profile, outVal, out, userId, documentLabel);
				if (resPythonExecution < 0) {
					res.setError("Python error");
					return res;
				}
				resPythonExecution = PyLib.execScript(outVal + "=" + outVal + ".to_json()\n"); // to get output as a String
				if (resPythonExecution < 0) {
					res.setError("Python error");
					return res;
				}

				pythonResult = PyModule.getMain().getAttribute(outVal).getStringValue();
			}

			res.setOutputType("text");
			// res.setResult("" + pythonResult); //return Json
			res.setResult("SpagoBi dataset saved, visible from Data Set section in Document Browser");
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

	private static int createAndPersistDataset(IEngUserProfile profile, String outVal, Output out, String userId, String documentLabel) throws Exception {
		logger.debug("IN");

		int resPythonExecution = 1;
		String spagoBiDatasetname = userId + "_" + documentLabel + "_" + out.getOuputLabel();

		FileDataSet dataSet = new FileDataSet();
		String path = getDatasetsDirectoryPath();
		dataSet.setResourcePath(path);// (DAOConfig.getResourcePath());

		JSONObject configurationObj = new JSONObject();
		configurationObj.put("fileType", "CSV");
		configurationObj.put("csvDelimiter", ",");
		configurationObj.put("csvQuote", "'"); // Alternativa "\""
		configurationObj.put("fileName", spagoBiDatasetname + ".csv");
		// configurationObj.put("fileName", outVal + ".csv");
		configurationObj.put("encoding", "UTF-8");
		String confString = configurationObj.toString();
		dataSet.setConfiguration(confString);

		PyLib.execScript("import os\n" + "import pandas\n" + "os.chdir(r'" + path + "')\n");
		PyLib.execScript(outVal + "=" + "pandas.DataFrame(" + outVal + ")\n");
		resPythonExecution = PyLib.execScript(outVal + ".to_csv('" + spagoBiDatasetname + ".csv'" + ",index=False)\n");
		if (resPythonExecution < 0) {
			return resPythonExecution;
		}

		// dataSet.setFileName(outVal + ".csv");
		dataSet.setFileName(spagoBiDatasetname + ".csv");
		dataSet.setFileType("CSV");
		dataSet.setDsType(DataSetConstants.DS_FILE);

		dataSet.setLabel(spagoBiDatasetname);
		dataSet.setName(spagoBiDatasetname);
		dataSet.setDescription("Dataset created from execution of document " + documentLabel + " by user " + userId);
		dataSet.setOwner(profile.getUserUniqueIdentifier().toString());

		IDataSetDAO dataSetDAO = DAOFactory.getDataSetDAO();
		dataSetDAO.setUserProfile(profile);

		logger.debug("check if dataset with label " + spagoBiDatasetname + " is already present");

		// check label is already present; insert or modify dependengly
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

		return resPythonExecution;

	}

	private static int createAndPersistDatasetProductByFunction(IEngUserProfile profile, String outVal, Output out, String function, String userId,
			String documentLabel) throws Exception {
		logger.debug("IN");
		int resPythonExecution = 1;
		DataMiningResult res = new DataMiningResult();

		FileDataSet dataSet = new FileDataSet();
		String path = getDatasetsDirectoryPath();
		dataSet.setResourcePath(path);// (DAOConfig.getResourcePath());

		String spagoBiDatasetname = userId + "_" + documentLabel + "_" + out.getOuputLabel();

		JSONObject configurationObj = new JSONObject();
		configurationObj.put("fileType", "CSV");
		configurationObj.put("csvDelimiter", ",");
		configurationObj.put("csvQuote", "'"); // Alternativa "\""
		configurationObj.put("fileName", spagoBiDatasetname + ".csv");
		configurationObj.put("encoding", "UTF-8");
		String confString = configurationObj.toString();
		dataSet.setConfiguration(confString);

		PyLib.execScript("import os\n" + "import pandas\n" + "os.chdir(r'" + path + "')\n");
		PyLib.execScript(outVal + "=" + "pandas.DataFrame(" + function + ")\n");
		resPythonExecution = PyLib.execScript(outVal + ".to_csv('" + spagoBiDatasetname + ".csv'" + ",index=False)\n");

		if (resPythonExecution < 0) {
			return resPythonExecution;
		}

		dataSet.setFileName(spagoBiDatasetname + ".csv");
		dataSet.setFileType("CSV");
		dataSet.setDsType(DataSetConstants.DS_FILE);

		String label = out.getOuputLabel();
		dataSet.setLabel(spagoBiDatasetname);
		dataSet.setName(spagoBiDatasetname);
		dataSet.setDescription("Dataset created from execution of document " + documentLabel + " by user " + userId);
		dataSet.setOwner(profile.getUserUniqueIdentifier().toString());

		IDataSetDAO dataSetDAO = DAOFactory.getDataSetDAO();
		dataSetDAO.setUserProfile(profile);

		logger.debug("check if dataset with label " + spagoBiDatasetname + " is already present");

		// check label is already present; insert or modify dependengly
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

		return resPythonExecution;

	}

	public static String getDatasetsDirectoryPath() {

		String datasetDirPath = DAOConfig.getResourcePath();
		datasetDirPath += File.separatorChar + "dataset" + File.separatorChar + "files";

		File file = new File(datasetDirPath);
		if (!file.exists()) {
			if (file.mkdirs()) {
				System.out.println("Directory is created!");
			} else {
				System.out.println("Failed to create directory!");
			}
		}

		return datasetDirPath.replace("\\", "/");
	}

}
