/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.workflow.publishers;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.presentation.PublisherDispatcherIFace;

import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.taskmgmt.exe.TaskInstance;

public class AcceptActivityPublisher implements PublisherDispatcherIFace {

	/* (non-Javadoc)
	 * @see it.eng.spago.presentation.PublisherDispatcherIFace#getPublisherName(it.eng.spago.base.RequestContainer, it.eng.spago.base.ResponseContainer)
	 */
	public String getPublisherName(RequestContainer requestContainer, ResponseContainer responseContainer) {
		JbpmContext jbpmContext = null;
		try {
			SourceBean request = requestContainer.getServiceRequest();
	    	JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance();
	    	jbpmContext = jbpmConfiguration.createJbpmContext();
	    	String activityKeyIdStr = (String) request.getAttribute("ActivityKey");
			long activityKeyId = Long.valueOf(activityKeyIdStr).longValue();
			TaskInstance taskInstance = jbpmContext.getTaskInstance(activityKeyId);
			if(taskInstance.getStart()==null) {
				taskInstance.start();
			}
			String publisherName = taskInstance.getVariable("spago_handler").toString(); 
			return publisherName;
		} finally {
	    	if (jbpmContext != null) {
	    		jbpmContext.close();
	    	}
		}
	}

}
