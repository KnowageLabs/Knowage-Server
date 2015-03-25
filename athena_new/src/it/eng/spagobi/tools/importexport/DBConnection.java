/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.importexport;

/** 
 * Bean that describes a SpagoBI database connection
 */
public class DBConnection {

	private String name;
	private String description;
	private boolean jdbcType = false;
	private boolean jndiType = false;
	
	/**
	 * Gets the description of the database connection.
	 * 
	 * @return description of the database connection
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Set the description.
	 * 
	 * @param description Description for the connection
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Gets the name of the database connection.
	 * 
	 * @return name of the databse connection
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the name.
	 * 
	 * @param name Name for the connection
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Check if the connection is a jdbc connections.
	 * 
	 * @return True if the connection is a jdbc
	 */
	public boolean isJdbcType() {
		return jdbcType;
	}
	
	/**
	 * Set the flag which store if the connection is a jdbc kind of connection.
	 * 
	 * @param jdbcType boolean value to describe a jdbc kind of connection
	 */
	public void setJdbcType(boolean jdbcType) {
		this.jdbcType = jdbcType;
	}
	
	/**
	 * Check if the connection is a jndi connections.
	 * 
	 * @return True if the connection is a jndi
	 */
	public boolean isJndiType() {
		return jndiType;
	}
	
	/**
	 * Set the flag which store if the connection is a jndi kind of connection.
	 * 
	 * @param jndiTypw boolean value to describe a jndi kind of connection
	 */
	public void setJndiType(boolean jndiTypw) {
		this.jndiType = jndiTypw;
	}
	
	
}
