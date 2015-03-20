/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Created on 13-mag-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package it.eng.spagobi.behaviouralmodel.analyticaldriver.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

import java.util.List;

/**
 * Defines  the interfaces for all methods needed to insert, modify and deleting a parameter.
 * 
 * @author Zoppello
 */
public interface IParameterDAO extends ISpagoBIDao{
	
	/**
	 * Loads all detail information for a parameter identified by its
	 * <code>parameterID</code>. All these information, are stored into a
	 * <code>Parameter</code> object, which is
	 * returned.
	 * 
	 * @param parameterID The id for the parameter to load
	 * 
	 * @return A <code>Parameter</code> object containing all loaded information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	
	public Parameter loadForDetailByParameterID(Integer parameterID) throws EMFUserError;
	
	
	/**
	 * Loads all detail information for a parameter identified by its
	 * <code>label</code>. All these information, are stored into a
	 * <code>Parameter</code> object, which is
	 * returned.
	 * 
	 * @param label The label for the parameter to load
	 * 
	 * @return A <code>Parameter</code> object containing all loaded information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	
	public Parameter loadForDetailByParameterLabel(String Label) throws EMFUserError;

	
	/**
	 * Load for execution by parameter i dand role name.
	 * 
	 * @param parameterID the parameter id
	 * @param roleName the role name
	 * 
	 * @return the parameter
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public Parameter loadForExecutionByParameterIDandRoleName(Integer parameterID, String roleName) throws EMFUserError;

	/**
	 * Loads all detail information for all parameters. For each of them, detail
	 * information is stored into a <code>Parameter</code> object. After that,
	 * all parameters are stored into a <code>List</code>, which is returned.
	 * 
	 * @return A list containing all parameters objects
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List loadAllParameters() throws EMFUserError;

	/**
	 * Implements the query to modify a parameter. All information needed is stored
	 * into the input <code>Parameter</code> object.
	 * 
	 * @param aParameter The object containing all modify information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void modifyParameter(Parameter aParameter) throws EMFUserError;
	
	/**
	 * Implements the query to insert a parameter. All information needed is stored
	 * into the input <code>Parameter</code> object.
	 * 
	 * @param aParameter The object containing all insert information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void insertParameter(Parameter aParameter) throws EMFUserError;

	/**
	 * Implements the query to erase a parameter. All information needed is stored
	 * into the input <code>Parameter</code> object.
	 * 
	 * @param aParameter The object containing all delete information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void eraseParameter(Parameter aParameter) throws EMFUserError;
}