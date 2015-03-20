/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
