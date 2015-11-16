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
package it.eng.spagobi.tools.objmetadata.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetadata;

import java.util.List;

/**
 * Defines the interfaces for all methods needed to insert, modify and deleting object's metadata.
 */
public interface IObjMetadataDAO extends ISpagoBIDao{
	
	/**
	 * Loads all detail information for a metadata identified by its <code>type</code>.
	 * All these informations,  achived by a query to the DB, are stored
	 * into a <code>metadata</code> object, which is returned.
	 * 
	 * @param type The type of metadata to load (LONG_TEXT or SHORT_TEXT)
	 * 
	 * @return A <code>objMetadata</code> object containing all loaded information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List loadObjMetaDataListByType(String type) throws EMFUserError;
	
	/**
	 * Loads all detail information for a metadata identified by its <code>id</code>.
	 * All these information,  achived by a query to the DB, are stored
	 * into a <code>metadata</code> object, which is returned.
	 * 
	 * @param id The id for the metadata to load
	 * 
	 * @return A <code>objMetadata</code> object containing all loaded information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public ObjMetadata loadObjMetaDataByID(Integer id) throws EMFUserError;
	
	/**
	 * Loads all detail information for object's metadata whose label is equal to <code>label</code>.
	 * 
	 * @param label The label for the metadata to load
	 * 
	 * @return An <code>ObjMetada</code> object containing all loaded information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public ObjMetadata loadObjMetadataByLabel(String label) throws EMFUserError;
	
	/**
	 * Loads all detail information for all object's metadata. For each of them, detail
	 * information is stored into a <code>ObjMetadata</code> object. After that, all metadata
	 * are stored into a <code>List</code>, which is returned.
	 * 
	 * @return A list containing all metadata objects
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	
	public List loadAllObjMetadata() throws EMFUserError;

	/**
	 * Implements the query to modify an object's metadata. All information needed is stored
	 * into the input <code>ObjMetadata</code> object.
	 * 
	 * @param aObjMetadata The object containing all modify information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	
	public void modifyObjMetadata(ObjMetadata aObjMetadata) throws EMFUserError;
	
	/**
	 * Implements the query to insert an object's metadata. All information needed is stored
	 * into the input <code>ObjMetadata</code> object.
	 * 
	 * @param aObjMetadata The object containing all insert information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void insertObjMetadata(ObjMetadata aObjMetadata) throws EMFUserError;
	
	/**
	 * Implements the query to erase an object's metadata. All information needed is stored
	 * into the input <code>ObjMetadata</code> object.
	 * 
	 * @param aObjMetadata The object containing all delete information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */	
	public void eraseObjMetadata(ObjMetadata aObjMetadata) throws EMFUserError;

	/**
	 * Tells if a objMetadata is associated to any
	 * BI Objects. It is useful because a metadata cannot be deleted
	 * if it is used by one or more BI Objects.
	 * 
	 * @param id The objMetadata identifier
	 * 
	 * @return True if the metadata is used by one or more
	 * objects, else false
	 * 
	 * @throws EMFUserError If any exception occurred
	 */
	public boolean hasBIObjAssociated (String id) throws EMFUserError;

	
	/**
	 * Tells if a objMetadata is associated to any
	 * BI SubObjects. It is useful because a metadata cannot be deleted
	 * if it is used by one or more BI SubObjects.
	 * 
	 * @param id The objMetadata identifier
	 * 
	 * @return True if the metadata is used by one or more
	 * subobjects, else false
	 * 
	 * @throws EMFUserError If any exception occurred
	 */
	public boolean hasSubObjAssociated (String id) throws EMFUserError;

}