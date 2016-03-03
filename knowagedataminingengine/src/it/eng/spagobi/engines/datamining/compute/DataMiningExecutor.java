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

import it.eng.spago.engines.datamining.work.DataminingWriteWork;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.bo.DataMiningResult;
import it.eng.spagobi.engines.datamining.common.utils.DataMiningConstants;
import it.eng.spagobi.engines.datamining.model.DataMiningCommand;
import it.eng.spagobi.engines.datamining.model.Output;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.threadmanager.WorkManager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;

import commonj.work.WorkException;
import commonj.work.WorkItem;

public class DataMiningExecutor {
	static private Logger logger = Logger.getLogger(DataMiningExecutor.class);

	private REngine re;
	private IEngUserProfile profile;

	private final CommandsExecutor commandsExecutor;
	private final DatasetsExecutor datasetsExecutor;
	private final OutputExecutor outputExecutor;
	private final ScriptExecutor scriptExecutor;

	public DataMiningExecutor(DataMiningEngineInstance dataminingInstance, IEngUserProfile profile) {
		super();
		commandsExecutor = new CommandsExecutor(dataminingInstance, profile);
		datasetsExecutor = new DatasetsExecutor(dataminingInstance, profile);
		outputExecutor = new OutputExecutor(dataminingInstance, profile);
		scriptExecutor = new ScriptExecutor(dataminingInstance, profile);
	}

	/**
	 * Prepare REngine and user workspace environmet
	 *
	 * @param dataminingInstance
	 * @param userProfile
	 * @throws IOException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 * @throws NamingException
	 */
	private void setupEnvonment(IEngUserProfile userProfile) {
		logger.debug("IN");
		profile = userProfile;

		re = createREngineInstanceWithWork();
		if (re == null) {
			logger.error("Cannot load R");
			return;
		}
		commandsExecutor.setRe(re);
		datasetsExecutor.setRe(re);
		outputExecutor.setRe(re);
		scriptExecutor.setRe(re);

		logger.debug("created user dir");
		logger.debug("OUT");
	}

	/**
	 * Prepare REngine and user workspace environmet
	 *
	 * @param dataminingInstance
	 * @param userProfile
	 * @throws IOException
	 * @throws REXPMismatchException
	 * @throws REngineException
	 * @throws NamingException
	 */
	private void setupEnvonmentForExternal() throws IOException, REngineException, REXPMismatchException, NamingException {
		logger.debug("IN");
		// new R-engine
		re = createREngineInstanceWithWork();
		if (re == null) {
			logger.error("Cannot load R");
			return;
		}
		re.parseAndEval("setwd(\"" + DataMiningUtils.UPLOADED_FILE_PATH + DataMiningConstants.DATA_MINING_EXTERNAL_CODE_PATH + "\")");
		logger.debug("Set working directory");
		commandsExecutor.setRe(re);
		datasetsExecutor.setRe(re);
		outputExecutor.setRe(re);
		scriptExecutor.setRe(re);

		logger.debug("OUT");
	}

	/*
	 * Create or retrieve R instance. To avoid deadlock is used a Work, scheduled by the WorkManager
	 */
	private REngine createREngineInstanceWithWork() {

		REngine rEng = null;
		DataminingWriteWork workResult = null;
		long timeout = 1000;
		WorkManager workerManager;
		List<WorkItem> workItems = new LinkedList<WorkItem>();

		try {
			workerManager = new WorkManager(getSpagoBIConfigurationProperty("JNDI_THREAD_MANAGER"));
			commonj.work.WorkManager workManager = workerManager.getInnerInstance();
			DataminingWriteWork work = new DataminingWriteWork();
			WorkItem workItem = workManager.schedule(work);
			workItems.add(workItem);
			workManager.waitForAll(workItems, timeout);
			workResult = (DataminingWriteWork) workItem.getResult();
			if (workResult != null) {
				rEng = workResult.getrEngine();
			}
			work.release();
		} catch (IllegalArgumentException e) {
			logger.error("error while workManager scheduling to load R");
		} catch (WorkException e) {
			logger.error("error while workManager scheduling to load R");
		} catch (InterruptedException e) {
			logger.error("error while workManager scheduling to load R");
		} catch (NamingException e1) {
			logger.error("error while workManager scheduling to load R");
		}
		return rEng;
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

	/**
	 * Starts the execution of the auto mode output and the auto mode command passed in paramenters (to avoid list discovering).
	 *
	 * @param dataminingInstance
	 * @param command
	 *            in atuo mode
	 * @param output
	 *            in atuo mode
	 * @param userProfile
	 * @param executionType
	 * @return DataMiningResult
	 * @throws Exception
	 */
	public DataMiningResult execute(HashMap params, DataMiningCommand command, Output output, IEngUserProfile userProfile, Boolean rerun) throws Exception {
		logger.debug("IN");
		List<DataMiningResult> results = new ArrayList<DataMiningResult>();
		DataMiningResult result = null;
		setupEnvonment(userProfile);
		logger.debug("Set up environment");
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
		result = outputExecutor.evalOutput(output, scriptExecutor);
		logger.debug("Got result");
		// save result of script computation objects and datasets to
		// user workspace
		/*
		 * saveUserWorkSpace(); logger.debug("Saved user WS");
		 */
		logger.debug("OUT");
		// re.end();//has some problems
		return result;
	}

	public void externalExecution(String fileName, IEngUserProfile userProfile, HashMap paramsFilled) throws Exception {
		logger.debug("IN");
		List<DataMiningResult> results = new ArrayList<DataMiningResult>();

		setupEnvonmentForExternal();
		logger.debug("Set up environment");
		// evaluates script code
		scriptExecutor.evalExternalScript(fileName, paramsFilled);
		logger.debug("Executed script");
		logger.debug("OUT");

	}
	/*
	 * public void updateDatasetInWorkspace(DataMiningDataset ds, IEngUserProfile userProfile) throws IOException { logger.debug("IN");
	 * setupEnvonment(userProfile); logger.debug("Set up environment"); // datasets preparation datasetsExecutor.updateDataset(ds);
	 * logger.debug("Loaded datasets"); // save result of script computation objects and datasets to // user workspace saveUserWorkSpace();
	 * logger.debug("Saved WS"); logger.debug("OUT"); }
	 *
	 *
	 * protected void loadUserWorkSpace() throws IOException {
	 *
	 * example usage > save.image(file = 'D:/script/.Rdata', safe = TRUE) > load(file = 'D:/script/.Rdata')
	 *
	 * // create user workspace data logger.debug("IN"); re.(parseAndEval"save(list = ls(all = TRUE), file= '" + profile.getUserUniqueIdentifier() +
	 * ".RData')"); logger.debug("Save all object in "+profile.getUserUniqueIdentifier() + ".RData"); re.(parseAndEval"load(file= '" +
	 * profile.getUserUniqueIdentifier() + ".RData')"); logger.debug("Loaded "+profile.getUserUniqueIdentifier() + ".RData"); logger.debug("OUT"); }
	 */

	/*
	 * protected void saveUserWorkSpace() throws IOException { logger.debug("IN"); re.(parseAndEval"save(list = ls(all = TRUE), file= '" +
	 * profile.getUserUniqueIdentifier() + ".RData')"); logger.debug("OUT"); }
	 */

}
