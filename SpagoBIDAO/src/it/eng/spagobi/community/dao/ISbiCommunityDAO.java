/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
