/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.kpi.service;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.dispatching.service.DefaultRequestContext;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.kpi.SpagoBIKpiInternalEngine;

import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class KpiEngineJob implements Job {

        static private Logger logger = Logger.getLogger(KpiEngineJob.class);	
	
        static public final String MODEL_INSTANCE_ID="MODEL_INSTANCE_ID";
        static public final String PERIODICITY_ID="PERIODICITY_ID";
    	
	public void execute(JobExecutionContext context) throws JobExecutionException 
	{
	    logger.debug("IN");
	    
	    try{
		      String instName = context.getJobDetail().getName();
		      String instGroup = context.getJobDetail().getGroup();
		      JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		      logger.debug("context.isRecovering()="+context.isRecovering());
		      // use this variable for running the KPI Engine
		      String modelInstanceId = dataMap.getString(MODEL_INSTANCE_ID);
		      logger.debug("modelInstanceId="+modelInstanceId);
		     /* String periodicityID = dataMap.getString(PERIODICITY_ID);
		      logger.debug("periodicity ID="+periodicityID);*/
		      String cascade = dataMap.getString("cascade");		      
	      
		      Date data=context.getFireTime();
		      logger.debug("data="+data.toString());
		      Date beginDate=context.getScheduledFireTime();
		      Date endDate=context.getNextFireTime();
		      
		      
		      SourceBean request = null;
	    		SourceBean resp = null;
	    		EMFErrorHandler errorHandler = null;
	    		
	    		try {
	    			request = new SourceBean("");
	    			resp = new SourceBean("");
	    		} catch (SourceBeanException e1) {
	    			e1.printStackTrace();
	    		}
	    		RequestContainer reqContainer = new RequestContainer();
	    		ResponseContainer resContainer = new ResponseContainer();
	    		reqContainer.setServiceRequest(request);
	    		resContainer.setServiceResponse(resp);
	    		DefaultRequestContext defaultRequestContext = new DefaultRequestContext(
	    				reqContainer, resContainer);
	    		resContainer.setErrorHandler(new EMFErrorHandler());
	    		RequestContainer.setRequestContainer(reqContainer);
	    		ResponseContainer.setResponseContainer(resContainer);
	    		Locale locale = new Locale("it","IT","");
	    		SessionContainer session = new SessionContainer(true);
	    		reqContainer.setSessionContainer(session);
	    		SessionContainer permSession = session.getPermanentContainer();
	    		IEngUserProfile profile =UserProfile.createSchedulerUserProfile();	    		
	    		permSession.setAttribute(IEngUserProfile.ENG_USER_PROFILE, profile);
	    		errorHandler = defaultRequestContext.getErrorHandler();
	    		reqContainer.setAttribute("model_node_instance", modelInstanceId);
	    		reqContainer.setAttribute("start_date", beginDate);
	    		reqContainer.setAttribute("end_date", endDate);
	    		reqContainer.setAttribute("cascade", cascade);
	    		reqContainer.setAttribute("recalculate_anyway", "true");

	    		SpagoBIKpiInternalEngine engine = new SpagoBIKpiInternalEngine();	    		

				try {
					engine.executeByKpiEngineJob(reqContainer, resp);
				} catch (EMFUserError e) {
					logger.error("Error during engine execution", e);
					errorHandler.addError(e);
				} catch (Exception e) {
					logger.error("Error while engine execution", e);
					errorHandler.addError(new EMFUserError(EMFErrorSeverity.ERROR,
							100));
				}
		
	     } catch (Throwable e) {
        	    logger.error("Error while executiong KpiEngineJob", e);
	     } finally {

        	 logger.debug("OUT");
	    }
	}
	

}
