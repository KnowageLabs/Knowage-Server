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
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParview;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

import java.util.List;

import org.hibernate.Session;

/**
 * Defines the interfaces for all methods needed to insert, modify and deleting a
 * ObjParview.
 * 
 * @author Gavardi
 *
 */
public interface IObjParviewDAO extends ISpagoBIDao{
	
	/**
	 * Loads the list of ObjParview associated to the input
	 * <code>objParId</code> and <code>paruseId</code>. All these information,
	 * achived by a query to the DB, are stored into a List of <code>ObjParview</code> object,
	 * which is returned.
	 * 
	 * @param objParId The id for the BI object parameter to load
	 * @param parvireId The parameterUse-id for the Parameter to load
	 * 
	 * @return A List of <code>ObjParuse</code> object containing all loaded information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List loadObjParview(Integer objParId, Integer parviewId) throws EMFUserError;

	/**
	 * Implements the query to modify a ObjParview. All information needed is stored
	 * into the input <code>ObjParview</code> object.
	 * 
	 * @param aObjParview The ObjParview containing all modify information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void modifyObjParview(ObjParview aObjParview) throws EMFUserError;

	/**
	 * Implements the query to insert a ObjParview. All information needed is stored
	 * into the input <code>ObjParview</code> object.
	 * 
	 * @param aObjParview The ObjParview containing all insert information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void insertObjParview(ObjParview aObjParview) throws EMFUserError;

	/**
	 * Implements the query to erase a ObjParview. All information needed is stored
	 * into the input <code>ObjParview</code> object.
	 * 
	 * @param aObjParview The object containing all delete information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void eraseObjParview(ObjParview aObjParview) throws EMFUserError;
	
	/**
	 * 	 Implements the query to erase a ObjParview preserving the session. All information needed is stored
	 * into the input <code>ObjParview</code> object.
	 * 
	 * @param aObjParview
	 * @param aSession
	 * @throws EMFUserError
	 */
	
	public void eraseObjParview(ObjParview aObjParview, Session aSession) throws EMFUserError; 

	/**
	 * 	 Implements the query to erase a ObjParview preserving the session. All information needed is stored
	 * into the input <code>ObjParview</code> object. If does not exist don'd do anything
	 * 
	 * @param aObjParview
	 * @param aSession
	 * @throws EMFUserError
	 */

	public void eraseObjParviewIfExists(ObjParview aObjParview, Session aSession) throws EMFUserError;

	
	/**
	 * Returns the list of all ObjParview objects associated to a <code>BIObjectParameter</code>,
	 * known its <code>objParId</code>.
	 * 
	 * @param objParId The input BIObjectParameter id code
	 * 
	 * @return The list of all ObjParview objects associated
	 * 
	 * @throws EMFUserError If any exception occurred
	 */
	public List loadObjParviews(Integer objParId) throws EMFUserError;

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
	 * dependencies for the parameter view identified by the id at input.
	 * 
	 * @param viewId The Integer representing the view id
	 * 
	 * @return The list of BIObject objects labels
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public List getDocumentLabelsListWithAssociatedDependencies (Integer viewId) throws EMFUserError;
	
	/**
	 * Returns the list of dependencies (ObjParview objects list) for the Parameterview object identified by the id passes at input.
	 * 
	 * @param viewId The Integer representing the view id
	 * 
	 * @return The list of ObjParview objects
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public List getAllDependenciesForParameterview (Integer viewId) throws EMFUserError;
	
	
	/**
	 * Returns the list of all ObjParview objects associated to a <code>BIObjectParameter</code> by a father relationship,
	 * known its <code>objParId</code>.
	 * 
	 * @param objParId The input BIObjectParameter id code
	 * 
	 * @return The list of all ObjParview objects associated
	 * 
	 * @throws EMFUserError If any exception occurred
	 */
	public List loadObjParviewsFather(Integer objParId) throws EMFUserError;
}