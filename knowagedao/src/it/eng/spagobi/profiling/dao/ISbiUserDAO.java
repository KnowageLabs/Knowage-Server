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
package it.eng.spagobi.profiling.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.dao.PagedList;
import it.eng.spagobi.dao.QueryFilters;
import it.eng.spagobi.profiling.bean.SbiExtUserRoles;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.profiling.bo.UserBO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface ISbiUserDAO extends ISpagoBIDao{
	
	public SbiUser loadSbiUserByUserId(String userId) throws EMFUserError;
	
	public SbiUser loadSbiUserById(Integer id) throws EMFUserError;
	
//	public UserBO loadUserById(Integer id) throws EMFUserError;
	
	public void deleteSbiUserById(Integer id) throws EMFUserError;
	
	public void deleteSbiUserAttributeById(Integer id, Integer attrId) throws EMFUserError;
	
	public Integer saveSbiUser(SbiUser user) throws EMFUserError;
	
	public void updateSbiUserRoles(SbiExtUserRoles role) throws EMFUserError;
	
	public void updateSbiUserAttributes(SbiUserAttributes attribute) throws EMFUserError;
	
	public ArrayList<SbiExtRoles> loadSbiUserRolesById(Integer id) throws EMFUserError;
	
	public ArrayList<SbiUserAttributes> loadSbiUserAttributesById(Integer id) throws EMFUserError;
	
	public ArrayList<SbiUser> loadSbiUsers() throws EMFUserError;
	
	public ArrayList<UserBO> loadUsers() throws EMFUserError;
	
	public void updateSbiUser(SbiUser user, Integer userID) throws EMFUserError;
	
	public Integer fullSaveOrUpdateSbiUser(SbiUser user) throws EMFUserError;
	
	public PagedList<UserBO> loadUsersPagedList(QueryFilters filters, Integer offset, Integer fetchSize) throws EMFUserError;
	
	public void checkUserId(String userId, Integer id) throws EMFUserError;
	
	public Integer isUserIdAlreadyInUse(String userId);
	
//	public PagedList<UserBO> loadSbiUserListFiltered(String hsql,Integer offset, Integer fetchSize) throws EMFUserError;

}
