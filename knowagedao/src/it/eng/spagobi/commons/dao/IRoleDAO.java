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

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleDataSetCategory;
import it.eng.spagobi.commons.bo.RoleMetaModelCategory;
import it.eng.spagobi.commons.metadata.SbiAuthorizations;
import it.eng.spagobi.commons.metadata.SbiAuthorizationsRoles;
import it.eng.spagobi.commons.metadata.SbiExtRoles;

import java.util.List;

import org.hibernate.Session;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Defines the interfaces for all methods needed to insert, modify and deleting a role
 *
 * @author Zoppello
 */
public interface IRoleDAO extends ISpagoBIDao {

	/**
	 * Loads a role identified by its <code>roleID</code>. All these information, are stored into a <code>Role</code> object, which is returned.
	 *
	 * @param roleID
	 *            The id for the role to load
	 *
	 * @return A <code>Role</code> object containing all loaded information
	 *
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public Role loadByID(Integer roleID) throws EMFUserError;

	public SbiExtRoles loadSbiExtRoleById(Integer roleId) throws EMFUserError;

	/**
	 * Loads a role identified by its <code>roleName</code>. All these information, are stored into a <code>Role</code> object, which is returned.
	 *
	 * @param roleName
	 *            The name for the role to load
	 *
	 * @return A <code>Role</code> object containing all loaded information
	 *
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public Role loadByName(String roleName) throws EMFUserError;

	/**
	 * Loads all detail information for all roles. For each of them, detail information is stored into a <code>Role</code> object. After that, all roles are
	 * stored into a <code>List</code>, which is returned.
	 *
	 * @return A list containing all role objects
	 *
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public List loadAllRoles() throws EMFUserError;

	public List loadRolesItem(JSONObject item) throws EMFUserError, JSONException;

	/**
	 * Implements the query to insert a role. All information needed is stored into the input <code>Role</code> object.
	 *
	 * @param aRole
	 *            The object containing all insert information
	 *
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public void insertRole(Role aRole) throws EMFUserError;

	/**
	 * Implements the query to erase a role. All information needed is stored into the input <code>Role</code> object.
	 *
	 * @param aRole
	 *            The object containing all delete information
	 *
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public void eraseRole(Role aRole) throws EMFUserError;

	/**
	 * Implements the query to modify a role. All information needed is stored into the input <code>Role</code> object.
	 *
	 * @param aRole
	 *            The object containing all modify information
	 *
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public void modifyRole(Role aRole) throws EMFUserError;

	/**
	 * Gets all free roles for Insert. When a parameter has some parameter use modes associated, this association happens with one or more roles. For the same
	 * parameter, roles belonging to a parameter use mode cannot be assigned to others. So, when a new parameter use mode has to be inserted/modifies, this
	 * methods gives the roles that are still free.
	 *
	 * @param parameterID
	 *            The parameter id
	 *
	 * @return The free roles list
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public List loadAllFreeRolesForInsert(Integer parameterID) throws EMFUserError;

	/**
	 * Gets all free roles for detail. When a parameter has some parameter use modes associated, this association happens with one or more roles. For the same
	 * parameter, roles belonging to a parameter use mode cannot be assigned to others. So, when a parameter use mode detail is required, this methods gives the
	 * roles that are still free.
	 *
	 * @param parUseID
	 *            The parameter use mode id
	 *
	 * @return The free roles list
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public List loadAllFreeRolesForDetail(Integer parUseID) throws EMFUserError;

	/**
	 * Gets all the functionalities associated to the role.
	 *
	 * @param roleID
	 *            The role id
	 *
	 * @return The functionalities associated to the role
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public List LoadFunctionalitiesAssociated(Integer roleID) throws EMFUserError;

	/**
	 * Gets all the parameter uses associated to the role.
	 *
	 * @param roleID
	 *            The role id
	 *
	 * @return The parameter uses associated to the role
	 *
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public List LoadParUsesAssociated(Integer roleID) throws EMFUserError;

	/**
	 * Implements the query to insert a role. All information needed is stored into the input <code>Role</code> object.
	 *
	 * @param aRole
	 *            The object containing all insert information, includig the role abilitations
	 * @return The role id
	 *
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public Integer insertRoleComplete(Role aRole) throws EMFUserError;

	public List<Role> loadPagedRolesList(Integer offset, Integer fetchSize) throws EMFUserError;

	public Integer countRoles() throws EMFUserError;

	/*
	 * Methods for managing Role - MetaModelCategory association
	 */

	public void insertRoleMetaModelCategory(Integer roleId, Integer categoryId) throws EMFUserError;

	public void removeRoleMetaModelCategory(Integer roleId, Integer categoryId) throws EMFUserError;

	public List<RoleMetaModelCategory> getMetaModelCategoriesForRole(Integer roleId) throws EMFUserError;

	/*
	 * Methods for managing Role - DataSetCategory association
	 */

	public void insertRoleDataSetCategory(Integer roleId, Integer categoryId) throws EMFUserError;

	public void removeRoleDataSetCategory(Integer roleId, Integer categoryId) throws EMFUserError;

	public List<RoleDataSetCategory> getDataSetCategoriesForRole(Integer roleId) throws EMFUserError;

	/*
	 * Methods for managing Role - Authorization association
	 */

	public List<SbiAuthorizations> loadAllAuthorizations() throws EMFUserError;

	public SbiAuthorizations insertAuthorization(String authorizationName, String productType) throws EMFUserError;

	public List<SbiAuthorizations> LoadAuthorizationsAssociatedToRole(Integer roleID) throws EMFUserError;

	public List<SbiAuthorizationsRoles> LoadAuthorizationsRolesAssociatedToRole(Integer roleID) throws EMFUserError;

	public void eraseAuthorizationsRolesAssociatedToRole(Integer roleID, Session currSessionDB) throws EMFUserError;

	public List<SbiAuthorizations> loadAllAuthorizationsByProductTypes(List<Integer> productTypesIds) throws EMFUserError;

	List<String> loadAllAuthorizationsNamesByProductTypes(List<Integer> productTypesIds) throws EMFUserError;

}