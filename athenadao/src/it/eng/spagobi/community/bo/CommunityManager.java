/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.community.bo;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.community.dao.ISbiCommunityDAO;
import it.eng.spagobi.community.mapping.SbiCommunity;
import it.eng.spagobi.community.mapping.SbiCommunityUsers;
import it.eng.spagobi.community.util.CommunityUtilities;
import it.eng.spagobi.profiling.bean.SbiAttribute;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.profiling.dao.ISbiAttributeDAO;
import it.eng.spagobi.profiling.dao.ISbiUserDAO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

public class CommunityManager {
	
	static private Logger logger = Logger.getLogger(CommunityManager.class);
	
	public Integer saveCommunity(SbiCommunity community, String communityName, String userId, HttpServletRequest request){
		Integer communityId = null;
		//if user is registering to SpagoBI and inserts a community,
		//the systems checks for community existence by its name.
		ISbiCommunityDAO commDAO = DAOFactory.getCommunityDAO();

		try {
			ISbiAttributeDAO attrsDAO = DAOFactory.getSbiAttributeDAO();
			ISbiUserDAO userDao = DAOFactory.getSbiUserDAO();
			ISbiCommunityDAO commDao = DAOFactory.getCommunityDAO();
			
			//loads the user:
			SbiUser user = userDao.loadSbiUserByUserId(userId);

			if(community != null  && community.getCommunityId() != null){
				//if exists a mail is sent to the owner of the community that accepts him as 
				//member or refuse him
				
				//1. recovers the e-mail address of the community owner from the user attributes
				SbiUser owner = userDao.loadSbiUserByUserId(community.getOwner());
				
				//2. deletes user from other communities (only one community at a time)
				mngUserCommunityAfterDelete(user);
				logger.debug("User-community membership deleted");
				
				List communities = commDao.loadSbiCommunityByUser(userId);
				if (communities != null){
					for (int i=0; i<communities.size(); i++){
						SbiCommunity comm = (SbiCommunity)communities.get(i);
						commDao.deleteMemberFromCommunity(userId, comm.getCommunityId());
						logger.debug("Erase user by community " + comm.getName() + " for new insertion to " + community.getName());
					}					
				}
				SbiAttribute attrMail = attrsDAO.loadSbiAttributeByName("email");
				if(attrMail != null){
					Integer attrId = attrMail.getAttributeId();
					SbiUserAttributes userAttr= attrsDAO.loadSbiAttributesByUserAndId(owner.getId(), attrId);
					String emailValue = userAttr.getAttributeValue();
					
					//3. sends the email
					CommunityUtilities communityUtil = new CommunityUtilities();
					boolean result = communityUtil.dispatchMail(communityName, user, owner, emailValue, request);
				}else{
					logger.info("Owner doesn't have an email address");
				}
				
			}else{
				//if doesn't exist then the community is created, together with a new folder with 
				//the name of the community (functionality code)	
			
				Random generator = new Random();
				int randomInt = generator.nextInt();
				//1.creates a folder:
				LowFunctionality aLowFunctionality = new LowFunctionality();
				
				ILowFunctionalityDAO lowFunct = DAOFactory.getLowFunctionalityDAO();
				LowFunctionality root = lowFunct.loadRootLowFunctionality(false);
				
				aLowFunctionality.setCodType("COMMUNITY_FUNCT");
				String code = "community"+Integer.valueOf(randomInt).toString();
				aLowFunctionality.setCode(code);
				aLowFunctionality.setName(communityName);
				aLowFunctionality.setPath("/"+communityName);	
				aLowFunctionality.setParentId(root.getId());

				//2.populates community bean
				if(community == null){
					community = populateCommunity(userId, communityName, code);				
				}
								
				//3. deletes user from other communities (only one community at a time)
				mngUserCommunityAfterDelete(user);
				logger.debug("User-community membership deleted");
					
				List communities = commDao.loadSbiCommunityByUser(userId);
				if (communities != null){
					for (int i=0; i<communities.size(); i++){
						SbiCommunity comm = (SbiCommunity)communities.get(i);
						commDao.deleteCommunityById(comm.getCommunityId());
						logger.debug("Erase user by community " + comm.getName() + " for new insertion to " + community.getName());
					}					
				}
				//4. saves community and user-community relashionship
				communityId = commDAO.saveSbiComunityUsers(community, userId);
				
				Integer functId = lowFunct.insertCommunityFunctionality(aLowFunctionality);
				
				//5. add roles for the user				
				addRolesToFunctionality(userId, code);
			}
		} catch (EMFUserError e) {
			logger.error(e.getMessage());
		}
		
		return communityId;
		
	}
	private SbiCommunity populateCommunity(String userId, 
			String communityName,
			String functCode){
		SbiCommunity community = new SbiCommunity();
		community.setName(communityName);
		community.setDescription(communityName);
		community.setFunctCode(functCode);
		community.setOwner(userId);
		
		return community;
		
	}
	public void addRolesToFunctionality(String userId, String functCode) throws EMFUserError{
		ISbiUserDAO userDao = DAOFactory.getSbiUserDAO();
		IRoleDAO roledao= DAOFactory.getRoleDAO();
		ILowFunctionalityDAO lowFunctDao = DAOFactory.getLowFunctionalityDAO();
		SbiUser user = userDao.loadSbiUserByUserId(userId);
		
		ArrayList<SbiExtRoles> userRoles = userDao.loadSbiUserRolesById(user.getId());
		LowFunctionality funct = lowFunctDao.loadLowFunctionalityByCode(functCode, false);
		Role [] execRole4Funct = funct.getExecRoles();
		
		ArrayList<Role> roles = new ArrayList<Role>();
		Set<Integer> roleIds = new HashSet<Integer>();
		for(int j=0; j<execRole4Funct.length; j++){
			Role alreadySetRole = execRole4Funct[j];
			roles.add(alreadySetRole);
			roleIds.add(alreadySetRole.getId());
		}
		for(int i =0; i<userRoles.size();i++){
			SbiExtRoles extr = userRoles.get(i);
			Integer extRID= extr.getExtRoleId();
			if(!roleIds.contains(extRID)){
				Role r = roledao.loadByID(extRID);
				roles.add(r);
			}					
		}
		Role [] rolesArr = roles.toArray(new Role[roles.size()]);
		
		
		funct.setDevRoles(rolesArr);
		funct.setExecRoles(rolesArr);
		funct.setTestRoles(rolesArr);
		funct.setCreateRoles(rolesArr);
		
		lowFunctDao.modifyLowFunctionality(funct);
	}
	/**This method executes the following actions after user deletion:
	 * - if user is owner of a community and there are no other members--> the community is deleted
	 * - if user is owner of a community and there are other members --> the ownership shifts to the oldest member
	 * - if he is just a member --> the relationship with the community is deleted
	 * @param userId the user that has been deleted
	 * @throws EMFUserError 
	 */
	public void mngUserCommunityAfterDelete(SbiUser user) throws EMFUserError{
		logger.debug("IN");
		ISbiCommunityDAO commDao = DAOFactory.getCommunityDAO();
		List <SbiCommunity> communitiesOwned= commDao.loadSbiCommunityByOwner(user.getUserId());
		if(communitiesOwned != null && !communitiesOwned.isEmpty()){
			//find other members
			for(int i=0; i<communitiesOwned.size(); i++){
				SbiCommunity commOwned = communitiesOwned.get(i);
				List<SbiCommunityUsers> members= commDao.loadCommunitieMembersByName(commOwned, user);
				if(members != null && !members.isEmpty()){
					//takes the first (ordered query resultset)
					SbiCommunityUsers membership = members.get(0);
					String newOwnerId = membership.getId().getUserId();
					commOwned.setOwner(newOwnerId);
					commDao.updateSbiComunity(commOwned);
					logger.debug("New owner "+newOwnerId+" for community "+commOwned.getName());
				}else{
					commDao.deleteCommunityById(commOwned.getCommunityId());
					logger.debug("Deleted owner community "+commOwned.getName());
				}
			}			
		}
		//in any case delete relationship
		commDao.deleteCommunityMembership(user.getUserId());
		logger.debug("Deleted community memberships for user "+user.getUserId());
		logger.debug("OUT");
	}
}
