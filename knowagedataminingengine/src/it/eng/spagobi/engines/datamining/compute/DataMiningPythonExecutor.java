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
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.bo.DataMiningResult;
import it.eng.spagobi.engines.datamining.common.utils.DataMiningConstants;
import it.eng.spagobi.engines.datamining.model.DataMiningCommand;
import it.eng.spagobi.engines.datamining.model.DataMiningDataset;
import it.eng.spagobi.engines.datamining.model.Output;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

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

	// private REngine re;
	private IEngUserProfile profile;

	// private final PythonCommandsExecutor commandsExecutor;
	private final PythonDatasetsExecutor datasetsExecutor;
	private final PythonOutputExecutor outputExecutor;
	private final PythonScriptExecutor scriptExecutor;

	public DataMiningPythonExecutor(DataMiningEngineInstance dataminingInstance, IEngUserProfile profile) {
		super();
		// commandsExecutor = new PythonCommandsExecutor(dataminingInstance, profile);
		datasetsExecutor = new PythonDatasetsExecutor(dataminingInstance, profile);
		outputExecutor = new PythonOutputExecutor(dataminingInstance, profile);
		scriptExecutor = new PythonScriptExecutor(dataminingInstance, profile);
	}

	private void setupEnvonment(IEngUserProfile userProfile) {
		logger.debug("IN");
		profile = userProfile;

		logger.debug("created user dir");
		logger.debug("OUT");
	}

	private void setupEnvonmentForExternal() throws IOException, REngineException, REXPMismatchException, NamingException {
		logger.debug("IN");

		String str = "" + DataMiningUtils.UPLOADED_FILE_PATH + DataMiningConstants.DATA_MINING_EXTERNAL_CODE_PATH;
		if (!PyLib.isPythonRunning()) {
			PyLib.startPython();
		}
		PyLib.execScript("import os\n" + "os.chdir(r'" + str + "')\n");
		// System.out
		// .println("executed Python code:" + "import os\n" + "os.chdir(r'" + str + "')\n" + "from DataMiningPythonExecutor.setupEnvironmentForExternal");

		logger.debug("Set working directory");
		logger.debug("OUT");
	}

	private static String getSpagoBIConfigurationProperty(String propertyName) {
		try {
			String propertyValue = null;
			IConfigDAO configDao = DAOFactory.getSbiConfigDAO();
			Config cacheSpaceCleanableConfig = configDao.loadConfigParametersByLabel(propertyName);
			if ((cacheSpaceCleanableConfig != null) && (cacheSpaceCleanableConfig.isActive())) {
				propertyValue = cacheSpaceCleanableConfig.getValueCheck();
			}
			return propertyValue;
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected exception occured while loading spagobi property [" + propertyName + "]", t);
		}
	}

	public DataMiningResult execute(HashMap params, DataMiningCommand command, Output output, IEngUserProfile userProfile, Boolean rerun, String documentLabel)
			throws Exception {
		logger.debug("IN");
		List<DataMiningResult> results = new ArrayList<DataMiningResult>();
		DataMiningResult result = null;
		setupEnvonment(userProfile);
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

		// evaluates script code
		scriptExecutor.evalScript(command, rerun);
		logger.debug("Evaluated script");
		// create output
		UserProfile profile = (UserProfile) userProfile;
		result = outputExecutor.evalOutput(output, scriptExecutor, documentLabel, (String) profile.getUserId());
		logger.debug("Got result");
		// save result of script computation objects and datasets to
		// user workspace
		/*
		 * saveUserWorkSpace(); logger.debug("Saved user WS");
		 */

		// PyLib.stopPython(); //Per colpa di un errore di JPY non funziona, quando saranno disponibili nuove versioni di JPY non buggate, scommentare questo

		logger.debug("Stop Python");

		logger.debug("OUT");
		return result;
	}

	public void externalExecution(String fileName, IEngUserProfile userProfile, HashMap paramsFilled) throws Exception { // never called
		logger.debug("IN");
		List<DataMiningResult> results = new ArrayList<DataMiningResult>();

		setupEnvonmentForExternal();
		logger.debug("Set up environment");
		// evaluates script code
		scriptExecutor.evalExternalScript(fileName, paramsFilled);
		logger.debug("Executed script");
		logger.debug("OUT");

	}

	public void updateDatasetInWorkspace(DataMiningDataset ds, IEngUserProfile userProfile) throws IOException {
		logger.debug("IN");
		setupEnvonment(userProfile);
		logger.debug("Set up environment"); // datasets preparation datasetsExecutor.updateDataset(ds);
		logger.debug("Loaded datasets"); // save result of script computation objects and datasets to // user workspace saveUserWorkSpace();
		logger.debug("Saved WS");
		logger.debug("OUT");
	}

	protected void loadUserWorkSpace() throws IOException { // Never used

		// example usage > save.image(file = 'D:/script/.Rdata', safe = TRUE) > load(file = 'D:/script/.Rdata')

		// create user workspace data
		logger.debug("IN");
		// re.(parseAndEval"save(list = ls(all = TRUE), file= '" + profile.getUserUniqueIdentifier() + ".RData')");
		// logger.debug("Save all object in " + profile.getUserUniqueIdentifier() + ".RData");
		// re.(parseAndEval"load(file= '" + profile.getUserUniqueIdentifier() + ".RData')");
		logger.debug("Loaded " + profile.getUserUniqueIdentifier() + ".RData");
		logger.debug("OUT");
	}
	/*
	 * protected void saveUserWorkSpace() throws IOException { logger.debug("IN"); re.(parseAndEval"save(list = ls(all = TRUE), file= '" +
	 * profile.getUserUniqueIdentifier() + ".RData')"); logger.debug("OUT"); }
	 */

}
