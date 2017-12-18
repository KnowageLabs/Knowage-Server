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
package it.eng.spagobi.community.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.community.mapping.SbiCommunity;
import it.eng.spagobi.community.mapping.SbiCommunityUsers;
import it.eng.spagobi.profiling.bean.SbiUser;

import java.util.List;

public interface ISbiCommunityDAO extends ISpagoBIDao {
	
	public void deleteCommunityById(Integer id) throws EMFUserError;
	
	public void deleteCommunityMembership(String userID) throws EMFUserError;
	
	public SbiCommunity loadSbiCommunityByName(String name) throws EMFUserError;

	public Integer saveSbiComunityUsers(SbiCommunity community, String userID) throws EMFUserError;
	
	public List<SbiCommunity> loadSbiCommunityByUser(String userID) throws EMFUserError;
	
	public List<SbiCommunity> loadAllSbiCommunities() throws EMFUserError;
	
	public List<SbiCommunity> loadSbiCommunityByOwner(String userID) throws EMFUserError;
	
	public void addCommunityMember(SbiCommunity community, String userID) throws EMFUserError;
	
	public Integer saveSbiComunity(SbiCommunity community) throws EMFUserError;
	
	public Integer updateSbiComunity(SbiCommunity community) throws EMFUserError;
	
	public List<SbiCommunityUsers> loadCommunitieMembersByName(SbiCommunity community, SbiUser owner) throws EMFUserError;
	
	public void deleteMemberFromCommunity(String userID, Integer communityId) throws EMFUserError;
}
