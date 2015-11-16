/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.datamining.compute;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.common.utils.DataMiningConstants;
import it.eng.spagobi.engines.datamining.model.DataMiningCommand;
import it.eng.spagobi.engines.datamining.model.DataMiningScript;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

public class ScriptExecutor {

	static private Logger logger = Logger.getLogger(ScriptExecutor.class);

	private Rengine re;
	DataMiningEngineInstance dataminingInstance;
	IEngUserProfile profile;

	public ScriptExecutor(DataMiningEngineInstance dataminingInstance, IEngUserProfile profile) {
		this.dataminingInstance = dataminingInstance;
		this.profile = profile;
	}

	public Rengine getRe() {
		return re;
	}

	public void setRe(Rengine re) {
		this.re = re;
	}

	protected void evalScript(DataMiningCommand command, Boolean rerun) throws Exception {
		logger.debug("IN");

		// checks whether executed before
		if (rerun || command.getExecuted() == null || !command.getExecuted()) {
			logger.debug("rerun or first execution");
			//load libraries from local dir (if needed)
			DataMiningScript script = getScript(command);
			loadLibrariesFromRLocal(script.getLibraries());
			logger.debug("loaded libraries from local dir (if needed)");
			// command-->script name --> execute script without output
			String scriptToExecute = getScriptCodeToEval(command);
			logger.debug("loaded script to execute");
			// loading libraries, preprocessing, functions definition in main
			// "auto"
			// script
			String ret = createTemporarySourceScript(scriptToExecute);
			logger.debug("created temporary script");
			re.eval("source(\"" + ret + "\")");
			logger.debug("detects action to execute from command --> used to call functions");
			// detects action to execute from command --> used to call functions
			String action = command.getAction();
			if (action != null) {
				re.eval(action);
				logger.debug("evaluated action");
			}

			command.setExecuted(true);
			logger.debug("delete temporary scripts");
			deleteTemporarySourceScript(ret);
			logger.debug("deleted temporary scripts");
		} else {
			// everithing in user workspace
			logger.debug("Command " + command.getName() + " script " + command.getScriptName() + " already executed");
		}
		logger.debug("OUT");
	}
	protected void evalExternalScript(String fileName, Map params) throws Exception {
		logger.debug("IN");
		String path = DataMiningUtils.UPLOADED_FILE_PATH + DataMiningConstants.DATA_MINING_EXTERNAL_CODE_PATH;
		String codeResource = path+fileName;
		logger.debug(codeResource);
		if(params!= null && !params.isEmpty()){
			//get libraries param  to load libraries before execution
			logger.debug("get libraries param  to load libraries before execution");
			if(params.keySet().contains(DataMiningConstants.PARAM_LIBRARIES)){
				String libs = (String)params.get(DataMiningConstants.PARAM_LIBRARIES);
				if(libs != null && !libs.equals("")){
					loadLibrariesFromRLocal(libs);
					logger.debug("Loaded libraries "+libs);
				}
			}
			
			String codeResourceTemp = path+ "temp_"+ fileName;		
			logger.debug("Needs params for temp script "+codeResourceTemp);
			File codeResourceFile = new File(codeResource);
			if(codeResourceFile.exists()){
				BufferedReader br = null;
				String code=null;
				FileWriter fw =null;
				BufferedWriter bw =null;
				try {
		 
					String sCurrentLine;
					StringBuffer content =new StringBuffer();
					br = new BufferedReader(new FileReader(codeResourceFile));
		 
					while ((sCurrentLine = br.readLine()) != null) {
						content.append(sCurrentLine+"\n");
					}
					code = content.toString();
					logger.debug("code read from input file");
					if(code != null && !code.equals("")){
						code = StringUtilities.substituteParametersInString(code, params, null, false);
						logger.debug("parameters replaced");
					}
					
				} catch (IOException e) {
					logger.error("Unable to read file "+codeResource);				
					throw e;
				} finally {
					try {
						if (br != null) br.close();
					} catch (IOException ex) {
						logger.error("Unable to close file "+codeResource);
						throw ex;
					}
				}
				try{
					File codeResourceFileTemp = new File(codeResourceTemp);
					fw= new FileWriter(codeResourceFileTemp);
					bw = new BufferedWriter(fw);
					
					bw.write(code);
					bw.close();
					fw.close();
					
				} finally {
					logger.debug("temp file created");
					try {
						if (bw != null)bw.close();
						if (fw != null)fw.close();
						
					} catch (IOException ex) {
						logger.error("Unable to close file writer "+codeResource);
						throw ex;
					}
				}
				logger.debug("Ready to execute external script with params");
				re.eval("source(\"" + codeResourceTemp + "\")");
				logger.debug("External script executed with params");
				deleteTemporarySourceScript(codeResourceTemp);
				logger.debug("Deleted temp source file");
			}else{
				logger.debug("Ready to execute external script without params");
				re.eval("source(\"" + codeResource + "\")");
				logger.debug("External script executed without params");
			}

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
	private void loadLibrariesFromRLocal(String libraryNames){
		logger.debug("IN");

		REXP rHome = re.eval("libdir<-paste(R.home(),\"library\", sep=\"/\")");
		if(rHome != null){
			if(libraryNames != null){
				String[] libs = libraryNames.split(",");
				for (int i = 0; i < libs.length; i++) {
					String lib = libs[i].trim();
					re.eval("library("+lib+",lib.loc=libdir)");
				}
			}
		}

		logger.debug("OUT");
	}

	
	private DataMiningScript getScript(DataMiningCommand command){
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
		File temporarySource = new File(DataMiningUtils.getUserResourcesPath(profile) + DataMiningConstants.DATA_MINING_TEMP_PATH_SUFFIX + name + ".R");
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


}
