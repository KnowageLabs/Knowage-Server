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

/**
 * DAO for SBI_USER table and related ones.
 *
 * WARNING : All the implementation must consider the difference between queries
 * and commands because all the commands executed must be tracked for GDPR.
 *
 */
public interface ISbiUserDAO extends ISpagoBIDao {

	// Query
	public SbiUser loadSbiUserByUserId(String userId);

	public SbiUser loadSbiUserById(Integer id);

	public ArrayList<SbiExtRoles> loadSbiUserRolesById(Integer id);

	public ArrayList<SbiUserAttributes> loadSbiUserAttributesById(Integer id);

	public ArrayList<SbiUser> loadSbiUsers();

	public ArrayList<UserBO> loadUsers();

	public List<UserBO> loadUsers(QueryFilters filters);

	public List<UserBO> loadUsers(QueryFilters filters, String dateFilter);

	public PagedList<UserBO> loadUsersPagedList(QueryFilters filters, Integer offset, Integer fetchSize);

	public boolean thereIsAnyUsers();

	public int getFailedLoginAttempts(String userId);

	public Integer isUserIdAlreadyInUse(String userId);

	public void checkUserId(String userId, Integer id);

	// Commands

	public void deleteSbiUserById(Integer id);

	public void deleteSbiUserAttributeById(Integer id, Integer attrId);

	public Integer saveSbiUser(SbiUser user);

	public void updateSbiUserRoles(SbiExtUserRoles role);

	public void updateSbiUserAttributes(SbiUserAttributes attribute);

	public void updateSbiUser(SbiUser user, Integer userID);

	public Integer fullSaveOrUpdateSbiUser(SbiUser user);

	public void incrementFailedLoginAttempts(String userId);

	public void resetFailedLoginAttempts(String userId);

}
