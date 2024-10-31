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
package it.eng.spagobi.commons.dao;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleMetaModelCategory;
import it.eng.spagobi.commons.dao.es.RoleEventsEmittingCommand;
import it.eng.spagobi.commons.metadata.SbiAuthorizations;
import it.eng.spagobi.commons.metadata.SbiAuthorizationsRoles;
import it.eng.spagobi.commons.metadata.SbiExtRoles;

/**
 * Defines the interfaces for all methods needed to insert, modify and deleting a role
 *
 * WARNING : All the implementation must consider the difference between queries and commands because all the commands executed must be tracked for GDPR.
 *
 * @author Zoppello
 */
public interface IRoleDAO extends ISpagoBIDao, EmittingEventDAO<RoleEventsEmittingCommand> {

	// Query

	/**
	 * Loads a role identified by its <code>roleID</code>. All these information, are stored into a <code>Role</code> object, which is returned.
	 *
	 * @param roleID The id for the role to load
	 *
	 * @return A <code>Role</code> object containing all loaded information
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	Role loadByID(Integer roleID) throws EMFUserError;

	Role loadAllElemtnsForRoleByID(Integer roleID) throws EMFUserError;

	SbiExtRoles loadSbiExtRoleById(Integer roleId) throws EMFUserError;

	/**
	 * Loads a role identified by its <code>roleName</code>. All these information, are stored into a <code>Role</code> object, which is returned.
	 *
	 * @param roleName The name for the role to load
	 *
	 * @return A <code>Role</code> object containing all loaded information
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	Role loadByName(String roleName) throws EMFUserError;

	/**
	 * Loads all detail information for all roles. For each of them, detail information is stored into a <code>Role</code> object. After that, all roles are
	 * stored into a <code>List</code>, which is returned.
	 *
	 * @return A list containing all role objects
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	List loadAllRoles() throws EMFUserError;

	List loadAllRolesFiltereByTenant() throws EMFUserError;

	List loadRolesItem(JSONObject item) throws EMFUserError, JSONException;

	/**
	 * Gets all free roles for Insert. When a parameter has some parameter use modes associated, this association happens with one or more roles. For the same
	 * parameter, roles belonging to a parameter use mode cannot be assigned to others. So, when a new parameter use mode has to be inserted/modifies, this
	 * methods gives the roles that are still free.
	 *
	 * @param parameterID The parameter id
	 *
	 * @return The free roles list
	 *
	 * @throws EMFUserError the EMF user error
	 */
	List loadAllFreeRolesForInsert(Integer parameterID) throws EMFUserError;

	/**
	 * Gets all free roles for detail. When a parameter has some parameter use modes associated, this association happens with one or more roles. For the same
	 * parameter, roles belonging to a parameter use mode cannot be assigned to others. So, when a parameter use mode detail is required, this methods gives the
	 * roles that are still free.
	 *
	 * @param parUseID The parameter use mode id
	 *
	 * @return The free roles list
	 *
	 * @throws EMFUserError the EMF user error
	 */
	List loadAllFreeRolesForDetail(Integer parUseID) throws EMFUserError;

	/**
	 * Gets all the functionalities associated to the role.
	 *
	 * @param roleID The role id
	 *
	 * @return The functionalities associated to the role
	 *
	 * @throws EMFUserError the EMF user error
	 */
	List LoadFunctionalitiesAssociated(Integer roleID) throws EMFUserError;

	/**
	 * Gets all the parameter uses associated to the role.
	 *
	 * @param roleID The role id
	 *
	 * @return The parameter uses associated to the role
	 *
	 * @throws EMFUserError the EMF user error
	 */
	List LoadParUsesAssociated(Integer roleID) throws EMFUserError;

	List<Role> loadPagedRolesList(Integer offset, Integer fetchSize) throws EMFUserError;

	Integer countRoles() throws EMFUserError;

	/*
	 * Methods for managing Role - MetaModelCategory association
	 */

	void insertRoleMetaModelCategory(Integer roleId, Integer categoryId) throws EMFUserError;

	void removeRoleMetaModelCategory(Integer roleId, Integer categoryId) throws EMFUserError;

	List<RoleMetaModelCategory> getMetaModelCategoriesForRole(Integer roleId) throws EMFUserError;

	List<RoleMetaModelCategory> getDataSetCategoriesForRole(String roleName) throws EMFUserError;

	List<Integer> getMetaModelCategoriesForRoles(Collection<String> roles) throws EMFUserError;

	/*
	 * Methods for managing Role - Authorization association
	 */

	List<SbiAuthorizations> loadAllAuthorizations() throws EMFUserError;

	List<SbiAuthorizations> LoadAuthorizationsAssociatedToRole(Integer roleID) throws EMFUserError;

	List<SbiAuthorizationsRoles> LoadAuthorizationsRolesAssociatedToRole(Integer roleID) throws EMFUserError;

	List<SbiAuthorizations> loadAllAuthorizationsByProductTypes(List<Integer> productTypesIds) throws EMFUserError;

	List<String> loadAllAuthorizationsNamesByProductTypes(List<Integer> productTypesIds) throws EMFUserError;

	Role loadPublicRole() throws EMFUserError;

	// Commands but not directly connected to roles

	SbiAuthorizations insertAuthorization(String authorizationName, String productType) throws EMFUserError;

	// Commands

	void unsetOtherPublicRole(Session aSession);

	void eraseAuthorizationsRolesAssociatedToRole(Integer roleID, Session currSessionDB) throws EMFUserError;

	/*
	 * Methods for managing Role - DataSetCategory association
	 */

	void insertRoleDataSetCategory(Integer roleId, Integer categoryId) throws EMFUserError;

	void removeRoleDataSetCategory(Integer roleId, Integer categoryId) throws EMFUserError;

	/**
	 * Implements the query to insert a role. All information needed is stored into the input <code>Role</code> object.
	 *
	 * @param aRole The object containing all insert information, includig the role abilitations
	 * @return The role id
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	Integer insertRoleComplete(Role aRole) throws EMFUserError;

	/**
	 * Implements the query to insert a role. All information needed is stored into the input <code>Role</code> object.
	 *
	 * @param aRole The object containing all insert information
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	void insertRole(Role aRole) throws EMFUserError;

	/**
	 * Implements the query to erase a role. All information needed is stored into the input <code>Role</code> object.
	 *
	 * @param aRole The object containing all delete information
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	void eraseRole(Role aRole) throws EMFUserError;

	/**
	 * Implements the query to modify a role. All information needed is stored into the input <code>Role</code> object.
	 *
	 * @param aRole The object containing all modify information
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	void modifyRole(Role aRole) throws EMFUserError;

	// Utils

}