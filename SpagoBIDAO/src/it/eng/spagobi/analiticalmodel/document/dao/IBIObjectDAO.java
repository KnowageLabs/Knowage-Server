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
package it.eng.spagobi.analiticalmodel.document.dao;

import java.util.List;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

/**
 * Defines the interfaces for all methods needed to insert, modify and deleting 
 * a BI object.
 */
public interface IBIObjectDAO extends ISpagoBIDao{

	/**
	 * Loads all  information for the execution of a BI Object identified by its
	 * <code>id</code> and its <code>role</code>. All these information,
	 * achived by a query to the DB, are stored into a <code>BIObject</code> object,
	 * which is returned.
	 * 
	 * @param id The BI object id
	 * @param role The BI object role
	 * 
	 * @return The BIobject execution information, stored into a <code>BIObject</code>
	 * 
	 * @throws EMFUserError If an Exception occurs
	 */
	public  BIObject loadBIObjectForExecutionByIdAndRole(Integer id, String role)
			throws EMFUserError;
	/**
	 * Loads all  information for the execution of a BI Object identified by its
	 * <code>label</code> and its <code>role</code>. All these information,
	 * achived by a query to the DB, are stored into a <code>BIObject</code> object,
	 * which is returned.
	 * 
	 * @param label The BI object label
	 * @param role The BI object role
	 * 
	 * @return The BIobject execution information, stored into a <code>BIObject</code>
	 * 
	 * @throws EMFUserError If an Exception occurs
	 */	
	public  BIObject loadBIObjectForExecutionByLabelAndRole(String label, String role)
			throws EMFUserError;
	/**
	 * Loads all  detail information  for a BI Object identified by its
	 * <code>label</code> identifier. All these information,
	 * achived by a query to the DB, are stored into a <code>BIObject</code> object,
	 * which is returned.
	 * 
	 * @param label The BI object label identifier
	 * 
	 * @return The BI object detail information, stored into a <code>BIObject</code>
	 * 
	 * @throws EMFUserError If an Exception occurs
	 */
	public BIObject loadBIObjectByLabel(String label)
			throws EMFUserError;
	
	/**
	 * Loads all  detail information  for a BI Object identified by its
	 * <code>biObjectID</code> identifier. All these information,
	 * achived by a query to the DB, are stored into a <code>BIObject</code> object,
	 * which is returned.
	 * 
	 * @param biObjectID the BI object identifier
	 * 
	 * @return The BI object detail information, stored into a <code>BIObject</code>
	 * 
	 * @throws EMFUserError If an Exception occurs
	 */
	public  BIObject loadBIObjectById(Integer biObjectID) throws EMFUserError;

	
	/**
	 * Loads all  detail information  for a BI Object identified by its
	 * <code>id</code> identifier integer. All these information,
	 * achived by a query to the DB, are stored into a <code>BIObject</code> object,
	 * which is returned.
	 * 
	 * @param id the id
	 * 
	 * @return The BI object detail information, stored into a <code>BIObject</code>
	 * 
	 * @throws EMFUserError If an Exception occurs
	 */
	public  BIObject loadBIObjectForDetail(Integer id) throws EMFUserError;
	
	/**
	 * Loads all  detail information  for a BI Object identified by its
	 * <code>path</code> in the cms. All these information,
	 * achived by a query to the DB, are stored into a <code>BIObject</code> object,
	 * which is returned.
	 * 
	 * @param path the path
	 * 
	 * @return The BI object detail information, stored into a <code>BIObject</code>
	 * 
	 * @throws EMFUserError If an Exception occurs
	 */
	public  BIObject loadBIObjectForDetail(String path) throws EMFUserError;
	
	/**
	 * Loads all  tree information  for a BI Object identified by its
	 * <code>id</code> identifier Integer. All these information,
	 * achived by a query to the DB, are stored into a <code>BIObject</code> object,
	 * which is returned.
	 * 
	 * @param id the id
	 * 
	 * @return The BI object tree information, stored into a <code>BIObject</code>
	 * 
	 * @throws EMFUserError If an Exception occurs
	 */
	public BIObject loadBIObjectForTree(Integer id) throws EMFUserError;
	
	/**
	 * Implements the query to modify a BI Object. All information needed is stored
	 * into the input <code>BIObject</code> object.
	 * 
	 * @param obj the obj
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	
	public void modifyBIObject(BIObject obj) throws EMFUserError;
	
	/**
	 * Implements the query to modify a BI Object. All information needed is stored
	 * into the input <code>BIObject</code> object.
	 * 
	 * @param obj the obj
     * @param loadParsDC the boolean that permit the loading of parameters of a document composition
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	
	public void modifyBIObject(BIObject obj, boolean loadParsDC ) throws EMFUserError;
	
	/**
	 * Implements the query to modify a BI Object and its template. All information needed is stored
	 * into the input <code>BIObject</code> and <code>ObjTemplate</code> objects.
	 * 
	 * @param objTemp The template of the biobject
	 * @param obj the obj
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void modifyBIObject(BIObject obj, ObjTemplate objTemp) throws EMFUserError;
	
	
	/**
	 * Implements the query to modify a BI Object and its template. All information needed is stored
	 * into the input <code>BIObject</code> and <code>ObjTemplate</code> objects.
	 * 
	 * @param objTemp The template of the biobject
	 * @param obj the obj
	 * @param loadParsDC the boolean that permit the loading of parameters of a document composition
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void modifyBIObject(BIObject obj, ObjTemplate objTemp, boolean loadParsDC) throws EMFUserError;
	
	/**
	 * Implements the query to insert a BIObject. All information needed is stored
	 * into the input <code>BIObject</code> object.
	 * 
	 * @param obj The object containing all insert information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void insertBIObject(BIObject obj) throws EMFUserError;
	
	/**
	 * Implements the query to insert a BIObject and its template. All information needed is stored
	 * into the input <code>BIObject</code> and <code>ObjTemplate</code> objects.
	 * 
	 * @param obj The object containing all insert information
	 * @param loadParsDC the boolean that permit the loading of parameters of a document composition
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void insertBIObject(BIObject obj, boolean loadParsDC) throws EMFUserError;
	
	/**
	 * Implements the query to insert a BIObject and its template. All information needed is stored
	 * into the input <code>BIObject</code> and <code>ObjTemplate</code> objects.
	 * 
	 * @param obj The object containing all insert information
	 * @param objTemp The template of the biobject
	 * @throws EMFUserError If an Exception occurred
	 */
	public Integer insertBIObject(BIObject obj, ObjTemplate objTemp) throws EMFUserError;
	
	/**
	 * Implements the query to insert a BIObject and its template. All information needed is stored
	 * into the input <code>BIObject</code> and <code>ObjTemplate</code> objects.
	 * 
	 * @param obj The object containing all insert information
	 * @param objTemp The template of the biobject
	 * @param loadParsDC the boolean that permit the loading of parameters of a document composition
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void insertBIObject(BIObject obj, ObjTemplate objTemp, boolean loadParsDC) throws EMFUserError;

	/**
	 * Deletes a BIObject from a functionality. If the functionality is not specified
	 * (i.e. idFunct == null), the method deletes the BIObject from all the functionalities.
	 * Then, if the BIObject is no more referenced in any
	 * functionality, deletes it completely from db and from CMS.
	 * 
	 * @param obj The object containing all delete information
	 * @param idFunct The Integer representing the functionality id
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void eraseBIObject(BIObject obj, Integer idFunct) throws EMFUserError;

	
	/**
	 * Given the id for a report and the user profile, gets the corret roles for
	 * execution.
	 * 
	 * @param id The Integer id for the report
	 * @param profile The user profile
	 * 
	 * @return The list of correct roles for execution
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List getCorrectRolesForExecution(Integer id, IEngUserProfile profile) throws EMFUserError;
	
	/**
	 * Gets the correct roles for Report execution, given only the Report's id.
	 * 
	 * @param id The Integer id for the report
	 * 
	 * @return The list of correct roles for execution
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List getCorrectRolesForExecution(Integer id) throws EMFUserError;
	
	/**
	 * Gets the biparameters associated with to a biobject.
	 * 
	 * @param aBIObject BIObject the biobject to analize
	 * 
	 * @return List, list of the biparameters associated with the biobject
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public List getBIObjectParameters(BIObject aBIObject) throws EMFUserError;
	
	/**
	 * Loads all the BIObjects.
	 * 
	 * @return the list of BIObjects
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List loadAllBIObjects() throws EMFUserError;
	
	/**
	 * Loads all the BIObjects ordered by parameter column.
	 * 
	 * @param filterOrder the filter order
	 * 
	 * @return the list of BIObjects
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List loadAllBIObjects(String filterOrder) throws EMFUserError;

	public List loadPagedObjectsList(Integer offset, Integer fetchSize)throws EMFUserError ;
	
	public Integer countBIObjects()throws EMFUserError ;
	
	/**
	 * Loads all the BIObjects that belong to sub functionalities of the given functionality path.
	 * 
	 * @param initialPath the initial path
	 * 
	 * @return the list of BIObjects
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List loadAllBIObjectsFromInitialPath(String initialPath) throws EMFUserError;
	
	/**
	 * Loads all the BIObjects that belong to sub functionalities of the given functionality path
	 * and ordered by parameter column.
	 * 
	 * @param initialPath the initial path
	 * @param filterOrder the filter order
	 * 
	 * @return the list of BIObjects
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List loadAllBIObjectsFromInitialPath(String initialPath, String filterOrder) throws EMFUserError;
	
	/**
	 * Loads all the BIObjects filtering with the input parameters.
	 * 
	 * @param type the type of the biobjects (DASH/REPORT/OLAP...); if it is null, all types will be considered
	 * @param state the type of the biobjects (REL/DEV...); if it is null, all states will be considered
	 * @param folderPath the path of the folder; if it is null, all folders will be considered
	 * 
	 * @return the list of BIObjects
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List loadBIObjects(String type, String state, String folderPath) throws EMFUserError;
	
	/**
	 * Loads all the BIObjects filtering with the input parameters.
	 * 
	 * @param folderID the identifier of the folder; if it is null, all folders will be considered
	 * @param profile the user profile
	 * 
	 * @return the list of BIObjects
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List loadBIObjects(Integer folderID, IEngUserProfile profile, boolean isPersonalFolder)	throws EMFUserError;
	
	/**
	 * Search objects with the features specified.
	 * 
	 * @param valueFilter  the value of the filter for the research
	 * @param typeFilter   the type of the filter (the operator: equals, starts...)
	 * @param columnFilter the column on which the filter is applied
	 * @param nodeFilter   the node (folder id) on which the filter is applied
	 * @param profile      the profile of the user
	 * 
	 * @return the list of BIObjects
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List searchBIObjects(String valueFilter, String typeFilter, String columnFilter,  String isGlobal, Integer nodeFilter, IEngUserProfile profile) 
		throws EMFUserError;
}
