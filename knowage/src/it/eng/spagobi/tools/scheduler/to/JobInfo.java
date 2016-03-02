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
