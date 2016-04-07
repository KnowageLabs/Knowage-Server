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
import it.eng.spagobi.engines.datamining.model.DataMiningDataset;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jpy.PyLib;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;

public class PythonDatasetsExecutor {

	static private Logger logger = Logger.getLogger(PythonDatasetsExecutor.class);

	private REngine re;

	DataMiningEngineInstance dataminingInstance;
	IEngUserProfile profile;

	public PythonDatasetsExecutor(DataMiningEngineInstance dataminingInstance, IEngUserProfile profile) {
		this.dataminingInstance = dataminingInstance;
		this.profile = profile;
	}

	/*
	 * public REngine getRe() { return re; }
	 *
	 * public void setRe(REngine re) { this.re = re; }
	 */
	protected String evalDatasetsNeeded(HashMap paramsFilled) throws IOException, REngineException, REXPMismatchException {

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
					if (ds.getReadType().equalsIgnoreCase("csv")) {
						String strPathUploadedFile = DataMiningUtils.getUserResourcesPath(profile).toString() + ds.getName();
						PyLib.execScript("import os\n" + "import pandas\n" + "import csv\n" + "os.chdir(r'" + strPathUploadedFile + "')\n");
						PyLib.execScript(ds.getName() + " = pandas.read_csv('" + ds.getFileName() + "'," + options + ")\n");
					}

				} else if (ds.getType().equalsIgnoreCase("spagobi_ds")) {
					logger.debug("SpagoBI ds");
					// spagobi dataset content could change independently from
					// the engine, so it must be recalculated every time

					if (ds.getReadType().equalsIgnoreCase("csv")) {
						String csvToEval = DataMiningUtils.getFileFromSpagoBIDataset(paramsFilled, ds, profile);
						PyLib.execScript("import os\n" + "import pandas\n" + "import csv\n" + "os.chdir(r'" + csvToEval + "')\n");
						PyLib.execScript(ds.getName() + " = pandas.read_csv('" + ds.getFileName() + "'," + options + ")\n");
					}

				}

			}
		}

		return "";
	}

	protected boolean getAndEvalDefaultDataset(DataMiningDataset ds) throws IOException { // never used
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
				PyLib.execScript("import os\n" + "import pandas\n" + "import csv\n" + "os.chdir(r'" + strPathUploadedFile + "')\n");
				PyLib.execScript(ds.getName() + " = pandas.read_csv('" + defDSRelPath + "'," + ds.getOptions() + ")\n");
			}
		}
		logger.debug("OUT");
		return true;

	}

	protected void updateDataset(DataMiningDataset ds) throws IOException, REngineException, REXPMismatchException { // never used
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
					PyLib.execScript("import os\n" + "import pandas\n" + "import csv\n" + "os.chdir(r'" + strPathUploadedFile + "')\n");
					PyLib.execScript(ds.getName() + " = pandas.read_csv('" + fileDSPath + "'," + ds.getOptions() + ")\n");
				}
			}
		}
		logger.debug("OUT");
	}
}
