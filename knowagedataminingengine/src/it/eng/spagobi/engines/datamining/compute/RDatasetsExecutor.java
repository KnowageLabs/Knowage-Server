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
import it.eng.spagobi.engines.datamining.common.utils.DataMiningConstants;
import it.eng.spagobi.engines.datamining.model.DataMiningDataset;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPUnknown;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;

public class RDatasetsExecutor {

	static private Logger logger = Logger.getLogger(RDatasetsExecutor.class);

	private REngine re;

	DataMiningEngineInstance dataminingInstance;
	IEngUserProfile profile;

	public RDatasetsExecutor(DataMiningEngineInstance dataminingInstance, IEngUserProfile profile) {
		this.dataminingInstance = dataminingInstance;
		this.profile = profile;
	}

	public REngine getRe() {
		return re;
	}

	public void setRe(REngine re) {
		this.re = re;
	}

	protected String evalDatasetsNeeded(HashMap paramsFilled) throws IOException, REngineException, REXPMismatchException {
		logger.debug("IN");
		if (re != null && dataminingInstance.getDatasets() != null && !dataminingInstance.getDatasets().isEmpty()) {
			for (Iterator dsIt = dataminingInstance.getDatasets().iterator(); dsIt.hasNext();) {
				DataMiningDataset ds = (DataMiningDataset) dsIt.next();
				String options = ds.getOptions();
				if (options == null || options.equals("")) {
					options = "header = TRUE, sep = \",\"";
				}
				if (ds.getType().equalsIgnoreCase("file")) {

					// tries to get it from user workspace
					REXP datasetNameInR = re.parseAndEval(ds.getName());
					if (datasetNameInR.isNull() || datasetNameInR instanceof REXPUnknown) {
						logger.debug("File ds: gets default DS");
						Boolean defaultExists = getAndEvalDefaultDataset(ds);
						if (!defaultExists) {

							File fileDSDir = new File(DataMiningUtils.getUserResourcesPath(profile) + ds.getName());
							// /find file in dir
							File[] dsfiles = fileDSDir.listFiles();
							if (dsfiles != null && dsfiles.length != 0) {
								String fileDSPath = dsfiles[0].getPath();

								fileDSPath = fileDSPath.replaceAll("\\\\", "/");

								String stringToEval = ds.getName() + "<-read." + ds.getReadType() + "(\"" + fileDSPath + "\"," + options + ");";
								REXP resultRead = re.parseAndEval(stringToEval);
								if (resultRead.inherits("try-error")) {
									logger.error("Impossibile to read the dataset with command: " + stringToEval);
									return resultRead.asString();
								} else {
									logger.debug("Dataset " + ds.getName() + "sucessfull read");
								}

							}
						}

					} else {
						// use it!
						logger.debug("dataset " + ds.getName() + " already loaded in user workspace!");
					}
				} else if (ds.getType().equalsIgnoreCase(DataMiningConstants.DATASET_OUTPUT)) {
					logger.debug("Dataset");
					// spagobi dataset content could change independently from
					// the engine, so it must be recalculated every time
					try {

						String csvToEval = DataMiningUtils.getFileFromSpagoBIDataset(paramsFilled, ds, profile);
						String stringToEval = ds.getName() + "<-read.csv(\"" + csvToEval + "\",header = TRUE, sep = \",\");";
						re.parseAndEval(stringToEval);

					} catch (IOException e) {
						logger.error(e.getMessage());
						throw e;
					}

				}
			}
		}
		logger.debug("OUT");
		return "";
	}

	protected boolean getAndEvalDefaultDataset(DataMiningDataset ds) throws IOException, REngineException, REXPMismatchException {
		logger.debug("IN");
		// checks relative path
		String relPathToDefDS = ds.getDefaultDS();
		if (relPathToDefDS == null || relPathToDefDS.equals("")) {
			return false;
		}
		if (relPathToDefDS.startsWith("/") || relPathToDefDS.startsWith("\\\\")) {
			relPathToDefDS = relPathToDefDS.substring(1);
		}
		String defDSRelPath = DataMiningUtils.UPLOADED_FILE_PATH + relPathToDefDS;

		defDSRelPath = defDSRelPath.replaceAll("\\\\", "/");
		logger.debug("Default path " + defDSRelPath);
		File fileDSDefault = new File(defDSRelPath);
		if (!fileDSDefault.exists()) {
			logger.debug("Default file doesn't exist");
			return false;
		}
		String stringToEval = ds.getName() + "<-read." + ds.getReadType() + "(\"" + defDSRelPath + "\"," + ds.getOptions() + ");";

		logger.debug("R code to eval " + stringToEval);
		re.parseAndEval(stringToEval);
		logger.debug("OUT");
		return true;

	}

	protected void updateDataset(DataMiningDataset ds) throws IOException, REngineException, REXPMismatchException {
		logger.debug("IN");
		File fileDSDir = new File(DataMiningUtils.getUserResourcesPath(profile) + ds.getName());
		// /find file in dir
		File[] dsfiles = fileDSDir.listFiles();
		if (dsfiles != null) {
			String fileDSPath = dsfiles[0].getPath();

			fileDSPath = fileDSPath.replaceAll("\\\\", "/");
			logger.debug("File ds path " + fileDSPath);

			String stringToEval = ds.getName() + "<-read." + ds.getReadType() + "(\"" + fileDSPath + "\"," + ds.getOptions() + ");";
			logger.debug("R code to eval " + stringToEval);
			re.parseAndEval(stringToEval);
		}
		logger.debug("OUT");
	}
}
