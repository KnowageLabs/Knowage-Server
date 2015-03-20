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
package it.eng.spagobi.engines.config.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.engines.config.bo.Engine;

import java.util.List;

/**
 * Defines the interfaces for all methods needed to insert, modify and deleting an engine.
 * 
 * @author Zoppello
 */
public interface IEngineDAO extends ISpagoBIDao{
	
	
	/**
	 * Loads all detail information for an engine identified by its <code>engineID</code>. All these information,
	 * achived by a query to the DB, are stored into an <code>engine</code> object, which is
	 * returned.
	 * 
	 * @param engineID The id for the engine to load
	 * 
	 * @return An <code>engine</code> object containing all loaded information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public Engine loadEngineByID(Integer engineID) throws EMFUserError;
	
	
	/**
	 * Loads all detail information for an engine identified by its <code>engineLabel</code>. All these information,
	 * achived by a query to the DB, are stored into an <code>engine</code> object, which is
	 * returned.
	 * 
	 * @param engineLabel The label for the engine to load
	 * 
	 * @return An <code>engine</code> object containing all loaded information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public Engine loadEngineByLabel(String engineLabel) throws EMFUserError;
	
	/**
	 * Loads all detail information for an engine identified by its <code>driver</code>. All these information,
	 * achived by a query to the DB, are stored into an <code>engine</code> object, which is
	 * returned.
	 * 
	 * @param driver The name for the engine to load
	 * 
	 * @return An <code>engine</code> object containing all loaded information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public Engine loadEngineByDriver(String driver) throws EMFUserError;
	
	
	/**
	 * Loads all detail information for all engines. For each of them, detail
	 * information is stored into an <code>engine</code> object. After that, all engines
	 * are stored into a <code>List</code>, which is returned.
	 * 
	 * @return A list containing all engine objects
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	
	public List<Engine> loadAllEngines() throws EMFUserError;
	
	
	/**
	 * Loads all detail information for all engines in paged way. For each of them, detail
	 * information is stored into an <code>engine</code> object. After that, all engines inside the paging logic
	 * are stored into a <code>List</code>, which is returned.
	 * 
	 * @return A list containing all engine objects
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	
	public List<Engine> loadPagedEnginesList(Integer offset, Integer fetchSize) throws EMFUserError;
	
	
	/**
	 * Loads all detail information for all engines filtered by tenant. For each of them, detail
	 * information is stored into an <code>engine</code> object. After that, all engines
	 * are stored into a <code>List</code>, which is returned.
	 * 
	 * @return A list containing all engine objects
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	
	public List<Engine> loadAllEnginesByTenant() throws EMFUserError;
	/**
	 * Loads all detail information for all engines compatible to the BIObject type specified 
	 * at input and the tenant. For each of them, detail information is stored into an <code>engine</code> object.
	 * After that, all engines are stored into a <code>List</code>, which is returned.
	 * 
	 * @param biobjectType the biobject type
	 * 
	 * @return A list containing all engine objects compatible with the BIObject type passed at input
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	
	public List<Engine> loadAllEnginesForBIObjectTypeAndTenant(String biobjectType) throws EMFUserError;
	/**
	 * Loads all detail information for all engines compatible to the BIObject type specified
	 * at input. For each of them, detail information is stored into an <code>engine</code> object.
	 * After that, all engines are stored into a <code>List</code>, which is returned.
	 * 
	 * @param biobjectType the biobject type
	 * 
	 * @return A list containing all engine objects compatible with the BIObject type passed at input
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	
	public List<Engine> loadAllEnginesForBIObjectType(String biobjectType) throws EMFUserError;
	/**
	 * Implements the query to modify an engine. All information needed is stored
	 * into the input <code>engine</code> object.
	 * 
	 * @param aEngine The object containing all modify information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	
	public void modifyEngine(Engine aEngine) throws EMFUserError;
	
	/**
	 * Implements the query to insert an engine. All information needed is stored
	 * into the input <code>engine</code> object.
	 * 
	 * @param aEngine The object containing all insert information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void insertEngine(Engine aEngine) throws EMFUserError;
	
	/**
	 * Implements the query to erase an engine. All information needed is stored
	 * into the input <code>engine</code> object.
	 * 
	 * @param aEngine The object containing all delete information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	
	public void eraseEngine(Engine aEngine) throws EMFUserError;

	/**
	 * Tells if an engine is associated to any
	 * BI Object. It is useful because an engine cannot be deleted
	 * if it is used by one or more BI Objects.
	 * 
	 * @param engineId The engine identifier
	 * 
	 * @return True if the engine is used by one or more
	 * objects, else false
	 * 
	 * @throws EMFUserError If any exception occurred
	 */
	public boolean hasBIObjAssociated (String engineId) throws EMFUserError;


	/**
	 * Get all the associated Exporters
	 * 
	 * @param engineId The engine identifier
	 * 
	 * @return The list of associated Exporters
	 * 
	 * @throws EMFUserError If any exception occurred
	 */
	public List getAssociatedExporters (Engine engineId) throws EMFUserError;

	
	
	public Integer countEngines() throws EMFUserError;

}