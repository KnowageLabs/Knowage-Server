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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngine;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.bo.DataMiningResult;
import it.eng.spagobi.engines.datamining.common.utils.DataMiningConstants;
import it.eng.spagobi.engines.datamining.model.Output;
import it.eng.spagobi.engines.datamining.model.Variable;
import it.eng.spagobi.tools.dataset.bo.FileDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.utils.DatasetMetadataParser;

public class ROutputExecutor {
	static private Logger logger = Logger.getLogger(ROutputExecutor.class);

	private static final String OUTPUT_PLOT_EXTENSION = "png";
	private static final String OUTPUT_PLOT_IMG = "png";

	private REngine re;
	DataMiningEngineInstance dataminingInstance;
	IEngUserProfile profile;

	public ROutputExecutor(DataMiningEngineInstance dataminingInstance, IEngUserProfile profile) {
		this.dataminingInstance = dataminingInstance;
		this.profile = profile;
	}

	public REngine getRe() {
		return re;
	}

	public void setRe(REngine re) {
		this.re = re;
	}

	protected DataMiningResult evalOutput(Output out, RScriptExecutor scriptExecutor, String documentLabel, String userId) throws Exception {
		logger.debug("IN");
		// output -->if image and function --> execute function then prepare
		// output
		// output -->if script --> execute script then prepare output

		DataMiningResult res = new DataMiningResult();
		if (re == null) {
			res.setError("No R instance found");
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

		String noArgFunctionExecuted = "";

		if (out.getOutputType().equalsIgnoreCase(DataMiningConstants.IMAGE_OUTPUT) && out.getOutputName() != null) {
			logger.debug("Image output");
			REXP rexp = null;

			// String imgWidth = out.getOutputImgWidth() == null ? "300" : out.getOutputImgWidth();
			// String imgHeight = out.getOutputImgHeight() == null ? "300" : out.getOutputImgHeight();

			String imgWidth = out.getOutputImgWidth();
			String imgHeight = out.getOutputImgHeight();

			// String rSetImgSize = "dev.new(width=" + imgWidth + ", height=" + imgHeight + ")";

			// rexp = re.parseAndEval("try( png(width = " + imgWidth + ", height = " + imgHeight + ") )");

			res.setVariablename(outVal);// could be multiple value
										// comma separated
			String plotName = out.getOutputName();
			re.parseAndEval(getPlotFilePath(plotName, imgHeight, imgWidth));

			logger.debug("Plot file name " + plotName);
			if (function.equals("hist")) {
				// predefined Histogram function
				rexp = re.parseAndEval("try(" + function + "(" + outVal + ", col=4))");
			} else if (function.equals("plot") || function.equals("biplot")) {
				// predefined plot/biplot functions
				rexp = re.parseAndEval("try(" + function + "(" + outVal + ", col=2))");
			} else {
				// function recalling a function inside the main script (auto)
				// to produce an image result

				if (outVal == null || outVal.equals("")) {
					rexp = re.parseAndEval("try(" + function + ")");
				} else {
					rexp = re.parseAndEval("try(" + function + "(" + outVal + "))");
				}

			}
			if (rexp.inherits("try-error")) {
				logger.debug("Script contains error(s)");
				res.setError(rexp.asString());
			} else {
				logger.debug("Evaluated function");
				rexp = re.parseAndEval("try(dev.off())");
				if (rexp.inherits("try-error")) {
					logger.debug("Script contains error(s)");
					res.setError(rexp.asString());
				} else {
					logger.debug("Evaluated dev.off()");
					res.setOutputType(out.getOutputType());
					String path = DataMiningUtils.getUserResourcesPath(profile) + DataMiningConstants.DATA_MINING_TEMP_PATH_SUFFIX + plotName + "."
							+ OUTPUT_PLOT_EXTENSION;
					String resImg = SpagoBIUtilities.getImageAsBase64(path, OUTPUT_PLOT_EXTENSION);
					res.setPlotName(plotName);
					if (resImg != null && !resImg.equals("")) {
						res.setResult(resImg);
						scriptExecutor.deleteTemporarySourceScript(path);
						logger.debug("Deleted temp image");
					}
				}
			}

		} else if (out.getOutputType().equalsIgnoreCase(DataMiningConstants.TEXT_OUTPUT) && outVal != null && out.getOutputName() != null) {
			logger.debug("Text output");

			REXP rexp = null;

			if (function != null && function.length() > 0) {
				if (outVal == null || outVal.equals("")) {
					outVal = out.getOuputLabel();
					rexp = re.parseAndEval("try(" + function + ")");
				} else {
					rexp = re.parseAndEval("try(" + function + "(" + outVal + "))");
					noArgFunctionExecuted = function + "(" + outVal + "))";
				}

			} else {
				rexp = re.parseAndEval(outVal);
			}

			res.setVariablename(outVal);// could be multiple value
			// comma separated
			if (rexp.inherits("try-error")) {
				logger.debug("Script contains error(s)");
				res.setError(rexp.asString());
			} else if (!rexp.isNull()) {
				res.setOutputType(out.getOutputType());
				res.setResult(getResultAsString(rexp));
			} else {
				res.setOutputType(out.getOutputType());
				res.setResult("No result");
			}
			logger.debug("Evaluated result");
		} else if (out.getOutputType().equalsIgnoreCase(DataMiningConstants.HTML_OUTPUT)) {
			logger.debug("Html output");

			REXP rexp = null;
			re.parseAndEval("library(R2HTML)");
			re.parseAndEval("library(RCurl)");

			re.parseAndEval("HTMLStart(outdir = \"" + DataMiningUtils.getUserResourcesPath(profile).replaceAll("\\\\", "/") + "\", , filename = \""
					+ ((UserProfile) profile).getUserId() + "\")");

			if (function != null) {
				if (outVal == null || outVal.equals("")) {
					outVal = out.getOuputLabel();
					rexp = re.parseAndEval("try(HTML(" + function + ", append = FALSE))");
				} else {
					rexp = re.parseAndEval("try(HTML(" + function + "(" + outVal + ")" + ", append = FALSE))");
				}

			} else {
				rexp = re.parseAndEval("HTML(" + outVal + ", append = FALSE)");
			}
			if (rexp.inherits("try-error")) {
				logger.debug("Script contains error(s)");
				res.setError(rexp.asString());
			} else {
				REXP htmlFile = re.parseAndEval("HTMLGetFile()");
				if (htmlFile.inherits("try-error")) {
					logger.debug("Script contains error(s)");
					res.setError(htmlFile.asString());
				} else if (!htmlFile.isNull()) {
					logger.debug("Html result being created");
					re.parseAndEval("u<-HTMLGetFile()");
					logger.debug("got html output file");
					re.parseAndEval("HTMLStop()");
					rexp = re.parseAndEval("u");
					rexp = re.parseAndEval("s<-paste(\"file:///\",u, sep=\"\")");
					rexp = re.parseAndEval("c<-getURL(s)");
					rexp = re.parseAndEval("c");
					logger.debug("got html");
					// delete temp file:
					boolean success = (new File(htmlFile.asString())).delete();
					res.setResult(rexp.asString());
					// res.setResult("<html><body>Hello, <b>world</b>.</body></html>");
					// comma separated

				} else {
					res.setResult("No result");
				}
			}

			res.setOutputType(out.getOutputType());
			res.setVariablename(outVal);// could be multiple value

			logger.debug("Evaluated result");
		}

		else if ((out.getOutputType().equalsIgnoreCase(DataMiningConstants.DATASET) || out.getOutputType().equalsIgnoreCase(DataMiningConstants.SPAGOBI_DS)
				|| out.getOutputType().equalsIgnoreCase("SpagoBI Dataset") || out.getOutputType().equalsIgnoreCase("Dataset")) && outVal != null
				&& out.getOutputName() != null) {
			logger.debug("Dataset output");
			CreateDatasetResult creationResult = null;
			REXP rexp = null;

			if (function != null && function.length() > 0) {
				if (outVal == null || outVal.equals("")) {
					outVal = out.getOuputLabel();
					res.setVariablename(outVal);// could be multiple value
					creationResult = createAndPersistDatasetProductByFunction(profile, outVal, out, function, userId, documentLabel);
					if (creationResult.getRExecutionError() != null && !creationResult.getRExecutionError().equals("")) {
						res.setError("R execution error:" + creationResult.getRExecutionError());
						return res;
					}
				} else {
					// res.setVariablename(outVal);// could be multiple value
					noArgFunctionExecuted = function + "(" + outVal + ")";
					res.setVariablename(noArgFunctionExecuted);
					creationResult = createAndPersistDatasetProductByFunction(profile, outVal, out, noArgFunctionExecuted, userId, documentLabel);

					if (creationResult.getRExecutionError() != null && !creationResult.getRExecutionError().equals("")) {
						res.setError("R execution error:" + creationResult.getRExecutionError());
						return res;
					}

				}

			} else { // function="" or no function (simple case)
				res.setVariablename(outVal);// could be multiple value
				creationResult = createAndPersistDataset(profile, outVal, out, userId, documentLabel);
				if (creationResult.getRExecutionError() != null && !creationResult.equals("")) {
					res.setError("R execution error:" + creationResult.getRExecutionError());
					return res;
				}

				// Code to eventually return a JSON
				// resPythonExecution = PyLib.execScript(outVal + "=" + outVal + ".to_json()\n"); // to get output as a String if (resPythonExecution < 0) {
				// res.setError("Python error"); return res; }
				// pythonResult = PyModule.getMain().getAttribute(outVal).getStringValue();
			}

			res.setOutputType("dataset");
			// res.setResult("" + pythonResult); //return Json
			// res.setResult("SpagoBi dataset saved, visible from Data Set section in Document Browser, with label :" + creationResult.getDatasetlabel());
			res.setResult(creationResult.getDatasetlabel());
			logger.debug("Evaluated result");

		} else if (out.getOutputType().equalsIgnoreCase(DataMiningConstants.FILE_OUTPUT) && out.getOutputName() != null) {
			/* Read files out from resource directory and produce output */

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

			REXP rexp = null;
			String codeToExec = "";
			String error = "";
			// codeToExec = "import os\n" + "os.chdir(r'" + strDir + "')\n";
			// resPythonExecution = PyLib.execScript(codeToExec);

			String variableName = null;
			if (out.getOuputLabel().indexOf(".") > 0) {
				variableName = out.getOuputLabel().substring(0, out.getOuputLabel().lastIndexOf("."));
			} else {
				variableName = out.getOuputLabel();
			}

			String fileName = out.getOuputLabel();
			Random randomGenerator = new Random();
			int randomInt = randomGenerator.nextInt();
			if (randomInt < 0) {
				randomInt = randomInt * (-1);
			}
			String fileVar = "fileVarName" + randomInt;

			codeToExec = fileVar + "=file('" + strDir + fileName + "','wb')";
			rexp = re.parseAndEval("try(" + codeToExec + ")");
			// codeToExec = fileVar + "= open('" + fileName + "', 'w')\n";
			// PyLib.execScript(codeToExec);
			if (rexp.inherits("try-error")) {
				logger.debug("Script contains error(s):" + rexp.asString());
				error = error + rexp.asString() + "/n";
			}

			codeToExec = "writeBin(" + variableName + "," + fileVar + ")";
			rexp = re.parseAndEval("try(" + codeToExec + ")");
			// codeToExec = fileVar + ".write(str(" + variableName + "))\n" + fileVar + ".close()\n";
			// resPythonExecution = PyLib.execScript(codeToExec);
			if (rexp.inherits("try-error")) {
				logger.debug("Script contains error(s)");
				error = error + rexp.asString() + "/n";
			}

			codeToExec = "close(" + fileVar + ")";
			rexp = re.parseAndEval("try(" + codeToExec + ")");

			if (rexp.inherits("try-error")) {
				logger.debug("Script contains error(s)");
				error = error + rexp.asString() + "/n";
				res.setError(error);
			}

			logger.debug("Output file name " + fileName);

			String writtenFile = tempDir + "/" + fileName;
			String base64ContentString = Base64.encodeBase64String(FileUtils.readFileToByteArray(new File(writtenFile)));

			JSONObject result = new JSONObject();
			result.put("filename", fileName);
			result.put("base64", base64ContentString);

			res.setOutputType(out.getOutputType());
			res.setResult(result.toString());
			res.setVariablename(variableName);

		}

		logger.debug("OUT");
		return res;
	}

	private String getPlotFilePath(String plotName, String imgHeight, String imgWidth) throws IOException {
		logger.debug("IN");
		String path = null;
		String filePath = DataMiningUtils.getUserResourcesPath(profile).replaceAll("\\\\", "/");
		if (plotName != null && !plotName.equals("")) {
			if (imgHeight == null || imgWidth == null) {
				path = OUTPUT_PLOT_IMG + "(\"" + filePath + DataMiningConstants.DATA_MINING_TEMP_FOR_SCRIPT + plotName + "." + OUTPUT_PLOT_EXTENSION + "\") ";
			} else {
				path = OUTPUT_PLOT_IMG + "(\"" + filePath + DataMiningConstants.DATA_MINING_TEMP_FOR_SCRIPT + plotName + "." + OUTPUT_PLOT_EXTENSION
						+ "\",width=" + imgWidth + " , height=" + imgHeight + ") ";
			}
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

	private String getResultAsString(REXP rexp) throws REXPMismatchException {
		logger.debug("IN");
		String result = "";
		/* http://www.studytrails.com/RJava-Eclipse-Plugin/JRI-R-Java-Data-Communication.jsp */
		Object obj = rexp.asNativeJavaObject();
		if (rexp.isVector()) {
			if (obj instanceof double[]) {
				double[] intArr = rexp.asDoubles();
				result = Arrays.toString(intArr);
			} else if (obj instanceof int[]) {
				int[] doubleArr = rexp.asIntegers();
				result = Arrays.toString(doubleArr);
			} else if (obj instanceof String[]) {
				String[] strArr = rexp.asStrings();
				result = Arrays.toString(strArr);
			} else if (obj instanceof Boolean[]) {
				int[] strArr = rexp.asIntegers();
				result = Arrays.toString(strArr);
			} else {
				result = obj.toString();
			}
		} else if (rexp.isInteger()) {
			result = rexp.asInteger() + "";
		} else if (rexp.isLogical()) {
			result = ((Boolean) obj).toString();
		} else if (rexp.isNumeric()) {
			result = rexp.asDouble() + "";
		} else if (rexp.isList()) {
			result = rexp.asList().toString();
		} else if (rexp.isString()) {
			result = rexp.asString();
		} else if (rexp.isFactor()) {
			result = rexp.asFactor().toString();
		}
		logger.debug("OUT");
		return result;

	}

	private CreateDatasetResult createAndPersistDataset(IEngUserProfile profile, String outVal, Output out, String userId, String documentLabel)
			throws Exception {
		logger.debug("IN");

		REXP rexp = null;
		CreateDatasetResult creationResult = new CreateDatasetResult();

		String spagoBiDatasetname = userId + "_" + documentLabel + "_" + out.getOuputLabel();

		FileDataSet dataSet = new FileDataSet();
		String path = getDatasetsDirectoryPath();
		String resourcePath = DAOConfig.getResourcePath();
		dataSet.setResourcePath(resourcePath);

		JSONObject configurationObj = new JSONObject();

		configurationObj.put(DataSetConstants.FILE_TYPE, "CSV");
		configurationObj.put(DataSetConstants.CSV_FILE_DELIMITER_CHARACTER, ",");
		configurationObj.put(DataSetConstants.CSV_FILE_QUOTE_CHARACTER, "\"");
		configurationObj.put(DataSetConstants.FILE_NAME, spagoBiDatasetname + ".csv");
		configurationObj.put(DataSetConstants.CSV_FILE_ENCODING, "UTF-8");
		configurationObj.put(DataSetConstants.XSL_FILE_SKIP_ROWS, DataSetConstants.XSL_FILE_SKIP_ROWS);
		configurationObj.put(DataSetConstants.XSL_FILE_LIMIT_ROWS, DataSetConstants.XSL_FILE_LIMIT_ROWS);
		configurationObj.put(DataSetConstants.XSL_FILE_SHEET_NUMBER, DataSetConstants.XSL_FILE_SHEET_NUMBER);

		String confString = configurationObj.toString();
		dataSet.setConfiguration(confString);

		String rExecution = "write.csv(" + outVal + ",file='" + path + "/" + spagoBiDatasetname + ".csv',row.names=FALSE,na='')";
		rexp = re.parseAndEval("try(" + rExecution + ")");
		if (rexp.inherits("try-error")) {
			logger.debug("Script contains error(s)");
			creationResult.setRExecutionError(rexp.asString());
		}

		dataSet.setFileName(spagoBiDatasetname + ".csv");
		dataSet.setFileType("CSV");
		dataSet.setDsType(DataSetConstants.DS_FILE);

		dataSet.setLabel(spagoBiDatasetname);
		dataSet.setName(spagoBiDatasetname);
		dataSet.setDescription("Dataset created from execution of document " + documentLabel + " by user " + userId);
		dataSet.setOwner(((UserProfile) profile).getUserId().toString());

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
		creationResult.setDatasetlabel(spagoBiDatasetname);
		return creationResult;

	}

	private CreateDatasetResult createAndPersistDatasetProductByFunction(IEngUserProfile profile, String outVal, Output out, String function, String userId,
			String documentLabel) throws Exception {
		logger.debug("IN");
		CreateDatasetResult creationResult = new CreateDatasetResult();
		REXP rexp = null;
		DataMiningResult res = new DataMiningResult();

		FileDataSet dataSet = new FileDataSet();
		String path = getDatasetsDirectoryPath();
		String resourcePath = DAOConfig.getResourcePath();
		dataSet.setResourcePath(resourcePath);

		String spagoBiDatasetname = userId + "_" + documentLabel + "_" + out.getOuputLabel();
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

		String rExecution = "write.csv(" + outVal + ",file=" + path + spagoBiDatasetname + ",row.names=FALSE,na='')";
		rexp = re.parseAndEval("try(" + rExecution + ")");
		if (rexp.inherits("try-error")) {
			logger.debug("Script contains error(s)");
			creationResult.setRExecutionError(rexp.asString());
		}

		dataSet.setFileName(spagoBiDatasetname + ".csv");
		dataSet.setFileType("CSV");
		dataSet.setDsType(DataSetConstants.DS_FILE);

		String label = out.getOuputLabel();
		dataSet.setLabel(spagoBiDatasetname);
		dataSet.setName(spagoBiDatasetname);
		dataSet.setDescription("Dataset created from execution of document " + documentLabel + " by user " + userId);
		dataSet.setOwner(((UserProfile) profile).getUserId().toString());

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
		creationResult.setDatasetlabel(spagoBiDatasetname);
		return creationResult;

	}

	public static String getDatasetsDirectoryPath() {

		String datasetDirPath = DAOConfig.getResourcePath();
		datasetDirPath += File.separatorChar + "dataset" + File.separatorChar + "files";

		File file = new File(datasetDirPath);
		if (!file.exists()) {
			if (file.mkdirs()) {
				logger.debug(datasetDirPath + " Directory is created!");
			} else {
				logger.debug("Failed to create directory " + datasetDirPath);
			}
		}

		return datasetDirPath.replace("\\", "/");
	}

}
