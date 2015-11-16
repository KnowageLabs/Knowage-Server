/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.datamining.compute;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.bo.DataMiningResult;
import it.eng.spagobi.engines.datamining.common.utils.DataMiningConstants;
import it.eng.spagobi.engines.datamining.model.Output;
import it.eng.spagobi.engines.datamining.model.Variable;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RFactor;
import org.rosuda.JRI.Rengine;

public class OutputExecutor {
	static private Logger logger = Logger.getLogger(OutputExecutor.class);

	private static final String OUTPUT_PLOT_EXTENSION = "png";
	private static final String OUTPUT_PLOT_IMG = "png";

	private Rengine re;
	DataMiningEngineInstance dataminingInstance;
	IEngUserProfile profile;

	public OutputExecutor(DataMiningEngineInstance dataminingInstance, IEngUserProfile profile) {
		this.dataminingInstance = dataminingInstance;
		this.profile = profile;
	}

	public Rengine getRe() {
		return re;
	}

	public void setRe(Rengine re) {
		this.re = re;
	}

	protected DataMiningResult evalOutput(Output out, ScriptExecutor scriptExecutor) throws Exception {
		logger.debug("IN");
		// output -->if image and function --> execute function then prepare
		// output
		// output -->if script --> execute script then prepare output

		DataMiningResult res = new DataMiningResult();

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
			re.eval(getPlotFilePath(plotName));

			logger.debug("Plot file name " + plotName);
			if (function.equals("hist")) {
				// predefined Histogram function
				re.eval(function + "(" + outVal + ", col=4)");
			} else if (function.equals("plot") || function.equals("biplot")) {
				// predefined plot/biplot functions
				re.eval(function + "(" + outVal + ", col=2)");
			} else {
				// function recalling a function inside the main script (auto)
				// to produce an image result

				if (outVal == null || outVal.equals("")) {
					re.eval(function);
				} else {
					re.eval(function + "(" + outVal + ")");
				}

			}
			logger.debug("Evaluated function");
			re.eval("dev.off()");
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

			REXP rexp = null;

			if (function != null) {
				if (outVal == null || outVal.equals("")) {
					outVal = out.getOuputLabel();
					rexp = re.eval(function);
				} else {
					rexp = re.eval(function + "(" + outVal + ")");
				}

			} else {

				rexp = re.eval(outVal);
			}

			res.setVariablename(outVal);// could be multiple value
			// comma separated
			if (rexp != null) {
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
			re.eval("library(R2HTML)");
			re.eval("library(RCurl)");

			re.eval("HTMLStart(outdir = \"" + DataMiningUtils.getUserResourcesPath(profile).replaceAll("\\\\", "/") + "\", , filename = \""
					+ profile.getUserUniqueIdentifier() + "\")");

			if (function != null) {
				if (outVal == null || outVal.equals("")) {
					outVal = out.getOuputLabel();
					rexp = re.eval("HTML(" + function + ", append = FALSE)");
				} else {
					rexp = re.eval("HTML(" + function + "(" + outVal + ")" + ", append = FALSE)");
				}

			} else {
				rexp = re.eval("HTML(" + outVal + ", append = FALSE)");
			}
			REXP htmlFile = re.eval("HTMLGetFile()");
			if (htmlFile != null) {
				logger.debug("Html result being created");
				re.eval("u<-HTMLGetFile()");
				logger.debug("got html output file");
				re.eval("HTMLStop()");
				rexp = re.eval("u");
				rexp = re.eval("s<-paste(\"file:///\",u, sep=\"\")");
				rexp = re.eval("c<-getURL(s)");
				rexp = re.eval("c");
				logger.debug("got html");
				// delete temp file:
				boolean success = (new File(htmlFile.asString())).delete();
				res.setResult(rexp.asString());
				// comma separated

			} else {
				res.setResult("No result");
			}

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

	private String getResultAsString(REXP rexp) {
		logger.debug("IN");
		String result = "";
		/* http://www.studytrails.com/RJava-Eclipse-Plugin/JRI-R-Java-Data-Communication.jsp */
		int rexpType = rexp.getType();

		if (rexpType == REXP.XT_ARRAY_INT) {
			int[] intArr = rexp.asIntArray();
			result = Arrays.toString(intArr);
		} else if (rexpType == REXP.XT_ARRAY_DOUBLE) {
			double[] doubleArr = rexp.asDoubleArray();
			result = Arrays.toString(doubleArr);
		} else if (rexpType == REXP.XT_ARRAY_STR || (rexpType == REXP.XT_ARRAY_BOOL)) {
			String[] strArr = rexp.asStringArray();
			result = Arrays.toString(strArr);
		} else if (rexpType == REXP.XT_INT) {
			result = rexp.asInt() + "";
		} else if (rexpType == REXP.XT_BOOL) {
			result = rexp.asBool().toString();
		} else if (rexpType == REXP.XT_DOUBLE) {
			result = rexp.asDouble() + "";
		} else if (rexpType == REXP.XT_LIST) {
			result = rexp.asList().getBody().asString();
		} else if (rexpType == REXP.XT_STR) {
			result = rexp.asString();
		} else if (rexpType == REXP.XT_VECTOR) {
			result = rexp.asVector().toString();
		} else if (rexpType == REXP.XT_ARRAY_BOOL_INT) {
			int[] doubleArr = rexp.asIntArray();
			result = Arrays.toString(doubleArr);
		} else if (rexpType == REXP.XT_FACTOR) {
			RFactor factor = rexp.asFactor();
			result = factor.toString();
		}
		logger.debug("OUT");
		return result;

	}
}
