/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.scheduler;

public interface SchedulerService {

	String getJobList(String token,String user);
	
	String getJobSchedulationList(String token,String user,String jobName, String jobGroup);
	
	String deleteSchedulation(String token,String user,String triggerName, String triggerGroup);
	
	String deleteJob(String token,String user,String jobName, String jobGroupName);
	
	String defineJob(String token,String user,String xmlRequest);
	
	String getJobDefinition(String token,String user,String jobName, String jobGroup);
	
	String scheduleJob(String token,String user,String xmlRequest);
	
	String getJobSchedulationDefinition(String token,String user,String triggerName, String triggerGroup);	
	
	String existJobDefinition(String token,String user,String jobName, String jobGroup);
	
}
