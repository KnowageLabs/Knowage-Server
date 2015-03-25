/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.scheduler.service;


/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public interface ISchedulerServiceSupplier {
	String getJobList() ;
	String getJobDefinition(String jobName, String jobGroup) ;
	String getJobSchedulationList(String jobName, String jobGroupName) ;
	String getJobSchedulationDefinition(String triggerName, String triggerGroupName);
	String deleteSchedulation(String triggerName, String triggerGroup);
	String deleteJob(String jobName, String jobGroupName) ;
	String defineJob(String xmlRequest);
	String scheduleJob(String xmlRequest) ;
	String existJobDefinition(String jobName, String jobGroupName) ;
}
