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
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

import java.util.List;

import org.hibernate.Session;

/**
 * Defines the interfaces for all methods needed to insert, modify and deleting a
 * ObjParuse.
 * 
 * @author Zerbetto
 *
 */
public interface IObjParuseDAO extends ISpagoBIDao{
	
	/**
	 * Loads the list of ObjParuse associated to the input
	 * <code>objParId</code> and <code>paruseId</code>. All these information,
	 * achived by a query to the DB, are stored into a List of <code>ObjParuse</code> object,
	 * which is returned.
	 * 
	 * @param objParId The id for the BI object parameter to load
	 * @param paruseId The parameterUse-id for the Parameter to load
	 * 
	 * @return A List of <code>ObjParuse</code> object containing all loaded information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List loadObjParuse(Integer objParId, Integer paruseId) throws EMFUserError;

	/**
	 * Implements the query to modify a ObjParuse. All information needed is stored
	 * into the input <code>ObjParuse</code> object.
	 * 
	 * @param aObjParuse The ObjParuse containing all modify information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void modifyObjParuse(ObjParuse aObjParuse) throws EMFUserError;

	/**
	 * Implements the query to insert a ObjParuse. All information needed is stored
	 * into the input <code>ObjParuse</code> object.
	 * 
	 * @param aObjParuse The ObjParuse containing all insert information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void insertObjParuse(ObjParuse aObjParuse) throws EMFUserError;

	/**
	 * Implements the query to erase a ObjParuse. All information needed is stored
	 * into the input <code>ObjParuse</code> object.
	 * 
	 * @param aObjParuse The object containing all delete information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void eraseObjParuse(ObjParuse aObjParuse) throws EMFUserError;
	
	/**
	 * Returns the list of all ObjParuse objects associated to a <code>BIObjectParameter</code>,
	 * known its <code>objParId</code>.
	 * 
	 * @param objParId The input BIObjectParameter id code
	 * 
	 * @return The list of all ObjParuse objects associated
	 * 
	 * @throws EMFUserError If any exception occurred
	 */
	public List loadObjParuses(Integer objParId) throws EMFUserError;

	/**
	 * Returns the list of labels of BIObjectParameter objects that have a correlation relationship
	 * with the BIObjectParameter at input, given its id.
	 * 
	 * @param objParFatherId The id of the BIObjectParameter
	 * 
	 * @return the list of BIObjectParameter objects that have a correlation relationship
	 * with the BIObjectParameter at input
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public List getDependencies(Integer objParFatherId) throws EMFUserError;
	
	
	/**
	 * Returns the labels list of the documents containing
	 * dependencies for the parameter use identified by the id at input.
	 * 
	 * @param useId The Integer representing the use id
	 * 
	 * @return The list of BIObject objects labels
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public List getDocumentLabelsListWithAssociatedDependencies (Integer useId) throws EMFUserError;
	
	/**
	 * Returns the list of dependencies (ObjParuse objects list) for the ParameterUse object identified by the id passes at input.
	 * 
	 * @param useId The Integer representing the use id
	 * 
	 * @return The list of ObjParuse objects
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public List getAllDependenciesForParameterUse (Integer useId) throws EMFUserError;
	
	/**
	 * Implements the query to erase a ObjParuse. If the object does not exist does not do anything
	 * All information needed is stored
	 * into the input <code>ObjParuse</code> object.
	 * 
	 * @param aObjParuse The object containing all delete information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void eraseObjParuseIfExists(ObjParuse aObjParuse, Session aSession) throws EMFUserError;

	/**
	 * Returns the list of all ObjParuse objects associated to a <code>BIObjectParameter</code> via a father relationship,
	 * known its <code>objParId</code>.
	 * 
	 * @param objParId The input BIObjectParameter id code
	 * 
	 * @return The list of all ObjParuse objects associated
	 * 
	 * @throws EMFUserError If any exception occurred
	 */
	public List loadObjParusesFather(Integer objParId) throws EMFUserError;
	
}