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

import java.util.ArrayList;
import java.util.List;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.dao.PagedList;
import it.eng.spagobi.dao.QueryFilters;
import it.eng.spagobi.profiling.bean.SbiExtUserRoles;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.profiling.bo.UserBO;

public interface ISbiUserDAO extends ISpagoBIDao {

	public SbiUser loadSbiUserByUserId(String userId);

	public SbiUser loadSbiUserById(Integer id);

	public void deleteSbiUserById(Integer id);

	public void deleteSbiUserAttributeById(Integer id, Integer attrId);

	public Integer saveSbiUser(SbiUser user);

	public void updateSbiUserRoles(SbiExtUserRoles role);

	public void updateSbiUserAttributes(SbiUserAttributes attribute);

	public ArrayList<SbiExtRoles> loadSbiUserRolesById(Integer id);

	public ArrayList<SbiUserAttributes> loadSbiUserAttributesById(Integer id);

	public ArrayList<SbiUser> loadSbiUsers();

	public ArrayList<UserBO> loadUsers();

	public List<UserBO> loadUsers(QueryFilters filters);

	public List<UserBO> loadUsers(QueryFilters filters, String dateFilter);

	public void updateSbiUser(SbiUser user, Integer userID);

	public Integer fullSaveOrUpdateSbiUser(SbiUser user);

	public PagedList<UserBO> loadUsersPagedList(QueryFilters filters, Integer offset, Integer fetchSize);

	public void checkUserId(String userId, Integer id);

	public Integer isUserIdAlreadyInUse(String userId);

	public int getFailedLoginAttempts(String userId);

	public void incrementFailedLoginAttempts(String userId);

	public void resetFailedLoginAttempts(String userId);

	public boolean thereIsAnyUsers();

}
