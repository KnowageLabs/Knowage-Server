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
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjPar;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

import java.util.List;

import org.hibernate.Session;

/**
 * Defines the interfaces for all methods needed to insert, modify and deleting a
 * BI Object Parameter.
 * 
 * @author Zoppello
 *
 */
public interface IBIObjectParameterDAO extends ISpagoBIDao{
	
	
	/**
	 * Loads all detail information for a BI Object Parameter identified by its
	 * <code>objParId</code>.
	 * All these information, achived by a query to the DB, are stored into a
	 * <code>SbiObjPar</code> object, which is returned.
	 * 
	 * @param id The id for the BI object parameter to load
	 * 
	 * @return A <code>SbiObjPar</code> object containing all loaded information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	
	public SbiObjPar loadById(Integer id) throws EMFUserError;

	
	/** 
	 * Loads all detail information for a BI Object Parameter identified by its
	 * <code>objParId</code>. return a wrapper object
 * @return
 * @throws EMFUserError
 */
	
	public BIObjectParameter loadBiObjParameterById(Integer id) throws EMFUserError;
	
	/**
	 * Loads all detail information for a BI Object Parameter identified by its
	 * <code>objParId</code>.
	 * All these information, achived by a query to the DB, are stored into a
	 * <code>BIObjectParameter</code> object, which is returned.
	 * 
	 * @param objParId The id for the BI object parameter to load
	 * 
	 * @return A <code>BIObjectParameter</code> object containing all loaded information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	
	public BIObjectParameter loadForDetailByObjParId(Integer objParId) throws EMFUserError;

	

	/**
	 * Implements the query to modify a BI Object parameter. All information needed is stored
	 * into the input <code>BIObjectParameter</code> object.
	 * 
	 * @param aBIObjectParameter The object containing all modify information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void modifyBIObjectParameter(BIObjectParameter aBIObjectParameter) throws EMFUserError;

	/**
	 * Implements the query to insert a BI Object Parameter. All information needed is stored
	 * into the input <code>BIObjectParameter</code> object.
	 * 
	 * @param aBIObjectParameter The object containing all insert information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void insertBIObjectParameter(BIObjectParameter aBIObjectParameter) throws EMFUserError;

	/**
	 * Implements the query to erase a BIObjectParameter. All information needed is stored
	 * into the input <code>aBIObjectParameter</code> object.
	 * 
	 * @param aBIObjectParameter The object containing all delete information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void eraseBIObjectParameter(BIObjectParameter aBIObjectParameter, boolean alsoDependencies) throws EMFUserError;

	/**
	 * Returns the labels list of document using the parameter identified by the id at input.
	 * 
	 * @param parId The BI object Parameter id
	 * 
	 * @return The labels lis
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List getDocumentLabelsListUsingParameter(Integer parId) throws EMFUserError;
	
	/**
	 * Returns the list of all BIObject parameters associated to a <code>BIObject</code>,
	 * known its <code>biObjectID>/code>.
	 * 
	 * @param biObjectID The input BI object id code
	 * 
	 * @return The list of all BI Object Parameters associated
	 * 
	 * @throws EMFUserError If any exception occurred
	 */
	public List loadBIObjectParametersById(Integer biObjectID) throws EMFUserError ;
	
	/** erase alld ependencies of a given object parameterr
	 *  
	 * @param aBIObjectParameter
	 * @param aSession
	 */
	
	public void eraseBIObjectParameterDependencies(BIObjectParameter aBIObjectParameter, Session aSession) throws EMFUserError;

	
	/** erase all Object parameters referring to the Obj with id biObjId
	 *  
	 * @param aBIObjectParameter
	 * @param aSession
	 */
	public void eraseBIObjectParametersByObjectId(Integer biObjId,  Session currSession) throws EMFUserError;
	
}