/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.functionalitytree.bo;

import it.eng.spagobi.commons.bo.Role;

import java.io.Serializable;
import java.util.List;

/**
 * Defines a <code>UserFunctionality</code> object.
 */
public class UserFunctionality  implements Serializable  {

	private Integer id;
	
	private Integer parentId;
	
	private String code = "";

	private String name = "";

	private String description = "";

	private String path = null;
	
	private List biObjects = null;
	
	private Integer prog = null;
	
	private Role[] execRoles = null;
	
	/**
	 * Gets the description.
	 * 
	 * @return Low Functionality description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 * 
	 * @param description The low functionality description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Gets the id.
	 * 
	 * @return Low Functionality id
	 */
	public Integer getId() {
		return id;
	}
	
	/**
	 * Sets the id.
	 * 
	 * @param id The low functionality id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Gets the name.
	 * 
	 * @return Low Functionality name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 * 
	 * @param name The low functionality name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * Gets the path.
	 * 
	 * @return Low Functionality path
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * Sets the path.
	 * 
	 * @param path The low functionality path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Gets the parent id.
	 * 
	 * @return the parent id
	 */
	public Integer getParentId() {
		return parentId;
	}

	/**
	 * Sets the parent id.
	 * 
	 * @param parentId the new parent id
	 */
	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	/**
	 * Gets the bi objects.
	 * 
	 * @return the bi objects
	 */
	public List getBiObjects() {
		return biObjects;
	}

	/**
	 * Sets the bi objects.
	 * 
	 * @param biObjects the new bi objects
	 */
	public void setBiObjects(List biObjects) {
		this.biObjects = biObjects;
	}

	/**
	 * Gets the prog.
	 * 
	 * @return the prog
	 */
	public Integer getProg() {
		return prog;
	}

	/**
	 * Sets the prog.
	 * 
	 * @param prog the new prog
	 */
	public void setProg(Integer prog) {
		this.prog = prog;
	}

	/**
	 * Gets the code.
	 * 
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Sets the code.
	 * 
	 * @param code the new code
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * Gets the exec roles.
	 * 
	 * @return the exec roles
	 */
	public Role[] getExecRoles() {
		return execRoles;
	}

	/**
	 * Sets the exec roles.
	 * 
	 * @param execRoles the new exec roles
	 */
	public void setExecRoles(Role[] execRoles) {
		this.execRoles = execRoles;
	}
	
}
