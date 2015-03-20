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
package it.eng.spagobi.analiticalmodel.functionalitytree.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.UserFunctionality;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

import java.util.List;
import java.util.Set;


/**
 * Defines  the interfaces for all methods needed to insert, modify and deleting a low functionality.
 */


public interface ILowFunctionalityDAO extends ISpagoBIDao{
	
	
	/* ********* start luca changes *************** */
	/**
	 * Check user root exists.
	 * 
	 * @param username the username
	 * 
	 * @return true, if successful
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public boolean checkUserRootExists(String username) throws EMFUserError;
	
	/**
	 * Insert user functionality.
	 * 
	 * @param userfunct the userfunct
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public void insertUserFunctionality(UserFunctionality userfunct) throws EMFUserError;
	/* ********* end luca changes ***************** */
	
	
	/**
	 * Loads all information for a low functionality identified by its
	 * <code>functionalityID</code>. All these information, are stored into a
	 * <code>LowFunctionality</code> object, which is
	 * returned.
	 * 
	 * @param functionalityID The id for the low functionality to load
	 * @param recoverBIObjects If true the <code>LowFunctionality</code> at output will have the
	 * list of contained <code>BIObject</code> objects
	 * 
	 * @return A <code>LowFunctionality</code> object containing all loaded information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public LowFunctionality loadLowFunctionalityByID(Integer functionalityID, boolean recoverBIObjects) throws EMFUserError;
	
	/**
	 * Loads all information for a low functionality identified by its
	 * <code>code</code>. All these information, are stored into a
	 * <code>LowFunctionality</code> object, which is
	 * returned.
	 * 
	 * @param code The code for the low functionality to load
	 * @param recoverBIObjects If true the <code>LowFunctionality</code> at output will have the
	 * list of contained <code>BIObject</code> objects
	 * 
	 * @return A <code>LowFunctionality</code> object containing all loaded information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public LowFunctionality loadLowFunctionalityByCode(String code, boolean recoverBIObjects) throws EMFUserError;
	
	/**
	 * Load low functionality list by id List
	 * 
	 * @param functionalityIDs the functionality id List
	 * 
	 * @return the low functionalities List
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadLowFunctionalityByID(java.lang.Integer)
	 */
	public List loadLowFunctionalityList(List functionalityIDs) throws EMFUserError ;
	
	/**
	 * Loads all information for a low functionality identified by its
	 * <code>functionalityPath</code>. All these information, are stored into a
	 * <code>LowFunctionality</code> object, which is
	 * returned.
	 * 
	 * @param functionalityPath The path for the low functionality to load
	 * @param recoverBIObjects If true the <code>LowFunctionality</code> at output will have the
	 * list of contained <code>BIObject</code> objects
	 * 
	 * @return A <code>LowFunctionality</code> object containing all loaded information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public LowFunctionality loadLowFunctionalityByPath(String functionalityPath, boolean recoverBIObjects) throws EMFUserError;
	
	/**
	 * Implements the query to modify a low functionality. All information needed is stored
	 * into the input <code>LowFunctionality</code> object.
	 * 
	 * @param aLowFunctionality The object containing all modify information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void modifyLowFunctionality(LowFunctionality aLowFunctionality) throws EMFUserError;
	
	/**
	 * Implements the query to insert a low functionality. All information needed is stored
	 * into the input <code>LowFunctionality</code> object.
	 * 
	 * @param aLowFunctionality The object containing all insert information
	 * @param profile the profile
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void insertLowFunctionality(LowFunctionality aLowFunctionality, IEngUserProfile profile) throws EMFUserError;
	
	/**
	 * Implements the query to erase a low functionality. All information needed is stored
	 * into the input <code>LowFunctionality</code> object.
	 * 
	 * @param aLowFunctionality The object containing all erase information
	 * @param profile the profile
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void eraseLowFunctionality(LowFunctionality aLowFunctionality, IEngUserProfile profile) throws EMFUserError;
	
	/**
	 * Control if exist a functionality with the given code.
	 * 
	 * @param code  The code of the functionality
	 * 
	 * @return The functionality ID
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public Integer existByCode(String code) throws EMFUserError;
	
	/**
	 * Control if the functionality with the given id has childs.
	 * 
	 * @param id Integer id of the functionality
	 * 
	 * @return true, if checks for child
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public boolean hasChild(Integer id) throws EMFUserError;
	
	/**
	 * Delete inconsistent roles.
	 * 
	 * @param set the set
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public void deleteInconsistentRoles (Set set) throws EMFUserError;
	
	/**
	 * Loads all the functionalities.
	 * 
	 * @param recoverBIObjects If true each <code>LowFunctionality</code> at output will have the
	 * list of contained <code>BIObject</code> objects
	 * 
	 * @return the list of functionalities
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public List loadAllLowFunctionalities(boolean recoverBIObjects) throws EMFUserError;
	
	/**
	 * Loads all the sub functionalities of the given initial path.
	 * 
	 * @param initialPath The String representing the initial path
	 * @param recoverBIObjects If true each <code>LowFunctionality</code> at output will have the
	 * list of contained <code>BIObject</code> objects
	 * 
	 * @return the list of functionalities
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public List loadSubLowFunctionalities(String initialPath, boolean recoverBIObjects) throws EMFUserError;
	
	/**
	 * Loads all the child functionalities of the given parent functionality.
	 * 
	 * @param parentId The Integer representing the parent id
	 * @param recoverBIObjects If true each <code>LowFunctionality</code> at output will have the
	 * list of contained <code>BIObject</code> objects
	 * 
	 * @return the list of functionalities
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public List loadChildFunctionalities(Integer parentId, boolean recoverBIObjects) throws EMFUserError;
	
	/**
	 * Loads the root functionality.
	 * 
	 * @param recoverBIObjects If true each <code>LowFunctionality</code> at output will have the
	 * list of contained <code>BIObject</code> objects
	 * 
	 * @return the root functionality
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public LowFunctionality loadRootLowFunctionality(boolean recoverBIObjects) throws EMFUserError;
	
	/**
	 * Moves up the functionality specified by the id at input in the functionalities tree.
	 * 
	 * @param functionalityID the functionality id
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public void moveUpLowFunctionality(Integer functionalityID) throws EMFUserError;
	
	/**
	 * Moves down the functionality specified by the id at input in the functionalities tree.
	 * 
	 * @param functionalityID the functionality id
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public void moveDownLowFunctionality(Integer functionalityID) throws EMFUserError;
	
	/**
	 * Loads the user's functionalities.
	 * 
	 * @param onlyFirstLevel If true returns only first level functionalities, if else all
	 * @param recoverBIObjects If true each <code>LowFunctionality</code> at output will have the
	 * list of contained <code>BIObject</code> objects
	 * @param profile the user profile
	 * 
	 * @return the root functionality
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public List loadUserFunctionalities(Integer parentId, boolean recoverBIObjects,  IEngUserProfile profile) throws EMFUserError;
	
	/**
	 * Load all functionalities associated the user roles. 
	 * 
	 * @param onlyFirstLevel limits functionalities to first level
	 * @param recoverBIObjects the recover bi objects
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadAllLowFunctionalities(boolean)
	 */
	public List loadUserFunctionalitiesFiltered(Integer parentId, boolean recoverBIObjects, IEngUserProfile profile, String permission) throws EMFUserError ;

	/**
	 * Load all fathers functionalities to root level. 
	 * 
	 * @param functId the identifier of functionality child
	 * @return the list
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 */
	public List loadParentFunctionalities(Integer functId, Integer rootFolderID) throws EMFUserError;
	
	/**
	 * Load all functionalities with type USER_FUNCT
	 * 
	 * @return the list
	 * 
	 * @throws EMFUserError the EMF user error
	 * 
	 */
	public List loadAllUserFunct() throws EMFUserError;
	/**
	 * Implements the query to insert a community functionality. All information needed is stored
	 * into the input <code>LowFunctionality</code> object.
	 * 
	 * @param aLowFunctionality The object containing all insert information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public Integer insertCommunityFunctionality(LowFunctionality aLowFunctionality) throws EMFUserError;

}