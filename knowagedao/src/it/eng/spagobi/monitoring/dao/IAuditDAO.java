/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.monitoring.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.monitoring.metadata.SbiAudit;

import java.util.Collection;
import java.util.List;

public interface IAuditDAO extends ISpagoBIDao{

	/**
	 * Returns the list of all audit records.
	 * 
	 * @return A <code>List</code> object consisting of <code>SbiAudit</code> objects
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List loadAllAudits() throws EMFUserError;
	
	/**
	 * Returns the list of all audit records relevant to the user with the specified name at input.
	 * 
	 * @param userName The user name
	 * 
	 * @return A <code>List</code> object consisting of <code>SbiAudit</code> objects relevant to the user specified at input
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List loadAuditsByUserName(String userName) throws EMFUserError;
	
	/**
	 * Returns the list of all audit records relevant to the document with the specified label at input.
	 * 
	 * @param documentLabel The document label
	 * 
	 * @return A <code>List</code> object consisting of <code>SbiAudit</code> objects relevant to the document specified at input
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List loadAuditsByDocumentLabel(String documentLabel) throws EMFUserError;
	
	/**
	 * Returns the list of all audit records relevant to the engine with the specified label at input.
	 * 
	 * @param engineLabel The engine label
	 * 
	 * @return A <code>List</code> object consisting of <code>SbiAudit</code> objects relevant to the engine specified at input
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List loadAuditsByEngineLabel(String engineLabel) throws EMFUserError;
	
	/**
	 * Loads all detail information for an audit record identified by its <code>id</code>. All these information,
	 * achived by a query to the DB, are stored into an <code>SbiAudit</code> object, which is
	 * returned.
	 * 
	 * @param id The id for the audit record to load
	 * 
	 * @return A <code>SbiAudit</code> object containing all loaded information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public SbiAudit loadAuditByID(Integer id) throws EMFUserError;
	
	/**
	 * Insert an audit record as per the <code>SbiAudit</code> object at input.
	 * 
	 * @param aSbiAudit The <code>SbiAudit</code> object for the audit record to insert
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void insertAudit(SbiAudit aSbiAudit) throws EMFUserError;
	
	/**
	 * Modify an audit record as per the <code>SbiAudit</code> object at input.
	 * 
	 * @param aSbiAudit The <code>SbiAudit</code> object for the audit record to modify
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void modifyAudit(SbiAudit aSbiAudit) throws EMFUserError;

	/**
	 * Erase the audit record with the specified id.
	 * 
	 * @param id The id of the audit record to be erased
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void eraseAudit(Integer id) throws EMFUserError;
	
	/**
	 * Finds the most popular executions for the specified roles as a list of <code>HotLink</code> objects.
	 * 
	 * @param roles the roles
	 * @param limit the limit
	 * 
	 * @return the list of hot links
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public List getMostPopular(Collection roles, int limit) throws EMFUserError;
	
	/**
	 * Finds the most recent executions for the user with the specified user identifier as a list of <code>HotLink</code> objects.
	 * 
	 * @param userId the user id
	 * @param limit the limit
	 * 
	 * @return the list of hot links
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public List getMyRecentlyUsed(String userId, int limit) throws EMFUserError;
	
	/**
	 * Gets the last execution of a document with id objId.
	 * 
	 * @param objId the obj id
	 * 
	 * @return SbiAudit of last execution
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public SbiAudit getLastExecution(Integer objId) throws EMFUserError ;
	
	/**
	 * Gets the last execution of a document with id objId.
	 * 
	 * @param objId the obj id
	 * 
	 * @return Medium Execution Time
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public Double getMediumExecTime(Integer objId) throws EMFUserError ;
	
}
