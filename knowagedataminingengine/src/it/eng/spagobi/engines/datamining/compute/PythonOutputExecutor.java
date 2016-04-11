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

	protected DataMiningResult evalOutput(Output out, PythonScriptExecutor scriptExecutor) throws Exception {
		logger.debug("IN");
		// output -->if image and function --> execute function then prepare
		// output
		// output -->if script --> execute script then prepare output

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
			PyLib.execScript("import os\n" + "os.chdir(r'" + strDir + "')\n");

			logger.debug("Plot file name " + plotName);

			// function recalling a function inside the main script (auto)
			// to produce an image result

			if (outVal == null || outVal.equals("")) {
				// rexp = re.parseAndEval("try(" + function + ")");
				PyLib.execScript("temporaryPlotVariableToPrintOnFile=" + function + "\n");
				PyLib.execScript("temporaryPlotVariableToPrintOnFile.savefig('" + plotName + "." + OUTPUT_PLOT_EXTENSION + "')\n");
			} else {
				PyLib.execScript("temporaryPlotVariableToPrintOnFile=" + function + "(" + outVal + ")\n");
				PyLib.execScript("temporaryPlotVariableToPrintOnFile.savefig('" + plotName + "." + OUTPUT_PLOT_EXTENSION + "')\n");
				// rexp = re.parseAndEval("try(" + function + "(" + outVal + "))");
			}

			logger.debug("Evaluated dev.off()");
			res.setOutputType(out.getOutputType());
			String resImg = getPlotImageAsBase64(out.getOutputName());
			res.setPlotName(plotName);
			if (resImg != null && !resImg.equals("")) {
				res.setResult(resImg);
				scriptExecutor.deleteTemporarySourceScript(DataMiningUtils.getUserResourcesPath(profile) + DataMiningConstants.DATA_MINING_TEMP_PATH_SUFFIX
						+ plotName + "." + "OUTPUT_PLOT_EXTENSION");
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
					PyLib.execScript(outVal + "=str(" + outVal + ")"); // to get output as a String
					String pythonResult = PyModule.getMain().getAttribute(outVal).getStringValue();
					res.setOutputType(out.getOutputType());
					res.setResult("" + pythonResult);
				} else {
					// res.setVariablename(outVal);// could be multiple value
					String noArgFunctionExecuted = function + "(" + outVal + ")";
					res.setVariablename(noArgFunctionExecuted);

					PyLib.execScript(outVal + "=" + function + "(" + outVal + ")");
					PyLib.execScript(outVal + "=str(" + outVal + ")"); // to get output as a String
					String pythonResult = PyModule.getMain().getAttribute(outVal).getStringValue();
					res.setOutputType(out.getOutputType());
					res.setResult("" + pythonResult);

				}

			} else { // function="" or no function (simple case)
				res.setVariablename(outVal);// could be multiple value
				PyLib.execScript(outVal + "=str(" + outVal + ")"); // to get output as a String
				String pythonResult = PyModule.getMain().getAttribute(outVal).getStringValue();
				res.setOutputType(out.getOutputType());
				res.setResult("" + pythonResult);
			}

			// comma separated

			logger.debug("Evaluated result");
		} else if (out.getOutputType().equalsIgnoreCase(DataMiningConstants.SPAGOBI_DS_OUTPUT) && outVal != null && out.getOutputName() != null) {
			logger.debug("Text output");
			String pythonResult = null;

			if (function != null && function.length() > 0) {
				if (outVal == null || outVal.equals("")) {
					outVal = out.getOuputLabel();
					res.setVariablename(outVal);// could be multiple value
					createAndPersistDatasetProductByFunction(profile, outVal, out, function);
					PyLib.execScript(outVal + "=" + outVal + ".to_json()\n"); // to get output as a String
					pythonResult = PyModule.getMain().getAttribute(outVal).getStringValue();
				} else {
					// res.setVariablename(outVal);// could be multiple value
					String noArgFunctionExecuted = function + "(" + outVal + ")";
					res.setVariablename(noArgFunctionExecuted);
					createAndPersistDatasetProductByFunction(profile, outVal, out, noArgFunctionExecuted);

					// PyLib.execScript(outVal + "=" + function + "(" + outVal + ")");
					// PyLib.execScript(outVal + "=" + outVal + ".to_json()\n"); // to get output as a String
					pythonResult = PyModule.getMain().getAttribute(outVal).getStringValue();
				}

			} else { // function="" or no function (simple case)
				res.setVariablename(outVal);// could be multiple value
				createAndPersistDataset(profile, outVal, out);
				PyLib.execScript(outVal + "=" + outVal + ".to_json()\n"); // to get output as a String
				pythonResult = PyModule.getMain().getAttribute(outVal).getStringValue();
			}

			res.setOutputType(out.getOutputType());
			res.setResult("" + pythonResult);

			logger.debug("Evaluated result");

		} else if (out.getOutputType().equalsIgnoreCase(DataMiningConstants.HTML_OUTPUT)) {
			logger.debug("Html output");
			/*
			 * REXP rexp = null; re.parseAndEval("library(R2HTML)"); re.parseAndEval("library(RCurl)");
			 * 
			 * re.parseAndEval("HTMLStart(outdir = \"" + DataMiningUtils.getUserResourcesPath(profile).replaceAll("\\\\", "/") + "\", , filename = \"" +
			 * profile.getUserUniqueIdentifier() + "\")");
			 * 
			 * if (function != null) { if (outVal == null || outVal.equals("")) { outVal = out.getOuputLabel(); rexp = re.parseAndEval("try(HTML(" + function +
			 * ", append = FALSE))"); } else { rexp = re.parseAndEval("try(HTML(" + function + "(" + outVal + ")" + ", append = FALSE))"); }
			 * 
			 * } else { rexp = re.parseAndEval("HTML(" + outVal + ", append = FALSE)"); } if (rexp.inherits("try-error")) {
			 * logger.debug("Script contains error(s)"); res.setError(rexp.asString()); } else { REXP htmlFile = re.parseAndEval("HTMLGetFile()"); if
			 * (htmlFile.inherits("try-error")) { logger.debug("Script contains error(s)"); res.setError(htmlFile.asString()); } else if (!htmlFile.isNull()) {
			 * logger.debug("Html result being created"); re.parseAndEval("u<-HTMLGetFile()"); logger.debug("got html output file");
			 * re.parseAndEval("HTMLStop()"); rexp = re.parseAndEval("u"); rexp = re.parseAndEval("s<-paste(\"file:///\",u, sep=\"\")"); rexp =
			 * re.parseAndEval("c<-getURL(s)"); rexp = re.parseAndEval("c"); logger.debug("got html"); // delete temp file: boolean success = (new
			 * File(htmlFile.asString())).delete(); res.setResult(rexp.asString()); // comma separated
			 * 
			 * } else { res.setResult("No result"); } }
			 */
			res.setOutputType(out.getOutputType());
			res.setVariablename(outVal);// could be multiple value

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

	private static FileDataSet createAndPersistDataset(IEngUserProfile profile, String outVal, Output out) throws Exception {
		logger.debug("IN");

		FileDataSet dataSet = new FileDataSet();
		String path = getDatasetsDirectoryPath();
		dataSet.setResourcePath(path);// (DAOConfig.getResourcePath());
		System.out.println("PATH=" + path);
		// //??

		JSONObject configurationObj = new JSONObject();
		configurationObj.put("fileType", "CSV");
		configurationObj.put("csvDelimiter", ",");
		configurationObj.put("csvQuote", "'"); // Alternativa "\""
		configurationObj.put("fileName", outVal + ".csv");
		configurationObj.put("encoding", "UTF-8");
		String confString = configurationObj.toString();
		dataSet.setConfiguration(confString);

		PyLib.execScript("import os\n" + "import pandas\n" + "os.chdir(r'" + path + "')\n");
		PyLib.execScript(outVal + "=" + "pandas.DataFrame(" + outVal + ")\n");
		PyLib.execScript(outVal + ".to_csv('" + outVal + ".csv'" + ",index=False)\n");

		dataSet.setFileName(outVal + ".csv");
		dataSet.setFileType("CSV");
		dataSet.setDsType(DataSetConstants.DS_FILE);

		String label = out.getOuputLabel();
		dataSet.setLabel(label);
		dataSet.setName(label);
		dataSet.setDescription(label);
		dataSet.setOwner(profile.getUserUniqueIdentifier().toString());

		/*
		 * String cml; try { cml = writeXMLMetadata(jsonObject); dataSet.setDsMetadata(cml); } catch (SourceBeanException e) {
		 * logger.error("Error in retrieving fields metadata in correct format from metadata json"); }
		 */

		IDataSetDAO dataSetDAO = DAOFactory.getDataSetDAO();
		dataSetDAO.setUserProfile(profile);

		logger.debug("check if dataset with label " + label + " is already present");

		// check label is already present; insert or modify dependengly
		IDataSet iDataSet = dataSetDAO.loadDataSetByLabel(label);

		// loadActiveDataSetByLabel(label);
		if (iDataSet != null) {
			logger.debug("a dataset with label " + label + " is already present: modify it");
			dataSet.setId(iDataSet.getId());
			dataSetDAO.modifyDataSet(dataSet);
		} else {
			logger.debug("No dataset with label " + label + " is already present: insert it");
			dataSetDAO.insertDataSet(dataSet);

		}

		return dataSet;

	}

	private static FileDataSet createAndPersistDatasetProductByFunction(IEngUserProfile profile, String outVal, Output out, String function) throws Exception {
		logger.debug("IN");

		FileDataSet dataSet = new FileDataSet();
		String path = getDatasetsDirectoryPath();
		dataSet.setResourcePath(path);// (DAOConfig.getResourcePath());
		System.out.println("PATH=" + path);
		// //??

		JSONObject configurationObj = new JSONObject();
		configurationObj.put("fileType", "CSV");
		configurationObj.put("csvDelimiter", ",");
		configurationObj.put("csvQuote", "'"); // Alternativa "\""
		configurationObj.put("fileName", outVal + ".csv");
		configurationObj.put("encoding", "UTF-8");
		String confString = configurationObj.toString();
		dataSet.setConfiguration(confString);

		PyLib.execScript("import os\n" + "import pandas\n" + "os.chdir(r'" + path + "')\n");
		PyLib.execScript(outVal + "=" + "pandas.DataFrame(" + function + ")\n");
		PyLib.execScript(outVal + ".to_csv('" + outVal + ".csv'" + ",index=False)\n");

		dataSet.setFileName(outVal + ".csv");
		dataSet.setFileType("CSV");
		dataSet.setDsType(DataSetConstants.DS_FILE);

		String label = out.getOuputLabel();
		dataSet.setLabel(label);
		dataSet.setName(label);
		dataSet.setDescription(label);
		dataSet.setOwner(profile.getUserUniqueIdentifier().toString());

		/*
		 * String cml; try { cml = writeXMLMetadata(jsonObject); dataSet.setDsMetadata(cml); } catch (SourceBeanException e) {
		 * logger.error("Error in retrieving fields metadata in correct format from metadata json"); }
		 */

		IDataSetDAO dataSetDAO = DAOFactory.getDataSetDAO();
		dataSetDAO.setUserProfile(profile);

		logger.debug("check if dataset with label " + label + " is already present");

		// check label is already present; insert or modify dependengly
		IDataSet iDataSet = dataSetDAO.loadDataSetByLabel(label);

		// loadActiveDataSetByLabel(label);
		if (iDataSet != null) {
			logger.debug("a dataset with label " + label + " is already present: modify it");
			dataSet.setId(iDataSet.getId());
			dataSetDAO.modifyDataSet(dataSet);
		} else {
			logger.debug("No dataset with label " + label + " is already present: insert it");
			dataSetDAO.insertDataSet(dataSet);

		}

		return dataSet;

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
