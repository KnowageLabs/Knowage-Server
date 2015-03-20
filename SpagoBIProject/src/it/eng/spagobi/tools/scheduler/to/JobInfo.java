/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.scheduler.to;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class JobInfo implements Serializable{

	private String jobName = "";
	private String jobGroupName = "";
	private String jobDescription = "";
	private String schedulerAdminstratorIdentifier = "";
	private List<BIObject> documents = new ArrayList<BIObject>();
	
	/**
	 * Gets the job description.
	 * 
	 * @return the job description
	 */
	public String getJobDescription() {
		return jobDescription;
	}
	
	/**
	 * Sets the job description.
	 * 
	 * @param jobDescription the new job description
	 */
	public void setJobDescription(String jobDescription) {
		this.jobDescription = jobDescription;
	}
	
	/**
	 * Gets the job name.
	 * 
	 * @return the job name
	 */
	public String getJobName() {
		return jobName;
	}
	
	/**
	 * Sets the job name.
	 * 
	 * @param jobName the new job name
	 */
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	

	public List<BIObject> getDocuments() {
		return documents;
	}
	
	public void setDocuments(List<BIObject> documents) {
		this.documents = documents;
	}
	
	public List<Integer> getDocumentIds() {
		List<Integer> documentIds = new ArrayList<Integer>();
		for( BIObject document : documents) {
			Integer id =  document.getId();
			documentIds.add(id);
		}
		return documentIds;
	}
	
	/**
	 * Gets the job group name.
	 * 
	 * @return the job group name
	 */
	public String getJobGroupName() {
		return jobGroupName;
	}
	
	/**
	 * Sets the job group name.
	 * 
	 * @param jobGroupName the new job group name
	 */
	public void setJobGroupName(String jobGroupName) {
		this.jobGroupName = jobGroupName;
	}

	public String getSchedulerAdminstratorIdentifier() {
		return schedulerAdminstratorIdentifier;
	}

	public void setSchedulerAdminstratorIdentifier(
			String schedulerAdminstratorIdentifier) {
		this.schedulerAdminstratorIdentifier = schedulerAdminstratorIdentifier;
	}
	
}
