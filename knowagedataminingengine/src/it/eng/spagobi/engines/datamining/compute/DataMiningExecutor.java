/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.datamining.compute;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.bo.DataMiningResult;
import it.eng.spagobi.engines.datamining.common.utils.DataMiningConstants;
import it.eng.spagobi.engines.datamining.model.DataMiningCommand;
import it.eng.spagobi.engines.datamining.model.Output;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;

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
	 */
	private void setupEnvonment(IEngUserProfile userProfile) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		logger.debug("IN");
		profile = userProfile;

		re = createREngineInstance();
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
	 */
	private void setupEnvonmentForExternal() throws IOException, REngineException, REXPMismatchException {
		logger.debug("IN");
		// new R-engine
		re = createREngineInstance();
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

	private REngine createREngineInstance() {
		REngine reEng = null;
		// TODO use workMamager
		// WorkManager workerManager = new WorkManager();
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Callable<REngine> task = new Callable<REngine>() {
			public REngine call() throws REngineException, REXPMismatchException {
				REngine reEng = null;
				reEng = REngine.getLastEngine();
				if (reEng == null) {
					try {
						/*
						 * attention : setting to --save the engine doesn't remove object from the current environment,so it's unrelevant the workspace saving
						 * if you don't remove manualli the R objects first through the rm command:re = new REngine(new String[] { "--save" }, false, null);
						 */
						reEng = REngine.engineForClass("org.rosuda.REngine.JRI.JRIEngine", new String[] { "--vanilla" }, null, false);
						logger.debug("new R engine created");

					} catch (Exception e) {
						logger.error("Error during creation of new R instance");
					}
				} else {
					// Clean workspace
					reEng.parseAndEval("rm(list=ls())");
				}
				return reEng;
			}
		};
		Future<REngine> future = executor.submit(task);
		try {
			reEng = future.get(500, TimeUnit.MILLISECONDS);
		} catch (TimeoutException ex) {
			logger.error("TimeoutException during creation of new R instance");
		} catch (InterruptedException e) {
			logger.error("InterruptedException during creation of new R instance");
		} catch (ExecutionException e) {
			logger.error("ExecutionException during creation of new R instance");
		} finally {
			future.cancel(true);
		}
		return reEng;
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

		setupEnvonment(userProfile);
		logger.debug("Set up environment");
		// datasets preparation
		datasetsExecutor.evalDatasetsNeeded(params);// only
		logger.debug("Loaded datasets");
		// those
		// needed
		// by
		// command
		// and
		// output

		// evaluates script code
		scriptExecutor.evalScript(command, rerun);
		logger.debug("Evaluated script");
		// create output
		DataMiningResult result = outputExecutor.evalOutput(output, scriptExecutor);
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
