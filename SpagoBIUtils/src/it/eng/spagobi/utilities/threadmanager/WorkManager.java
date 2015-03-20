/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.threadmanager;


import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.utilities.assertion.Assert;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import commonj.work.Work;
import commonj.work.WorkItem;
import commonj.work.WorkListener;

import de.myfoo.commonj.util.ThreadPool;
import de.myfoo.commonj.work.FooRemoteWorkItem;
import de.myfoo.commonj.work.FooWorkManager;



/**
 * @authors Angelo Bernabei angelo.bernabei@eng.it, Andrea Gioia andrea.gioia@eng.it
 */
public class WorkManager {

	private FooWorkManager workManagerInstance;
	
	
	private static transient Logger logger = Logger.getLogger(WorkManager.class);

	/**
	 * Instantiates a new work manager.
	 * 
	 * @throws NamingException the naming exception
	 */
	public WorkManager() throws NamingException {
		init();
	}
	
	
	public WorkManager(String jndiServerManager) throws NamingException {
		init(jndiServerManager);
	}

	
	
	
	/**
	 * Run.
	 * 
	 * @param job the job
	 * @param listener the listener
	 * 
	 * @throws Exception the exception
	 */
	public void run(Work job, WorkListener listener) throws Exception {
		logger.debug("IN");
		try {
			WorkItem wi = workManagerInstance.schedule(job, listener);

		} catch (Exception e) {
			logger.error("Exception", e);
			throw e;
		} finally {
			logger.debug("OUT");
		}

	}

	/**
	 * Run.
	 * 
	 * @param job the job
	 * @param listener the listener
	 * 
	 * @throws Exception the exception
	 */
	public FooRemoteWorkItem runWithReturn(Work job, WorkListener listener) throws Exception {
		logger.debug("IN");
		FooRemoteWorkItem fooRemoteWorkItem=null;
		try {
			fooRemoteWorkItem=new FooRemoteWorkItem(job, listener, workManagerInstance);
			WorkItem wi = workManagerInstance.schedule(job, listener);

		} catch (Exception e) {
			logger.error("Exception", e);
			throw e;
		} finally {
			logger.debug("OUT");
		}
		return fooRemoteWorkItem;
	}


	public FooRemoteWorkItem buildFooRemoteWorkItem(Work job, WorkListener listener) throws Exception{
		FooRemoteWorkItem fooRemoteWorkItem=null;
		fooRemoteWorkItem=new FooRemoteWorkItem(job, listener, workManagerInstance);
		return fooRemoteWorkItem;
	}

	
	public WorkItem runWithReturnWI(Work job, WorkListener listener) throws Exception {
		logger.debug("IN");
		WorkItem workItem=null;
		try {
			workItem  = workManagerInstance.schedule(job, listener);

		} catch (Exception e) {
			logger.error("Exception", e);
			throw e;
		} finally {
			logger.debug("OUT");
		}
		return workItem;
	}


	/**
	 * Initialize the inner work manager instances.
	 * 
	 * @throws NamingException the naming exception
	 */
	public void init() throws NamingException {
		init(null);
	}
	public void init(String workManagerResourceName) throws NamingException {
		
		logger.debug("IN");

		try {
			workManagerInstance = (workManagerResourceName!=null)? getSharedWorkManagerResource(workManagerResourceName): getSharedWorkManagerResource();
			if(workManagerInstance == null) {
				logger.warn("Impossible to get shared work manager a private one to this webapp will be created");
				workManagerInstance = getPrivateWorkManagerResource();
			}
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while initializing work manager", t);
		} finally {
			logger.debug("OUT");
		}
	}
	

	public FooWorkManager getPrivateWorkManagerResource() {
		logger.debug("IN");

		try {
			ThreadPool threadPool = new ThreadPool(5, 10, 10);
			FooWorkManager workManagerResource = new FooWorkManager(threadPool);
			return workManagerResource;
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while creating a private work manager resource", t);
		} finally {
			logger.debug("OUT");
		}
	}
	public FooWorkManager getSharedWorkManagerResource() throws NamingException {
		
		logger.debug("IN");

		try {
			FooWorkManager workManagerResource = null;
			String taskManagerResourceName = getWorkManagerResourceNameFromConfiguration();
			if(taskManagerResourceName != null) {
				logger.debug("WorkManager jndi name is equal to[" + taskManagerResourceName + "]");
				workManagerResource = getSharedWorkManagerResource(taskManagerResourceName);
			} 
			
			return workManagerResource;
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while getting shared work manager resource", t);
		} finally {
			logger.debug("OUT");
		}
	}
	
	public FooWorkManager getSharedWorkManagerResource(String workManagerResourceName) {

		
		logger.debug("IN");

		try {
			
			FooWorkManager workManagerResource = null;
			
			Assert.assertTrue(StringUtilities.isNotEmpty(workManagerResourceName), "Work Manager resource name cannot be empty");
			logger.debug("Work Manager resource name is equal to [" + workManagerResourceName + "]");
			
			logger.debug("Loading from JNDI context the work manager resource [" + workManagerResourceName + "] ...");
			
			Context context = null;
			try {
				context = new InitialContext();
			} catch(Throwable t) {
				throw new RuntimeException("An unexpected error occured while initializing JNDI context", t);
			}

			Object jndiResource = null;
			try {
				jndiResource = context.lookup(workManagerResourceName);
			} catch(NamingException ne) {
				logger.warn("Resource [" + workManagerResourceName + "] is not bound in this context");
			} catch(Throwable t) {
				throw new RuntimeException("An unexpected error occured while loading JNDI resource [" + workManagerResourceName + "]", t);
			}
			
			if(jndiResource != null) {
				if(jndiResource instanceof FooWorkManager) {
					workManagerResource = (FooWorkManager) jndiResource;
				} else {
					logger.warn("The resource [" + workManagerResourceName + "] is an instance of [" + jndiResource.getClass().getName()+ "] " +
							"and not an instance of [" + FooWorkManager.class.getName()+ "] as expected");
				}
				
			}
			
			if(workManagerResource != null) {
				logger.debug("The work manager resource [" + workManagerResourceName + "] has been loaded succesfully from JNDI context");
			} else {
				logger.warn("Impossible to find work manager resource [" + workManagerResourceName + "] in JNDI context");
			}

			return workManagerResource;
		} catch (Throwable t) {
			throw new RuntimeException("An error occurred while initializing the WorkManager", t);
		} finally {
			logger.debug("OUT");
		}
	}
	
	private String getWorkManagerResourceNameFromConfiguration() {
		
		logger.debug("IN");

		try {
			String workManagerResourceName = null;
			SourceBean jndiSB = (SourceBean)EnginConf.getInstance().getConfig().getAttribute("JNDI_THREAD_MANAGER");
			logger.debug("Impossible to find block [<JNDI_THREAD_MANAGER>] into configuration");
			if(jndiSB != null) {
				workManagerResourceName = (String) jndiSB.getCharacters();
			}
			return workManagerResourceName;
		} catch (Throwable t) {
			throw new RuntimeException("An unespected error occured while getting work manager resource name from configurations", t);
		} finally {
			logger.debug("OUT");
		}
	}


	public commonj.work.WorkManager getInnerInstance() {
		return workManagerInstance;
	}
	
	public void shutdown(){
		workManagerInstance.shutdown();
	}

}
