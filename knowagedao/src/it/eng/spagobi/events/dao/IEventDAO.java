/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.events.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.events.bo.Event;

import java.util.List;

/**
 * Defines the interfaces for all methods needed to insert, modify and deleting an
 * Event object.
 * 
 * @author Gioia
 *
 */
public interface IEventDAO {
	
	/**
	 * Loads all detail information for a Event identified by its
	 * <code>eventId</code> and its <code>user</code>. All these information,
	 * achived by a query to the DB, are stored into a <code>Event</code> object,
	 * which is returned.
	 * 
	 * @param id The id for the Event to load
	 * @param user The user that has registered the event
	 * 
	 * @return A <code>Event</code> object containing all loaded information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public Event loadEvent(Integer id, String user) throws EMFUserError;
	
	/**
	 * Loads a list of all events registered by the specified  <code>user</code>.
	 * 
	 * @param user The user that has registered the events
	 * 
	 * @return A <code>List</code> of <code>Event</code> containing all loaded information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List loadEvents(String user) throws EMFUserError;
	
	/**
	 * Register a new Event. Return the the newly created event unique identifier
	 * 
	 * @param user The user who want to register a new event
	 * 
	 * @return the newly created event unique identifier
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public Integer registerEvent(String user) throws EMFUserError;
	
	/**
	 * Unregister an event.
	 * 
	 * @param id The id for the Event to load
	 * @param user The user that has registered the event
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void unregisterEvent(Integer id, String user) throws EMFUserError;
	
	/**
	 * Unregister an event. Equals to
	 * <p><code>unregisterEvent(event.getEventId(), event.getUser())<code>
	 * 
	 * @param event The object containing all delete information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void unregisterEvent(Event event) throws EMFUserError;
	
	/**
	 * Unregiste all the events registered by the specificated user.
	 * 
	 * @param user The user that has registered the events
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void unregisterEvents(String user) throws EMFUserError;
}
