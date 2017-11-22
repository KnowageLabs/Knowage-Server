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
import it.eng.spagobi.engines.datamining.model.DataMiningCommand;
import it.eng.spagobi.engines.datamining.model.DataMiningScript;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.jpy.PyLib;

public class PythonScriptExecutor {

	static private Logger logger = Logger.getLogger(PythonScriptExecutor.class);

	DataMiningEngineInstance dataminingInstance;
	IEngUserProfile profile;
	int resPythonExecution = 1;

	public PythonScriptExecutor(DataMiningEngineInstance dataminingInstance, IEngUserProfile profile) {
		this.dataminingInstance = dataminingInstance;
		this.profile = profile;
	}

	protected void evalScript(DataMiningCommand command, Boolean rerun) throws Exception {
		logger.debug("IN");

		if (!PyLib.isPythonRunning()) {
			logger.debug("No Python instance found");
			return;
		}
		// checks whether executed before
		if (rerun || command.getExecuted() == null || !command.getExecuted()) {
			logger.debug("rerun or first execution");
			// load libraries from local dir (if needed)
			DataMiningScript script = getScript(command);
			loadLibrariesFromPythonLocal(script.getLibraries());
			logger.debug("loaded specified libraries");
			// command-->script name --> execute script without output
			String scriptToExecute = getScriptCodeToEval(command);
			logger.debug("loaded script to execute");
			String codeToExecute = DataMiningUtils.replaceVariables(command.getVariables(), script.getCode());
			resPythonExecution = PyLib.execScript(codeToExecute);

			// String execRes = executeAndReturnOutString(codeToExecute);

			if (resPythonExecution < 0) {
				throw new SpagoBIEngineRuntimeException("Python engine error \n" + "Technical details:\n" + "PythonScriptExecutor.java:\n"
						+ "EXECUTION FAILED\n" + "See datamining engine log file for other details\n");

			}
			logger.debug("detects action to execute from command --> used to call functions");
			// detects action to execute from command --> used to call functions
			String action = command.getAction();
			if (action != null) {
				resPythonExecution = PyLib.execScript(action);
				if (resPythonExecution < 0) {
					throw new SpagoBIEngineRuntimeException("Python engine error \n" + "Technical details:\n" + "PythonScriptExecutor.java:\n"
							+ "EXECUTION FAILED\n" + "See datamining engine log file for other details\n");
				}
				logger.debug("evaluated action");
			}

			command.setExecuted(true);
			// logger.debug("delete temporary scripts");
			// deleteTemporarySourceScript(ret);
			// logger.debug("deleted temporary scripts");
		} else {
			// everithing in user workspace
			logger.debug("Command " + command.getName() + " script " + command.getScriptName() + " already executed");
		}
		logger.debug("OUT");
	}

	protected void deleteTemporarySourceScript(String path) {
		logger.debug("IN");
		boolean success = (new File(path)).delete();
		logger.debug("OUT");
	}

	private String getScriptCodeToEval(DataMiningCommand command) throws Exception {
		logger.debug("IN");
		String code = "";

		DataMiningScript script = getScript(command);
		if (script != null) {
			code = DataMiningUtils.replaceVariables(command.getVariables(), script.getCode());
		}
		logger.debug("OUT");
		return code;
	}

	private void loadLibrariesFromPythonLocal(String libraryNames) throws Exception {
		logger.debug("IN");

		if (libraryNames != null) {
			String[] libs = libraryNames.split(",");
			for (int i = 0; i < libs.length; i++) {
				String lib = libs[i].trim();
				PyLib.execScript("import " + lib + "\n");
			}
		}

		logger.debug("OUT");
	}

	private DataMiningScript getScript(DataMiningCommand command) {
		logger.debug("IN");
		String scriptName = command.getScriptName();
		if (dataminingInstance.getScripts() != null && !dataminingInstance.getScripts().isEmpty()) {
			for (Iterator it = dataminingInstance.getScripts().iterator(); it.hasNext();) {
				DataMiningScript script = (DataMiningScript) it.next();
				if (script.getName().equals(scriptName)) {
					return script;
				}
			}
		}
		logger.debug("OUT");
		return null;
	}

	private String createTemporarySourceScript(String code) throws IOException {
		logger.debug("IN");
		String name = RandomStringUtils.randomAlphabetic(10);
		File temporarySource = new File(DataMiningUtils.getUserResourcesPath(profile) + DataMiningConstants.DATA_MINING_TEMP_PATH_SUFFIX + name + ".py");
		if (!temporarySource.getParentFile().exists()) {
			temporarySource.getParentFile().mkdirs();
		}
		FileWriter fw = null;
		String ret = "";
		try {
			fw = new FileWriter(temporarySource);
			fw.write(code);
			fw.close();
			ret = temporarySource.getPath();
			ret = ret.replaceAll("\\\\", "/");
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
		logger.debug("OUT");
		return ret;

	}

	private String executeAndReturnOutString(String codeToExec) {
		// Create a stream to hold the output
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		// IMPORTANT: Save the old System.out!
		PrintStream old = System.out;
		// Tell Java to use your special stream
		System.setOut(ps);
		// Print some output: goes to your special stream
		resPythonExecution = PyLib.execScript(codeToExec);
		// System.out.println("CIAO");
		// Put things back
		String s = System.console().readLine();

		System.out.flush();
		System.setOut(old);
		return baos.toString();

	}

}
