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
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngine;

public class OutputExecutor {
	static private Logger logger = Logger.getLogger(OutputExecutor.class);

	private static final String OUTPUT_PLOT_EXTENSION = "png";
	private static final String OUTPUT_PLOT_IMG = "png";

	private REngine re;
	DataMiningEngineInstance dataminingInstance;
	IEngUserProfile profile;

	public OutputExecutor(DataMiningEngineInstance dataminingInstance, IEngUserProfile profile) {
		this.dataminingInstance = dataminingInstance;
		this.profile = profile;
	}

	public REngine getRe() {
		return re;
	}

	public void setRe(REngine re) {
		this.re = re;
	}

	protected DataMiningResult evalOutput(Output out, ScriptExecutor scriptExecutor) throws Exception {
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

		if (out.getOutputType().equalsIgnoreCase(DataMiningConstants.IMAGE_OUTPUT) && out.getOutputName() != null) {
			logger.debug("Image output");
			res.setVariablename(outVal);// could be multiple value
										// comma separated
			String plotName = out.getOutputName();
			re.parseAndEval(getPlotFilePath(plotName));

			REXP rexp = null;

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
					String resImg = getPlotImageAsBase64(out.getOutputName());
					res.setPlotName(plotName);
					if (resImg != null && !resImg.equals("")) {
						res.setResult(resImg);
						scriptExecutor.deleteTemporarySourceScript(DataMiningUtils.getUserResourcesPath(profile)
								+ DataMiningConstants.DATA_MINING_TEMP_PATH_SUFFIX + plotName + "." + OUTPUT_PLOT_EXTENSION);
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
					+ profile.getUserUniqueIdentifier() + "\")");

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
					// comma separated

				} else {
					res.setResult("No result");
				}
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

	private String getResultAsString(REXP rexp) throws REXPMismatchException {
		logger.debug("IN");
		String result = "";
		/* http://www.studytrails.com/RJava-Eclipse-Plugin/JRI-R-Java-Data-Communication.jsp */
		Object obj = rexp.asNativeJavaObject();
		if (rexp.isVector()) {
			if (obj instanceof double[]) {
				int[] intArr = rexp.asIntegers();
				result = Arrays.toString(intArr);
			} else if (obj instanceof int[]) {
				double[] doubleArr = rexp.asDoubles();
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
}
