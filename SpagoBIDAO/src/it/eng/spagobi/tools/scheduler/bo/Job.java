/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.scheduler.bo;

import java.util.HashMap;
import java.util.Map;

/**
 * Conveys the detail properties of a given Job instance. Jobs have a 
 * name and group associated with them, which should uniquely identify them.
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class Job {

	String name;
	String groupName;
	String description;
	Class jobClass;
	boolean durable;
	boolean requestsRecovery;
	boolean _volatile;
	Map<String, String> parameters;
	
	public Job() {
		description = null;
		parameters = new HashMap<String, String>();
		durable = true;
		_volatile = false;
		requestsRecovery = false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	/**
	 *  Return the description given to the Job instance by its creator (if any).
	 *  
	 * @return null if no description was set
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set a description for the Job instance - may be useful for 
	 * remembering/displaying the purpose of the job, though the 
	 * description has no meaning for the scheduler. 
	 * 
	 * @param description a description of the job purpose
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Get the instance of the actual class that contains the
	 * business logic of the job and that will be executed by the scheduler
	 * 
	 * @return the class that will be executed by the scheduler
	 */
	public Class getJobClass() {
		return jobClass;
	}

	public void setJobClass(Class jobClass) {
		this.jobClass = jobClass;
	}

	/**
	 * Whether or not the <code>Job</code> should remain stored after it is orphaned 
	 * (no <code>Triggers</code> point to it).
	 *
	 * If not explicitly set, the default value is false. 
	 * 
	 * @return true if the Job should remain persisted after being orphaned.
	 */
	public boolean isDurable() {
		return durable;
	}

	/**
	 * Instructs the Scheduler whether or not the  <code>Job</code> should remain 
	 * stored after it is orphaned (no <code>Triggers</code> point to it).
	 * 
	 * @params durable true if the Job should remain persisted after being orphaned. false
	 * otherwise
	 */
	public void setDurable(boolean durable) {
		this.durable = durable;
	}

	/**
	 * Instructs the Scheduler whether or not the Job should be re-executed 
	 * if a 'recovery' or 'fail-over' situation is encountered.
	 * 
	 * If not explicitly set, the default value is false. 
	 *  
	 * @return true if the Job should be re-executed  after 'recovery' or 'fail-over' situation
	 */
	public boolean isRequestsRecovery() {
		return requestsRecovery;
	}

	/**
	 * Instructs the Scheduler whether or not the Job should be re-executed 
	 * if a 'recovery' or 'fail-over' situation is encountered.
	 * 
	 * @param requestsRecovery true if the Job should be re-executed  after 'recovery' 
	 * or 'fail-over' situation. False otherwise
	 */
	public void setRequestsRecovery(boolean requestsRecovery) {
		this.requestsRecovery = requestsRecovery;
	}

	/**
	 * Whether or not the Job should not be persisted for re-use after program restarts.
	 *  
	 * If not explicitly set, the default value is false.
	 *  
	 * @return  true if the Job should be garbage collected along with the SpagoBI Server webapp.
	 */
	public boolean isVolatile() {
		return _volatile;
	}

	/**
	 * Instructs the Scheduler whether or not the Job should not be persisted for 
	 * re-use after program restarts.
	 *  
	 * @params _volatile true if the Job should be garbage collected along with the 
	 * SpagoBI Server webapp.
	 */
	public void setVolatile(boolean _volatile) {
		this._volatile = _volatile;
	}
	
	public void addParameter(String name, String value) {
		parameters.put(name, value);
	}
	
	public void addParameters(Map<String, String> parameters) {
		this.parameters.putAll(parameters);
	}
	
	public Map<String, String> getParameters() {
		return this.parameters;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((groupName == null) ? 0 : groupName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Job other = (Job) obj;
		if (groupName == null) {
			if (other.groupName != null)
				return false;
		} else if (!groupName.equals(other.groupName))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Job [name=" + name + ", groupName=" + groupName + "]";
	}	
}
