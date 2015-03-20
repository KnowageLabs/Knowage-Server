/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.dossier.bo;

import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.dao.DAOFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * Store the information about single document configuration
 *
 */
public class ConfiguredBIDocument implements Serializable{

//	private Integer id = null;
	private String name = "";
	private String label = "";
	private String description = "";
	private List roles = null;
	private Map parameters = null;
	// the placeholder 
	private String logicalName= "";
	
	/**
	 * Gets the description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the description.
	 * 
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Gets the label.
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * Sets the label.
	 * 
	 * @param label the new label
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	
	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 * 
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
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
	 * Gets the parameters.
	 * 
	 * @return the parameters
	 */
	public Map getParameters() {
		return parameters;
	}
	
	/**
	 * Sets the parameters.
	 * 
	 * @param parameters the new parameters
	 */
	public void setParameters(Map parameters) {
		this.parameters = parameters;
	}
//	public Integer getId() {
//		return id;
//	}
//	public void setId(Integer id) {
//		this.id = id;
//	}
	/**
 * Gets the logical name.
 * 
 * @return the logical name
 */
public String getLogicalName() {
		return logicalName;
	}
	
	/**
	 * Sets the logical name.
	 * 
	 * @param logicalName the new logical name
	 */
	public void setLogicalName(String logicalName) {
		this.logicalName = logicalName;
	}
	
	/**
	 * To xml.
	 * 
	 * @return the string
	 */
	public String toXml() {
		String toReturn = 	"<" + logicalName + " label='" + label + "' name='" + name + "' description='" + description + "' >\n";
		toReturn +=			"  <PARAMETERS>\n";
		Set keys = parameters.keySet();
		Iterator iterKeys = keys.iterator();
		while(iterKeys.hasNext()) {
			String paramname = (String) iterKeys.next();
			String paramvalue = (String) parameters.get(paramname);
			toReturn +=			"    <PARAMETER name='" + paramname + "' value='" + paramvalue + "' />\n";
		}
		toReturn +=			"  </PARAMETERS>\n";
		toReturn += 		"</" + logicalName + ">\n";
		return toReturn;
	}
	
	/**
	 * From xml.
	 * 
	 * @param input the input
	 * 
	 * @return the configured bi document
	 */
	public static ConfiguredBIDocument fromXml(SourceBean input) {
		ConfiguredBIDocument toReturn = new ConfiguredBIDocument();
		toReturn.setLogicalName(input.getName());
		toReturn.setLabel((String) input.getAttribute("label"));
		toReturn.setName((String) input.getAttribute("name"));
		toReturn.setDescription((String) input.getAttribute("description"));
		List parametersList = input.getAttributeAsList("PARAMETERS.PARAMETER");
		Map parameters = new HashMap();
		if (parametersList != null && parametersList.size() > 0) {
			Iterator parametersListIt = parametersList.iterator();
			while (parametersListIt.hasNext()) {
				SourceBean parameterSb = (SourceBean) parametersListIt.next();
				String name = (String) parameterSb.getAttribute("name");
				String value = (String) parameterSb.getAttribute("value");
				parameters.put(name, value);
			}
		}
		toReturn.setParameters(parameters);
		return toReturn;
	}
	
	public BIObject loadBIObjectDetails() {
		BIObject biObject;
		try {
			biObject = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(label);
			List parameters = DAOFactory.getBIObjectParameterDAO().loadBIObjectParametersById(biObject.getId());
			biObject.setBiObjectParameters(parameters);
		} catch (EMFUserError e) {
			throw new RuntimeException("Cannot load details of biobject with label " + label, e);
		}
		return biObject;
	}
	
	
}
