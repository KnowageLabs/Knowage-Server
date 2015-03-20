/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.commonj.runtime;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.engines.commonj.exception.WorkExecutionException;
import it.eng.spagobi.engines.commonj.exception.WorkNotFoundException;
import it.eng.spagobi.engines.commonj.process.CmdExecWork;
import it.eng.spagobi.engines.commonj.process.SpagoBIWork;
import it.eng.spagobi.engines.commonj.utils.ProcessesStatusContainer;
import it.eng.spagobi.services.proxy.EventServiceProxy;
import it.eng.spagobi.utilities.DynamicClassLoader;
import it.eng.spagobi.utilities.engines.AuditServiceProxy;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.threadmanager.WorkManager;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;


/**
 * configurazione ....
 * @author bernabei
 *
 */
public class WorkConfiguration {


	private WorksRepository worksRepository;

	public static final String DEFAULT_CONTEXT = "Default";

	private static transient Logger logger = Logger.getLogger(WorkConfiguration.class);

	public WorkConfiguration(WorksRepository worksRepository) {
		this.worksRepository = worksRepository;
	}


	/** This function prepare the execution of the new Process, 
	 * builds Listener and work manager
	 *  Loads work class
	 * Builds WorkCOntainer and adds it to Singleton class, from where will be retrieved by startWorkAction
	 * 
	 * @param session
	 * @param work
	 * @param parameters
	 * @throws WorkNotFoundException
	 * @throws WorkExecutionException
	 */

	public void configure(HttpSession session, CommonjWork work, Map parameters, String documentUnique, boolean isLabel)  throws WorkNotFoundException, WorkExecutionException {

		logger.debug("IN");

		File executableWorkDir;    	
		ProcessesStatusContainer processesStatusContainer = ProcessesStatusContainer.getInstance();

		try {
			logger.debug("Starting configure method of work : " +
					"name = [" + work.getWorkName() + "] ; " +
					"to start class= [" + work.getClassName() + "] ; ");


			executableWorkDir = worksRepository.getExecutableWorkDir(work);

			if (!worksRepository.containsWork(work)) {	    		
				logger.error("work [" + 
						worksRepository.getRootDir().getPath()+"/"+work.getWorkName()+ "] not found in repository");
				throw new WorkNotFoundException("work [" + 
						worksRepository.getRootDir().getPath()+"/"+work.getWorkName()+ "] not found in repository");
			}

			logger.debug("Work [" + work.getWorkName() +"] succesfully found in repository");

			// load in memory all jars found in folder!
			loadJars(work, executableWorkDir);

			//String classToLoad="prova.Studente";
			String classToLoad=work.getClassName();

			WorkManager wm = new WorkManager();
			logger.debug("work manager instanziated");

			AuditServiceProxy auditServiceProxy=null;
			Object auditO=parameters.get(EngineConstants.ENV_AUDIT_SERVICE_PROXY);			
			if(auditO!=null) auditServiceProxy=(AuditServiceProxy)auditO;
			Object eventO=parameters.get(EngineConstants.ENV_EVENT_SERVICE_PROXY);
			EventServiceProxy eventServiceProxy=null;
			eventServiceProxy=(EventServiceProxy)eventO;

			Object executionRoleO=parameters.get(SpagoBIConstants.EXECUTION_ROLE);
			String executionRole=executionRoleO!=null ? executionRoleO.toString() : ""; 


			// check if it is already in sessione means it is already running!!

			CommonjWorkContainer container=new CommonjWorkContainer();
			
			// no more used check in sesssion!
			//boolean already=container.isInSession(documentId, session);
			
			
//			if(already==false){
				CommonjWorkListener listener = new CommonjWorkListener(auditServiceProxy, eventServiceProxy);

				if (documentUnique!=null && isLabel) {
					listener.setBiObjectLabel(documentUnique);
				}
				else if (documentUnique!=null && !isLabel) {
					listener.setBiObjectID(documentUnique);
					
				}

				listener.setExecutionRole(executionRole);
				listener.setWorkName(work.getWorkName());
				listener.setWorkClass(work.getClassName());
				logger.info("Class to run "+classToLoad);

				logger.debug("listener ready");

				Class clazz=null;
				try {
					clazz = Thread.currentThread().getContextClassLoader().loadClass(classToLoad);
				} catch (ClassNotFoundException e) {
					logger.debug("class loaded not foud...",e);
				}
				Object obj = clazz.newInstance();
				logger.debug("class loaded "+classToLoad);
				SpagoBIWork workToLaunch=null;
				// class loaded could be instance of CmdExecWork o di Work, testa se è il primo, se no è l'altra
				if (obj instanceof CmdExecWork) {
					logger.debug("Class specified extends CmdExecWork");
					workToLaunch = (CmdExecWork) obj;
					workToLaunch.setPid(work.getPId());
					((CmdExecWork)obj).setCommand(work.getCommand());
					((CmdExecWork)obj).setCommandEnvironment(work.getCommand_environment());
					((CmdExecWork)obj).setCmdParameters(work.getCmdParameters());			
					((CmdExecWork)obj).setClasspathParameters(work.getClasspathParameters());
					workToLaunch.setAnalyticalParameters(work.getAnalyticalParameters());
					workToLaunch.setSbiParameters(work.getSbiParametersMap());					
					if(isLabel) workToLaunch.setSbiLabel(documentUnique);
				}
				else
					if (obj instanceof SpagoBIWork) {
						logger.debug("Class specified extends Work");
						workToLaunch=(SpagoBIWork)obj;
						workToLaunch.setPid(work.getPId());
						workToLaunch.setSbiParameters(work.getSbiParametersMap());
						workToLaunch.setAnalyticalParameters(work.getAnalyticalParameters());
						if(isLabel) workToLaunch.setSbiLabel(documentUnique);
					}
					else{
						logger.error("Class you want to launch should extend SpagoBIWork or CmdExecWork");
						return;
					}

				container.setPid(work.getPId());
				container.setWork(workToLaunch);
				container.setListener(listener);
				container.setName(work.getWorkName());
				container.setWm(wm);
				//container.setInSession(documentId, session);
				processesStatusContainer.getPidContainerMap().put(work.getPId(), container);

				for (Iterator iterator = processesStatusContainer.getPidContainerMap().keySet().iterator(); iterator.hasNext();) {
					String id = (String) iterator.next();
					logger.debug("ID: "+id);

				}
//			}
//			else{
//				System.out.println("Work already running");
//				logger.debug("Work already running");
//			}

		} catch (Throwable e) {
			logger.error("An error occurred while starting up execution for work [" + work.getWorkName() + "]");
			throw new WorkExecutionException("An error occurred while starting up execution for work [" + work.getWorkName() + "]", e);
		}
	}



	/** Explores all jar files in directory workDir  to load them
	 *  TODO: extend to other types!
	 * @param work
	 * @param parameters
	 * @param workDir
	 */

	private void loadJars(CommonjWork work, File workDir) {
		logger.debug("IN");

		// pass all the .jar into the folder
		File[] files = workDir.listFiles();

		for (int i = 0; i < files.length; i++) {
			File file=files[i];
			String name=file.getName();
			String ext = name.substring(name.lastIndexOf('.')+1, name.length());
			if(ext.equalsIgnoreCase("jar")){
				//updateCurrentClassLoader(file);
				logger.debug("loading file "+file.getName());			
				ClassLoader previous = Thread.currentThread().getContextClassLoader();
				DynamicClassLoader dcl = new DynamicClassLoader(file, previous);
				Thread.currentThread().setContextClassLoader(dcl);

			}
		}

		logger.debug("OUT");
	}



}
