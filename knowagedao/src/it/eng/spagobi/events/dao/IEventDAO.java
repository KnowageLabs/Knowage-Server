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
