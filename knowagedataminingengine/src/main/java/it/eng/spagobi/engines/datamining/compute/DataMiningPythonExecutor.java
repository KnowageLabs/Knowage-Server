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
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.bo.DataMiningResult;
import it.eng.spagobi.engines.datamining.common.utils.DataMiningConstants;
import it.eng.spagobi.engines.datamining.model.DataMiningCommand;
import it.eng.spagobi.engines.datamining.model.DataMiningFile;
import it.eng.spagobi.engines.datamining.model.Output;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.jpy.PyLib;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

public class DataMiningPythonExecutor implements IDataMiningExecutor {
	static private Logger logger = Logger.getLogger(DataMiningPythonExecutor.class);

	// private final PythonCommandsExecutor commandsExecutor;
	private final PythonDatasetsExecutor datasetsExecutor;
	private final PythonOutputExecutor outputExecutor;
	private final PythonScriptExecutor scriptExecutor;
	private final PythonFilesExecutor fileExecutor;

	public DataMiningPythonExecutor(DataMiningEngineInstance dataminingInstance, IEngUserProfile profile) {
		super();
		// commandsExecutor = new PythonCommandsExecutor(dataminingInstance, profile);
		datasetsExecutor = new PythonDatasetsExecutor(dataminingInstance, profile);
		outputExecutor = new PythonOutputExecutor(dataminingInstance, profile);
		scriptExecutor = new PythonScriptExecutor(dataminingInstance, profile);
		fileExecutor = new PythonFilesExecutor(dataminingInstance, profile);
	}

	private void setupEnvonmentForExternal() throws IOException, REngineException, REXPMismatchException, NamingException {
		logger.debug("IN");

		String str = "" + DataMiningUtils.UPLOADED_FILE_PATH + DataMiningConstants.DATA_MINING_EXTERNAL_CODE_PATH;
		if (!PyLib.isPythonRunning()) {
			PyLib.startPython();
		}
		PyLib.execScript("import os\n" + "os.chdir(r'" + str + "')\n");

		logger.debug("Set working directory");
		logger.debug("OUT");
	}

	@Override
	public DataMiningResult execute(HashMap params, DataMiningCommand command, Output output, IEngUserProfile userProfile, Boolean rerun, String documentLabel)
			throws Exception {
		logger.debug("IN");
		List<DataMiningResult> results = new ArrayList<DataMiningResult>();
		DataMiningResult result = null;
		logger.debug("Set up environment");

		PyLib.startPython();
		logger.debug("Start Python");

		// datasets preparation
		String error = datasetsExecutor.evalDatasetsNeeded(params);
		if (error.length() > 0) {
			result = new DataMiningResult();
			result.setError(error);
			return result;
		}
		logger.debug("Loaded datasets");

		// Files input preparation
		error = fileExecutor.evalFilesNeeded(params);
		if (error.length() > 0) {
			result = new DataMiningResult();
			result.setError(error);
			return result;
		}

		// evaluates script code
		scriptExecutor.evalScript(command, rerun);
		logger.debug("Evaluated script");
		// create output
		UserProfile profile = (UserProfile) userProfile;
		result = outputExecutor.evalOutput(output, scriptExecutor, documentLabel, (String) profile.getUserId());
		logger.debug("Got result");

		// Delete files if presents
		if (fileExecutor.dataminingInstance.getFiles() != null) {
			if (fileExecutor.dataminingInstance.getFiles().size() > 0) {

				for (DataMiningFile dmFile : fileExecutor.dataminingInstance.getFiles()) {

					File file = new File(DataMiningUtils.getUserResourcesPath(profile) + dmFile.getFileName());
					if (file.delete()) {
						logger.debug(file.getName() + " is deleted!");
					} else {
						logger.debug("Delete operation is failed.");
					}
				}

			}
		}

		// PyLib.stopPython(); // Per colpa di un errore di JPY non funziona, quando saranno disponibili nuove versioni di JPY non buggate, scommentare questo

		logger.debug("Stop Python");

		logger.debug("OUT");
		return result;
	}

	@Override
	public DataMiningResult executeScript(Logger logger, DataMiningResult result, HashMap params, DataMiningCommand command, Output output,
			IEngUserProfile userProfile, Boolean rerun, String documentLabel) throws Exception {

		// evaluates script code
		scriptExecutor.evalScript(command, rerun);
		logger.debug("Evaluated script");
		// create output
		UserProfile profile = (UserProfile) userProfile;
		result = outputExecutor.evalOutput(output, scriptExecutor, documentLabel, (String) profile.getUserId());
		logger.debug("Got result");
		return result;

	}

	@Override
	public DataMiningResult setExecEnvironment(Logger logger, DataMiningResult result, HashMap params, DataMiningCommand command, IEngUserProfile userProfile,
			Boolean rerun, String documentLabel) throws Exception {

		PyLib.startPython();
		logger.debug("Start Python");

		// datasets preparation
		String error = datasetsExecutor.evalDatasetsNeeded(params);
		if (error.length() > 0) {
			result = new DataMiningResult();
			result.setError(error);
			return result;
		}
		logger.debug("Loaded datasets");

		// Files input preparation
		error = fileExecutor.evalFilesNeeded(params);
		if (error.length() > 0) {
			result = new DataMiningResult();
			result.setError(error);
			return result;
		}
		return result;
	}

	@Override
	public DataMiningResult unsetExecEnvironment(Logger logger, DataMiningResult result, HashMap params, DataMiningCommand command,
			IEngUserProfile userProfile, Boolean rerun, String documentLabel) throws Exception {
		// Delete files if presents
		if (fileExecutor.dataminingInstance.getFiles() != null) {
			if (fileExecutor.dataminingInstance.getFiles().size() > 0) {

				for (DataMiningFile dmFile : fileExecutor.dataminingInstance.getFiles()) {

					File file = new File(DataMiningUtils.getUserResourcesPath(userProfile) + dmFile.getFileName());
					if (file.delete()) {
						logger.debug(file.getName() + " is deleted!");
					} else {
						logger.debug("Delete operation is failed.");
					}
				}

			}
		}

		// PyLib.stopPython(); // Per colpa di un errore di JPY non funziona, quando saranno disponibili nuove versioni di JPY non buggate, scommentare questo

		logger.debug("Stop Python");

		logger.debug("OUT");
		return result;
	}
}
