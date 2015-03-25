/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

**/
package it.eng.spagobi.tools.notification;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public abstract class AbstractEvent {
	
	//Unique name of the event
	String eventName;
	//Optional description of the event
	String eventDescritpion;
	//Optional object related to the event
	Object argument;
	
	
	/**
	 * @return the eventName
	 */
	public String getEventName() {
		return eventName;
	}
	/**
	 * @param eventName the eventName to set
	 */
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	/**
	 * @return the eventDescritpion
	 */
	public String getEventDescritpion() {
		return eventDescritpion;
	}
	/**
	 * @param eventDescritpion the eventDescritpion to set
	 */
	public void setEventDescritpion(String eventDescritpion) {
		this.eventDescritpion = eventDescritpion;
	}
	/**
	 * @return the argument
	 */
	public Object getArgument() {
		return argument;
	}
	/**
	 * @param argument the argument to set
	 */
	public void setArgument(Object argument) {
		this.argument = argument;
	}
	
	

}
