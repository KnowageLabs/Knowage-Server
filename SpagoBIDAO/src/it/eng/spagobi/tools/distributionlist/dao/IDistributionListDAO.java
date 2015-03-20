/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.distributionlist.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.tools.distributionlist.bo.DistributionList;
import it.eng.spagobi.tools.distributionlist.bo.Email;

import java.util.List;

/**
 * Defines the interfaces for all methods needed to insert, modify and delete a Distribution List
 */
public interface IDistributionListDAO extends ISpagoBIDao{
	
	
	/**
	 * Loads all detail information for a distribution list identified by its id.
	 * All these informations,  achived by a query to the DB, are stored
	 * into a distributionlist object, which is returned.
	 * 
	 * @param Id The id for the distributionlist to load
	 * 
	 * @return A <code>distributionlist</code> object containing all loaded information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public DistributionList loadDistributionListById(Integer Id) throws EMFUserError;
	
	/**
	 * Loads all detail information for a distribution list identified by its name.
	 * All these informations,  achived by a query to the DB, are stored
	 * into a distributionlist object, which is returned.
	 * 
	 * @param name The name for the distributionlist to load
	 * 
	 * @return A <code>distributionlist</code> object containing all loaded informations
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public DistributionList loadDistributionListByName(String name) throws EMFUserError;
	
	/**
	 * Loads all distribution lists.
	 * All these informations,  achived by a query to the DB, are stored
	 * into a List object, which is returned.
	 * 
	 * @return A <code>List</code> object containing all distribution Lists
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List loadAllDistributionLists() throws EMFUserError;

	/**
	 * Implements the query to modify a distributionlist. All information needed is stored
	 * into the input <code>distributionlist</code> object.
	 * 
	 * @param aDistributionList The object containing all modified informations
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	
	public void modifyDistributionList(DistributionList aDistributionList) throws EMFUserError;
	
	/**
	 * Implements the query to insert a distribution list. All information needed is stored
	 * into the input <code>distributionlist</code> object.
	 * 
	 * @param aDistributionList The object containing all insert information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void insertDistributionList(DistributionList aDistributionList) throws EMFUserError;
	
	/**
	 * Implements the query to erase a distribution list. All information needed is stored
	 * into the input <code>distributionlist</code> object.
	 * 
	 * @param aDistributionList The object containing all informations to be deleted
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */	
	public void eraseDistributionList(DistributionList aDistributionList) throws EMFUserError;
	
	/**
	 * Implements the query to erase distribution list objects.
	 * 
	 * @param biobId The id of the document
	 * @param triggername The triggername regarding the schedulation of the document
	 * @param dl the dl
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */	
	public void eraseDistributionListObjects(DistributionList dl, int biobId, String triggername) throws EMFUserError;
	
	/**
	 * Implements the query to erase all distribution list objects with the related triggername.
	 * 
	 * @param triggername The triggername regarding the schedulation of the document
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */	
	public void eraseAllRelatedDistributionListObjects(String triggername) throws EMFUserError;

	/**
	 * Tells if a distribution list is associated to any
	 * BI Object. It is useful because the user needs to know
	 * if he's deleting an empty distribution list or not
	 * 
	 * @param dsId The distribution list identifier
	 * 
	 * @return True if the distribution list is used by one or more
	 * objects, else false
	 * 
	 * @throws EMFUserError If any exception occurred
	 */
	public boolean hasBIObjAssociated (String dsId) throws EMFUserError; 
	
	/**
	 * Subscribes user to the distribution list.
	 * 
	 * @param user The user to be subscribed
	 * @param aDistributionList the a distribution list
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void subscribeToDistributionList(DistributionList aDistributionList, Email user) throws EMFUserError;
	
	/**
	 * Unubscribes user from the distribution list.
	 * 
	 * @param user The user to be unsubscribed
	 * @param aDistributionList the a distribution list
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void unsubscribeFromDistributionList(DistributionList aDistributionList, String user) throws EMFUserError;
	
	/**
	 * Inserts the document with id objId in the list of documents for the DIstributionList dl
	 * with the xml of its schedulation.
	 * 
	 * @param objId The id of the document
	 * @param xml The xml regarding the schedulation of the document
	 * @param dl the dl
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void insertDLforDocument(DistributionList dl, int objId, String xml) throws EMFUserError ;
	
	/**
	 * Verifies if the document with id objId is already in the list of documents for the DIstributionList dl
	 * with the xml of its schedulation.
	 * 
	 * @param objId The id of the document
	 * @param xml The xml regarding the schedulation of the document
	 * @param dl the dl
	 * 
	 * @return True if already exists, else false
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public boolean isDocScheduleAlreadyLinkedToDL(DistributionList dl, int objId, String xml) throws EMFUserError ;
	
	/**
	 * Verifies if the document with id objId is already in the list of documents for the DIstributionList dl
	 * with another xml of schedulation.
	 * 
	 * @param objId The id of the document
	 * @param xml The xml regarding the schedulation of the document
	 * @param dl the dl
	 * 
	 * @return True if already exists, else false
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public boolean isDocScheduledInOtherTime(DistributionList dl, int objId, String xml) throws EMFUserError ;
	
	/**
	 * Returns a list of the Xmls related to the specified Distribution List and the specified Document.
	 * 
	 * @param objId The id of the document
	 * @param dl the dl
	 * 
	 * @return List of Xml related to the DL and the doc with id objId
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public List getXmlRelated(DistributionList dl, int objId) throws EMFUserError ;

}
