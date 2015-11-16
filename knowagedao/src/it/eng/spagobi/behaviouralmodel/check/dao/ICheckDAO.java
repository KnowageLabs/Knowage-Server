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
package it.eng.spagobi.behaviouralmodel.check.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.behaviouralmodel.check.bo.Check;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

import java.util.List;


/**
 * Defines  the interfaces for all methods needed to insert, 
 * modify and deleting a value constraint.
 * 
 * @author Zoppello
 */
public interface ICheckDAO extends ISpagoBIDao{
	
	/**
	 * Loads all detail information for all value constraints. For each of them, detail
	 * information is stored into a <code>Check</code> object. After that,
	 * all value constraints are stored into a <code>List</code>, which is returned.
	 * 
	 * @return A list containing all value constraints objects
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List loadAllChecks() throws EMFUserError;
	
	/**
	 * Loads all detail information for a value constraint identified by its
	 * <code>id</code>. All these information are stored into a
	 * <code>Check</code> object, which is
	 * returned.
	 * 
	 * @param id The id for the value constraint to load
	 * 
	 * @return A <code>Check</code> object containing all loaded information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public Check loadCheckByID(Integer id) throws EMFUserError;
	
	/**
	 * Implements the query to erase a value constraint. All information needed is stored
	 * into the input <code>Check</code> object.
	 * 
	 * @param check The object containing all delete information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void eraseCheck(Check check) throws EMFUserError;
	
	/**
	 * Implements the query to insert a value constraint. All information needed is stored
	 * into the input <code>Check</code> object.
	 * 
	 * @param check The object containing all insert information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void insertCheck(Check check) throws EMFUserError;
	
	/**
	 * Implements the query to modify a value constraint. All information needed is stored
	 * into the input <code>Check</code> object.
	 * 
	 * @param check The object containing all modify information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void modifyCheck(Check check) throws EMFUserError;

	/**
	 * Checks if is referenced.
	 * 
	 * @param checkId the check id
	 * 
	 * @return true, if is referenced
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public boolean isReferenced (String checkId) throws EMFUserError;

}