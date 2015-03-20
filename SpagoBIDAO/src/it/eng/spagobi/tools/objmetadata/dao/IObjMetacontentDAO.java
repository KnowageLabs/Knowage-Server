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
import it.eng.spagobi.tools.objmetadata.bo.ObjMetacontent;

import java.util.List;

/**
 * Defines the interfaces for all methods needed to insert, modify and deleting object's metadata.
 */
public interface IObjMetacontentDAO extends ISpagoBIDao{
	
	
	/**
	 * Loads all detail information for a metadata content identified by its <code>id</code>.
	 * All these information,  achived by a query to the DB, are stored
	 * into a <code>metadata</code> object, which is returned.
	 * 
	 * @param id The id for the metadata to load
	 * 
	 * @return A <code>objMetadata</code> object containing all loaded information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public ObjMetacontent loadObjMetaContentByID(Integer id) throws EMFUserError;
	
	/**
	 * Loads all detail information for object's metadata content whose objMetaId is equal to <code>objMetaId</code>.
	 * 
	 * @param objMetaId The objMetaId for the metadata to load
	 * 
	 * @return A list containing all metadata objects
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List loadObjMetacontentByObjMetaId(Integer objMetaId) throws EMFUserError;
	
	/**
	 * Loads all detail information for object's metadata content whose objMetaId is equal to <code>objMetaId</code>
	 * and biobj_id is equal to <code>biObjId</code>.
	 * 
	 * @param objMetaId The objMetaId for the metadata to load
	 * @param biObjId The biObjId for the object to load
	 * @param subObjId The subObjId for the subObject to load
	 * 
	 * @return A list containing all metadata objects (it should contains ever only one element)
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public ObjMetacontent loadObjMetacontent(Integer objMetaId, Integer biObjId, Integer subObjId) throws EMFUserError;

	
	/**
	 * Loads all metacontent for one object or for one subobject, if biObjId is not null load all metacontents for object, if subObjId is not null load all metacontents for subobject 
	 * 
	 * @param biObjId The biObjId for the object to load
	 * @param subObjId The subObjId for the subObject to load
	 * 
	 * @return A list containing all metadata objects 
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List loadObjOrSubObjMetacontents(Integer biObjId, Integer subObjId) throws EMFUserError;

	
	/**
	 * Loads all detail information for all object's metadata. For each of them, detail
	 * information is stored into a <code>ObjMetadata</code> object. After that, all metadata
	 * are stored into a <code>List</code>, which is returned.
	 * 
	 * @return A list containing all metadata objects
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	
	public List loadAllObjMetacontent() throws EMFUserError;

	/**
	 * Implements the query to modify an object's metadata content. All information needed is stored
	 * into the input <code>ObjMetacontent</code> object.
	 * 
	 * @param aObjMetacontent The object containing all modify information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	
	public void modifyObjMetacontent(ObjMetacontent aObjMetacontent) throws EMFUserError;
	
	/**
	 * Implements the query to insert an object's metadata content. All information needed is stored
	 * into the input <code>ObjMetacontent</code> object.
	 * 
	 * @param aObjMetacontent The object containing all insert information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void insertObjMetacontent(ObjMetacontent aObjMetacontent) throws EMFUserError;
	
	/**
	 * Implements the query to erase an object's metadata content. All information needed is stored
	 * into the input <code>ObjMetacontent</code> object.
	 * 
	 * @param aObjMetacontent The object containing all delete information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */	
	public void eraseObjMetadata(ObjMetacontent aObjMetacontent) throws EMFUserError;


}