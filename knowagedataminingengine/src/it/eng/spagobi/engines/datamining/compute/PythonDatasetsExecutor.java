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
import org.jpy.PyLib;

public class PythonDatasetsExecutor {

	static private Logger logger = Logger.getLogger(PythonDatasetsExecutor.class);

	DataMiningEngineInstance dataminingInstance;
	IEngUserProfile profile;
	int resPythonExecution = 1;

	public PythonDatasetsExecutor(DataMiningEngineInstance dataminingInstance, IEngUserProfile profile) {
		this.dataminingInstance = dataminingInstance;
		this.profile = profile;
	}

	protected String evalDatasetsNeeded(HashMap paramsFilled) throws IOException, Exception {

		logger.debug("IN");
		if (PyLib.isPythonRunning() && dataminingInstance.getDatasets() != null && !dataminingInstance.getDatasets().isEmpty()) {
			for (Iterator dsIt = dataminingInstance.getDatasets().iterator(); dsIt.hasNext();) {
				DataMiningDataset ds = (DataMiningDataset) dsIt.next();
				String options = ds.getOptions();
				if (options == null || options.equals("")) {
					options = "sep = ','";
					// all available options for pandas read_csv at http://pandas.pydata.org/pandas-docs/stable/generated/pandas.read_csv.html
				}

				if (ds.getType().equalsIgnoreCase("file")) {
					String strPathUploadedFile = PythonOutputExecutor.getDatasetsDirectoryPath();
					resPythonExecution = PyLib.execScript("import os\n" + "import pandas\n" + "import csv\n" + "os.chdir(r'" + strPathUploadedFile + "')\n");
					if (resPythonExecution < 0) {
						throw new Exception("Python script execution error");
					}
					if (ds.getReadType() == null) {
						ds.setReadType("csv");
					}

					resPythonExecution = PyLib.execScript(ds.getName() + " = pandas.read_" + ds.getReadType() + "('" + ds.getFileName() + "'," + options
							+ ")\n");
					if (resPythonExecution < 0) {
						throw new Exception("Python script execution error");
					}

				} else if (ds.getType().equalsIgnoreCase(DataMiningConstants.DATASET_OUTPUT)
						|| ds.getType().equalsIgnoreCase(DataMiningConstants.SPAGOBI_DS_OUTPUT)) {
					logger.debug("Dataset");
					// dataset content could change independently from
					// the engine, so it must be recalculated every time
					String csvToEval = DataMiningUtils.getFileFromSpagoBIDataset(paramsFilled, ds, profile);
					File file = new File(csvToEval);
					String csvPath = file.getParent();
					resPythonExecution = PyLib.execScript("import os\n" + "import pandas\n" + "import csv\n" + "os.chdir(r'" + csvPath + "')\n");
					if (resPythonExecution < 0) {
						throw new Exception("Python script execution error");
					}
					resPythonExecution = PyLib.execScript(ds.getName() + " = pandas.read_" + "csv('" + ds.getName() + ".csv'," + "sep=','" + ")\n");
					if (resPythonExecution < 0) {
						throw new Exception("Python script execution error");
					}
				}

			}
		}

		return "";
	}

	protected boolean getAndEvalDefaultDataset(DataMiningDataset ds) throws IOException, Exception { // never used
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

		if (ds.getType().equalsIgnoreCase("file")) {
			if (ds.getReadType().equalsIgnoreCase("csv")) {
				String strPathUploadedFile = DataMiningUtils.getUserResourcesPath(profile).toString() + ds.getName();
				resPythonExecution = PyLib.execScript("import os\n" + "import pandas\n" + "import csv\n" + "os.chdir(r'" + strPathUploadedFile + "')\n");
				if (resPythonExecution < 0) {
					throw new Exception("Python script execution error");
				}
				resPythonExecution = PyLib.execScript(ds.getName() + " = pandas.read_csv('" + defDSRelPath + "'," + ds.getOptions() + ")\n");
				if (resPythonExecution < 0) {
					throw new Exception("Python script execution error");
				}
			}
		}
		logger.debug("OUT");
		return true;

	}

	protected void updateDataset(DataMiningDataset ds) throws IOException, Exception { // never used
		logger.debug("IN");
		File fileDSDir = new File(DataMiningUtils.getUserResourcesPath(profile) + ds.getName());
		// /find file in dir
		File[] dsfiles = fileDSDir.listFiles();
		if (dsfiles != null) {
			String fileDSPath = dsfiles[0].getPath();

			fileDSPath = fileDSPath.replaceAll("\\\\", "/");
			logger.debug("File ds path " + fileDSPath);

			if (ds.getType().equalsIgnoreCase("file")) {
				if (ds.getReadType().equalsIgnoreCase("csv")) {
					String strPathUploadedFile = DataMiningUtils.getUserResourcesPath(profile).toString() + ds.getName();
					resPythonExecution = PyLib.execScript("import os\n" + "import pandas\n" + "import csv\n" + "os.chdir(r'" + strPathUploadedFile + "')\n");
					if (resPythonExecution < 0) {
						throw new Exception("Python script execution error");
					}
					resPythonExecution = PyLib.execScript(ds.getName() + " = pandas.read_csv('" + fileDSPath + "'," + ds.getOptions() + ")\n");
					if (resPythonExecution < 0) {
						throw new Exception("Python script execution error");
					}
				}
			}
		}
		logger.debug("OUT");
	}
}
