/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.analiticalmodel.functionalitytree.dao;

import java.util.List;
import java.util.Set;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.UserFunctionality;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

/**
 * Defines the interfaces for all methods needed to insert, modify and deleting a low functionality.
 */

public interface ILowFunctionalityDAO extends ISpagoBIDao {

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
	boolean checkUserRootExists(String username) throws EMFUserError;

	/**
	 * Insert user functionality.
	 *
	 * @param userfunct the userfunct
	 *
	 * @throws EMFUserError the EMF user error
	 */
	void insertUserFunctionality(UserFunctionality userfunct) throws EMFUserError;

	/* ********* end luca changes ***************** */

	List<LowFunctionality> loadFunctionalitiesForSharing(Integer docId);

	/**
	 * Loads all information for a low functionality identified by its <code>functionalityID</code>. All these information, are stored into a
	 * <code>LowFunctionality</code> object, which is returned.
	 *
	 * @param functionalityID  The id for the low functionality to load
	 * @param recoverBIObjects If true the <code>LowFunctionality</code> at output will have the list of contained <code>BIObject</code> objects
	 *
	 * @return A <code>LowFunctionality</code> object containing all loaded information
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	LowFunctionality loadLowFunctionalityByID(Integer functionalityID, boolean recoverBIObjects) throws EMFUserError;

	/**
	 * Loads all information for a low functionality identified by its <code>code</code>. All these information, are stored into a <code>LowFunctionality</code>
	 * object, which is returned.
	 *
	 * @param code             The code for the low functionality to load
	 * @param recoverBIObjects If true the <code>LowFunctionality</code> at output will have the list of contained <code>BIObject</code> objects
	 *
	 * @return A <code>LowFunctionality</code> object containing all loaded information
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	LowFunctionality loadLowFunctionalityByCode(String code, boolean recoverBIObjects) throws EMFUserError;

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
	List<LowFunctionality> loadLowFunctionalityList(List<Integer> functionalityIDs) throws EMFUserError;

	/**
	 * Loads all information for a low functionality identified by its <code>functionalityPath</code>. All these information, are stored into a
	 * <code>LowFunctionality</code> object, which is returned.
	 *
	 * @param functionalityPath The path for the low functionality to load
	 * @param recoverBIObjects  If true the <code>LowFunctionality</code> at output will have the list of contained <code>BIObject</code> objects
	 *
	 * @return A <code>LowFunctionality</code> object containing all loaded information
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	LowFunctionality loadLowFunctionalityByPath(String functionalityPath, boolean recoverBIObjects) throws EMFUserError;

	/**
	 * Implements the query to modify a low functionality. All information needed is stored into the input <code>LowFunctionality</code> object.
	 *
	 * @param aLowFunctionality The object containing all modify information
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	void modifyLowFunctionality(LowFunctionality aLowFunctionality) throws EMFUserError;

	/**
	 * Implements the query to insert a low functionality. All information needed is stored into the input <code>LowFunctionality</code> object.
	 *
	 * @param aLowFunctionality The object containing all insert information
	 * @param profile           the profile
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	LowFunctionality insertLowFunctionality(LowFunctionality aLowFunctionality, IEngUserProfile profile) throws EMFUserError;

	/**
	 * Implements the query to erase a low functionality. All information needed is stored into the input <code>LowFunctionality</code> object.
	 *
	 * @param aLowFunctionality The object containing all erase information
	 * @param profile           the profile
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	void eraseLowFunctionality(LowFunctionality aLowFunctionality, IEngUserProfile profile) throws EMFUserError;

	/**
	 * Control if exist a functionality with the given code.
	 *
	 * @param code The code of the functionality
	 *
	 * @return The functionality ID
	 *
	 * @throws EMFUserError the EMF user error
	 */
	Integer existByCode(String code) throws EMFUserError;

	/**
	 * Control if the functionality with the given id has childs.
	 *
	 * @param id Integer id of the functionality
	 *
	 * @return true, if checks for child
	 *
	 * @throws EMFUserError the EMF user error
	 */
	boolean hasChild(Integer id) throws EMFUserError;

	/**
	 * Delete inconsistent roles.
	 *
	 * @param set the set
	 *
	 * @throws EMFUserError the EMF user error
	 */
	void deleteInconsistentRoles(Set set) throws EMFUserError;

	/**
	 * Loads all the functionalities.
	 *
	 * @param recoverBIObjects If true each <code>LowFunctionality</code> at output will have the list of contained <code>BIObject</code> objects
	 *
	 * @return the list of functionalities
	 *
	 * @throws EMFUserError the EMF user error
	 */
	List<LowFunctionality> loadAllLowFunctionalities(boolean recoverBIObjects) throws EMFUserError;

	/**
	 * Loads all the functionalities.
	 *
	 * @param recoverBIObjects If true each <code>LowFunctionality</code> at output will have the list of contained <code>BIObject</code> objects
	 *
	 * @return the list of functionalities
	 *
	 * @throws EMFUserError the EMF user error
	 */
	List<LowFunctionality> loadAllLowFunctionalities(boolean recoverBIObjects, List<String> allowedDocTypes) throws EMFUserError;

	/**
	 * Loads all the sub functionalities of the given initial path.
	 *
	 * @param initialPath      The String representing the initial path
	 * @param recoverBIObjects If true each <code>LowFunctionality</code> at output will have the list of contained <code>BIObject</code> objects
	 *
	 * @return the list of functionalities
	 *
	 * @throws EMFUserError the EMF user error
	 */
	List<LowFunctionality> loadSubLowFunctionalities(String initialPath, boolean recoverBIObjects) throws EMFUserError;

	/**
	 * Loads all the child functionalities of the given parent functionality.
	 *
	 * @param parentId         The Integer representing the parent id
	 * @param recoverBIObjects If true each <code>LowFunctionality</code> at output will have the list of contained <code>BIObject</code> objects
	 *
	 * @return the list of functionalities
	 *
	 * @throws EMFUserError the EMF user error
	 */
	List<LowFunctionality> loadChildFunctionalities(Integer parentId, boolean recoverBIObjects) throws EMFUserError;

	/**
	 * Loads the root functionality.
	 *
	 * @param recoverBIObjects If true each <code>LowFunctionality</code> at output will have the list of contained <code>BIObject</code> objects
	 *
	 * @return the root functionality
	 *
	 * @throws EMFUserError the EMF user error
	 */
	LowFunctionality loadRootLowFunctionality(boolean recoverBIObjects) throws EMFUserError;

	/**
	 * Moves up the functionality specified by the id at input in the functionalities tree.
	 *
	 * @param functionalityID the functionality id
	 *
	 * @throws EMFUserError the EMF user error
	 */
	void moveUpLowFunctionality(Integer functionalityID) throws EMFUserError;

	/**
	 * Moves down the functionality specified by the id at input in the functionalities tree.
	 *
	 * @param functionalityID the functionality id
	 *
	 * @throws EMFUserError the EMF user error
	 */
	void moveDownLowFunctionality(Integer functionalityID) throws EMFUserError;

	/**
	 * Loads the user's functionalities.
	 *
	 * @param onlyFirstLevel   If true returns only first level functionalities, if else all
	 * @param recoverBIObjects If true each <code>LowFunctionality</code> at output will have the list of contained <code>BIObject</code> objects
	 * @param profile          the user profile
	 *
	 * @return the root functionality
	 *
	 * @throws EMFUserError the EMF user error
	 */
	List<LowFunctionality> loadUserFunctionalities(Integer parentId, boolean recoverBIObjects, IEngUserProfile profile) throws EMFUserError;

	/**
	 * Load all functionalities associated the user roles.
	 *
	 * @param onlyFirstLevel   limits functionalities to first level
	 * @param recoverBIObjects the recover bi objects
	 *
	 * @return the list
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO#loadAllLowFunctionalities(boolean)
	 */
	List<LowFunctionality> loadUserFunctionalitiesFiltered(Integer parentId, boolean recoverBIObjects, IEngUserProfile profile, String permission)
			throws EMFUserError;

	/**
	 * Load all fathers functionalities to root level.
	 *
	 * @param functId the identifier of functionality child
	 * @return the list
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 */
	List<LowFunctionality> loadParentFunctionalities(Integer functId, Integer rootFolderID) throws EMFUserError;

	/**
	 * Load all functionalities with type USER_FUNCT
	 *
	 * @return the list
	 *
	 * @throws EMFUserError the EMF user error
	 *
	 */
	List<LowFunctionality> loadAllUserFunct() throws EMFUserError;

	/**
	 * Implements the query to insert a community functionality. All information needed is stored into the input <code>LowFunctionality</code> object.
	 *
	 * @param aLowFunctionality The object containing all insert information
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	Integer insertCommunityFunctionality(LowFunctionality aLowFunctionality) throws EMFUserError;

	List<LowFunctionality> loadAllLowFunctionalities(String dateFilter) throws EMFUserError;

	List<LowFunctionality> loadAllLowFunctionalities(boolean recoverBIObjects, List<String> allowedDocTypes, String date, String status) throws EMFUserError;
}