/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.workflow.services;

import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.module.AbstractModule;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.SpagoBITracer;

import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

public class CompleteOrRejectActivityModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.service.ServiceIFace#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public void service(SourceBean request, SourceBean response) throws Exception {
    	// This action handle both activity completion and activity reject 
		JbpmContext jbpmContext = null;
		try{
			JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance();
	    	jbpmContext = jbpmConfiguration.createJbpmContext();
	    	String activityKeyIdStr = (String) request.getAttribute("ActivityKey");
			long activityKeyId = Long.valueOf(activityKeyIdStr).longValue();
			TaskInstance taskInstance = jbpmContext.getTaskInstance(activityKeyId);
			ContextInstance contextInstance = taskInstance.getContextInstance();
	    	ProcessInstance processInstance = contextInstance.getProcessInstance();
			
	    	if (request.getAttribute("CompletedActivity") != null){
	    		// Submit buttin named CompleteActivity is pressed
	    		SpagoBITracer.info("Workflow", "CompleteOrRejectActivityModule", "service", "Completing Activity ["+ activityKeyId + "]");
	    		taskInstance.end();
	    	} else {
	    		//  Submit buttin named RejectActivity is pressed
	    		SpagoBITracer.info("Workflow", "CompleteOrRejectActivityModule", "service", "Completing Activity ["+ activityKeyId + "]");
	    		taskInstance.cancel();
	    	}
	    	jbpmContext.save(processInstance);
		} catch (Exception e) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
					            "service", "Error during the complete or reject workflow activity", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
	    	if (jbpmContext != null) {
	    		jbpmContext.close();
	    	}
		}
	}

}
