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
package it.eng.spagobi.analiticalmodel.functionalitytree.bo;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.Role;

import java.io.Serializable;
import java.util.List;

/**
 * Defines a <code>LowFunctionality</code> object.
 * 
 * @author sulis
 */
public class LowFunctionality  implements Serializable  {

	private Integer id;
	
	private Integer parentId;

	private String name = "";

	private String description = "";

	private String codType = ""; // code of the type of the functionality

	private String code = ""; // code of the functionality

	private String path = null;

	private Role[] execRoles = null;

	private Role[] devRoles = null;

	private Role[] testRoles = null;
	
	private Role[] createRoles = null;

	private IEngUserProfile profile = null;
	
	private List biObjects = null;
	
	private Integer prog = null;
	
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
	 * Gets the cod type.
	 * 
	 * @return Low Functionality code Type
	 */
	public String getCodType() {
		return codType;
	}
	
	/**
	 * Sets the cod type.
	 * 
	 * @param codType The low functionality code type to set
	 */
	public void setCodType(String codType) {
		this.codType = codType;
	}

	/**
	 * Gets the code.
	 * 
	 * @return Low Functionality code
	 */
	public String getCode() {
		return code;
	}
	
	/**
	 * Sets the code.
	 * 
	 * @param code The low functionality code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	
	/**
	 * constructor.
	 */
	public LowFunctionality() {
		this.setCodType("LOW_FUNCT");
	}
	
	/**
	 * Gets the dev roles.
	 * 
	 * @return Low Functionality develop roles
	 */
	public Role[] getDevRoles() {
		return devRoles;
	}
	
	/**
	 * Sets the dev roles.
	 * 
	 * @param devRoles The low functionality develop roles to set
	 */
	public void setDevRoles(Role[] devRoles) {
		this.devRoles = devRoles;
	}
	
	/**
	 * Gets the exec roles.
	 * 
	 * @return Low Functionality execution roles
	 */
	public Role[] getExecRoles() {
		return execRoles;
	}
	
	/**
	 * Sets the exec roles.
	 * 
	 * @param execRoles The low functionality execution roles to set
	 */
	public void setExecRoles(Role[] execRoles) {
		this.execRoles = execRoles;
	}
	
	public Role[] getCreateRoles() {
		return createRoles;
	}

	public void setCreateRoles(Role[] createRoles) {
		this.createRoles = createRoles;
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
	 * Gets the test roles.
	 * 
	 * @return Low Functionality test roles
	 */
	public Role[] getTestRoles() {
		return testRoles;
	}
	
	/**
	 * Sets the test roles.
	 * 
	 * @param testRoles The low functionality test roles to set
	 */
	public void setTestRoles(Role[] testRoles) {
		this.testRoles = testRoles;
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

	
}
