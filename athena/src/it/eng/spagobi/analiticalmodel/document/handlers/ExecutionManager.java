/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.document.handlers;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ExecutionManager {

	//private static ExecutionManager _instance = null;
	// exections before 2 hours ago are deleted  
	//private static int hoursAgo = 2;
	private Map _flows = null;
	
	/**
	 * Instantiates a new execution manager.
	 */
	public ExecutionManager() {
		_flows = new HashMap(); 
	}
	
	/*
    public static ExecutionManager getInstance() {
        if (_instance == null) {
        	_instance = new ExecutionManager();
        } else {
        	// erases old executions
        	Calendar now = new GregorianCalendar();
        	Calendar someHoursAgo = new GregorianCalendar();
        	someHoursAgo.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY) - hoursAgo);
        	Set keys = _instance._flows.keySet();
        	List keysToBeDeleted = new ArrayList();
        	Iterator keysIt = keys.iterator();
        	while (keysIt.hasNext()) {
        		String key = (String) keysIt.next();
        		Calendar lastExecutionCalendar = _instance.getLastExecutionInstance(key).calendar;
        		if (lastExecutionCalendar.before(someHoursAgo)) keysToBeDeleted.add(key);
        	}
        	Iterator keysToBeDeletedIt = keysToBeDeleted.iterator();
        	while (keysToBeDeletedIt.hasNext()) {
        		String key = (String) keysToBeDeletedIt.next();
        		_instance._flows.remove(key);
        	}
        }
        return _instance;
    }
    */
    
	/**
	 * Registers execution
	 * 
	 * @param instance The ExecutionInstance to be registered
	 */
	public void registerExecution(ExecutionInstance instance) {
    	if (_flows.containsKey(instance.getFlowId())) {
    		List instances = (List) _flows.get(instance.getFlowId());
    		if (!instances.contains(instance)) 
    			instances.add(instance);
    	} else {
    		List list = new ArrayList();
    		list.add(instance);
    		_flows.put(instance.getFlowId(), list);
    	}
    }
    
    /**
     * Gets the execution.
     * 
     * @param executionId the execution id
     * 
     * @return the execution
     */
    public ExecutionInstance getExecution(String executionId) {
    	Set keys = _flows.keySet();
    	Iterator it = keys.iterator();
    	while (it.hasNext()) {
    		ExecutionInstance toReturn = null;
    		String key = (String) it.next();
    		List instances = (List) _flows.get(key);
    		int i = 0;
    		for (; i < instances.size(); i++) {
    			ExecutionInstance instance = (ExecutionInstance) instances.get(i);
    			if (instance.getExecutionId().equals(executionId)) {
    				toReturn = instance;
    				break;
    			}
    		}
    		if (toReturn != null) {
        		return toReturn;
    		}
    	}
    	return null;
    }
    
    /**
     * Recover execution.
     * 
     * @param flowId the flow id
     * @param executionId the execution id
     * 
     * @return the execution instance
     */
    public ExecutionInstance recoverExecution(String flowId, String executionId) {
    	if (_flows.containsKey(flowId)) {
    		ExecutionInstance toReturn = null;
    		List instances = (List) _flows.get(flowId);
    		int i = 0;
    		for (; i < instances.size(); i++) {
    			ExecutionInstance instance = (ExecutionInstance) instances.get(i);
    			if (instance.getExecutionId().equals(executionId)) {
    				toReturn = instance;
    				break;
    			}
    		}
    		// removes execution instances starting from the requested one
    		int initialLength = instances.size();
    		for (int k = 0; k < initialLength - i; k++) {
    			instances.remove(i);
    		}
    		return toReturn;
    	} else {
    		return null;
    	}
    }
    
    /**
     * Checks if is being reexecuteing.
     * 
     * @param flowId the flow id
     * @param obj the obj
     * 
     * @return true, if is being reexecuteing
     */
    public boolean isBeingReexecuteing (String flowId, BIObject obj) {
    	if (!_flows.containsKey(flowId)) return false;
    	else {
    		BIObject lastObj = getLastExecutionObject(flowId);
    		if (lastObj.equals(obj)) return true;
    		else return false;
    	}
    }
    
    /**
     * Gets the last execution object.
     * 
     * @param flowId the flow id
     * 
     * @return the last execution object
     */
    public BIObject getLastExecutionObject(String flowId) {
    	ExecutionInstance executionInstance = getLastExecutionInstance(flowId);
    	if (executionInstance != null) {
    		return executionInstance.getBIObject();
    	} else {
    		return null;
    	}
    }
    
    /**
     * Gets the last execution id.
     * 
     * @param flowId the flow id
     * 
     * @return the last execution id
     */
    public String getLastExecutionId(String flowId) {
    	ExecutionInstance executionInstance = getLastExecutionInstance(flowId);
    	if (executionInstance != null) {
    		return executionInstance.getExecutionId();
    	} else {
    		return null;
    	}
    }
    
    /**
     * Gets the last execution instance.
     * 
     * @param flowId the flow id
     * 
     * @return the last execution instance
     */
    public ExecutionInstance getLastExecutionInstance(String flowId) {
    	if (_flows.containsKey(flowId)) {
    		List instances = (List) _flows.get(flowId);
    		ExecutionInstance toReturn = (ExecutionInstance) instances.get(instances.size() - 1); 
    		return toReturn;
    	} else {
    		return null;
    	}
    }
    
    /**
     * Gets the bI objects execution flow.
     * 
     * @param flowId the flow id
     * 
     * @return the bI objects execution flow
     */
    public List getBIObjectsExecutionFlow(String flowId) {
    	List instances = new ArrayList();
    	if (_flows.containsKey(flowId)) {
    		instances = (List) _flows.get(flowId);
    	}
    	return instances;
    }
    
}
