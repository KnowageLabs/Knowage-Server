/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General License for more details.
 *
 * You should have received a copy of the GNU Affero General License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.profiling.dao;

import java.util.ArrayList;
import java.util.List;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.commons.dao.es.UserEventsEmettingCommand;
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
	SbiUser loadSbiUserByUserId(String userId);

	SbiUser loadSbiUserById(Integer id);

	ArrayList<SbiExtRoles> loadSbiUserRolesById(Integer id);

	ArrayList<SbiUserAttributes> loadSbiUserAttributesById(Integer id);

	ArrayList<SbiUser> loadSbiUsers();

	ArrayList<UserBO> loadUsers();

	List<UserBO> loadUsers(QueryFilters filters);

	List<UserBO> loadUsers(QueryFilters filters, String dateFilter);

	PagedList<UserBO> loadUsersPagedList(QueryFilters filters, Integer offset, Integer fetchSize);

	boolean thereIsAnyUsers();

	int getFailedLoginAttempts(String userId);

	Integer isUserIdAlreadyInUse(String userId);

	void checkUserId(String userId, Integer id);

	// Commands

	void deleteSbiUserById(Integer id);

	void deleteSbiUserAttributeById(Integer id, Integer attrId);

	Integer saveSbiUser(SbiUser user);

	void updateSbiUserRoles(SbiExtUserRoles role);

	void updateSbiUserAttributes(SbiUserAttributes attribute);

	void updateSbiUser(SbiUser user, Integer userID);

	Integer fullSaveOrUpdateSbiUser(SbiUser user);

	void incrementFailedLoginAttempts(String userId);

	void resetFailedLoginAttempts(String userId);

	// Utils

	void setEventEmittingCommand(UserEventsEmettingCommand command);

}
