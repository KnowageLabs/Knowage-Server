/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.events.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.events.bo.EventLog;

import java.util.List;

/**
 * Defines the interfaces for all methods needed to insert, modify and deleting an
 * Event object.
 * 
 * @author Gioia
 *
 */
public interface IEventLogDAO {
		
	/**
	 * Loads an event log given its id.
	 * 
	 * @param id The Integer representing the event id
	 * 
	 * @return A <code>EventLog</code> with the id passed at input
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public EventLog loadEventLogById(Integer id) throws EMFUserError;
		
	
	/**
	 * Loads the list of all events logs associated to the user profile at input.
	 * 
	 * @param profile The user profile
	 * 
	 * @return A <code>List</code> of <code>EventLog</code> containing all loaded information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List loadEventsLogByUser(IEngUserProfile profile) throws EMFUserError;
	
	/**
	 * Register a new EventLog.
	 * 
	 * @param eventLog the event log
	 * 
	 * @return the newly created event unique identifier
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public Integer insertEventLog(EventLog eventLog) throws EMFUserError;
	
	/**
	 * Erase an event log.
	 * 
	 * @param eventLog the event log
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void eraseEventLog(EventLog eventLog) throws EMFUserError;
	
	/**
	 * Erase all event logs registered by the specificated user.
	 * 
	 * @param user The user who registered the events
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void eraseEventsLogByUser(String user) throws EMFUserError;
}
