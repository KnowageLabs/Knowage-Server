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
package it.eng.spagobi.events.bo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * This class map the SBI_EVENTS_LOG table
 * 
 * @author Gioia
 *
 */
public class EventLog implements Serializable {
	private Integer id;
	private String user;
	private Date date;
	private String desc;
	private String params;
	private String handler;
	private List roles;
	
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
	 * @param date the new date
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
	 * @param desc the new desc
	 */
	public void setDesc(String desc) {
		this.desc = desc;
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
	 * @param id the new id
	 */
	public void setId(Integer id) {
		this.id = id;
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
	 * @param params the new params
	 */
	public void setParams(String params) {
		this.params = params;
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
	 * @param user the new user
	 */
	public void setUser(String user) {
		this.user = user;
	}	
	
	/**
	 * Gets the roles.
	 * 
	 * @return the roles
	 */
	public List getRoles() {
		return roles;
	}
	
	/**
	 * Sets the roles.
	 * 
	 * @param roles the new roles
	 */
	public void setRoles(List roles) {
		this.roles = roles;
	}
	
	/**
	 * Gets the handler.
	 * 
	 * @return the handler
	 */
	public String getHandler() {
		return handler;
	}
	
	/**
	 * Sets the handler.
	 * 
	 * @param handler the new handler
	 */
	public void setHandler(String handler) {
		this.handler = handler;
	}
}
