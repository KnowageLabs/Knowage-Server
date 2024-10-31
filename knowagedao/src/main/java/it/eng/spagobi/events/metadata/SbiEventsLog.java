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
package it.eng.spagobi.events.metadata;

import java.util.Date;
import java.util.Set;

import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.events.bo.EventType;

/**
 * @author Gioia
 *
 */
public class SbiEventsLog extends SbiHibernateModel {
	private Integer id;
	private String user;
	private Date date;
	private String desc;
	private String params;
	private EventType eventType;
	private Set<SbiExtRoles> roles;

	/**
	 * Instantiates a new sbi events log.
	 */
	public SbiEventsLog() {
	}

	/**
	 * Instantiates a new sbi events log.
	 *
	 * @param id
	 *            the id
	 * @param user
	 *            the user
	 */
	public SbiEventsLog(Integer id, String user) {
		this.id = id;
		this.user = user;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id
	 *            the new id
	 */
	private void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Gets the user.
	 *
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Sets the user.
	 *
	 * @param user
	 *            the new user
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Gets the date.
	 *
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Sets the date.
	 *
	 * @param date
	 *            the new date
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * Gets the desc.
	 *
	 * @return the desc
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * Sets the desc.
	 *
	 * @param desc
	 *            the new desc
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}

	/**
	 * Gets the params.
	 *
	 * @return the params
	 */
	public String getParams() {
		return params;
	}

	/**
	 * Sets the params.
	 *
	 * @param params
	 *            the new params
	 */
	public void setParams(String params) {
		this.params = params;
	}

	/**
	 * Gets the eventType.
	 *
	 * @return the eventType
	 */
	public EventType getEventType() {
		return eventType;
	}

	/**
	 * Sets the eventType.
	 *
	 * @param type
	 *            the new eventType
	 */
	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}
}
