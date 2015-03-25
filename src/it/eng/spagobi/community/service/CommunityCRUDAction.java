/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.community.service;

import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.community.bo.CommunityManager;
import it.eng.spagobi.community.dao.ISbiCommunityDAO;
import it.eng.spagobi.community.mapping.SbiCommunity;
import it.eng.spagobi.services.exceptions.ExceptionUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;


@Path("/communityCRUD")
public class CommunityCRUDAction {
	@Context
	private HttpServletResponse servletResponse;
	@Context
	HttpSession session;

	private static Logger logger = Logger.getLogger(CommunityCRUDAction.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllCommunities(@Context HttpServletRequest req) {
		ISbiCommunityDAO commDao = null;

		List<SbiCommunity> communities;

		String communitiesJSONStr = "";
		try {
			commDao = DAOFactory.getCommunityDAO();
			
			communities = commDao.loadAllSbiCommunities();
			if(communities != null){
				ObjectMapper mapper = new ObjectMapper();    
				String innerList = mapper.writeValueAsString(communities);
				communitiesJSONStr ="{root:"+innerList+"}";
			}

		} catch (Throwable t) {
			throw new SpagoBIServiceException(
					"An unexpected error occured while instatiating the dao", t);
		}

		return communitiesJSONStr;

	}
	@DELETE
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String deleteCommunity(@Context HttpServletRequest req) {
		ISbiCommunityDAO commDao = null;

		String communitiesJSONStr = "";
		try {
			JSONObject requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
			String id = (String) requestBodyJSON.opt("communityId");
			commDao = DAOFactory.getCommunityDAO();
			if(id != null && !id.equals("")){
				commDao.deleteCommunityById(Integer.valueOf(id));
			}
			

		} catch (Throwable t) {
			throw new SpagoBIServiceException(
					"An unexpected error occured while instatiating the dao", t);
		}

		return communitiesJSONStr;

	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String saveCommunity(@Context HttpServletRequest req) {

		ISbiCommunityDAO commDao = null;
		String id =null;
		try {
			commDao = DAOFactory.getCommunityDAO();
			JSONObject requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);

			SbiCommunity community = recoverCommunityDetails(requestBodyJSON);
			
			if(community.getCommunityId() != null){
				//update
				commDao.updateSbiComunity(community);
				id= community.getCommunityId()+"";
			}else{
				//insert

				CommunityManager cm = new CommunityManager();

				Integer idInt = cm.saveCommunity(community, community.getName(), community.getOwner(), req);
				if(idInt != null){
					id = idInt+"";
				}
			}

			return ("{communityId:"+id+" }");
		} catch (SpagoBIRuntimeException ex) {
			logger.error("Cannot fill response container", ex);

			logger.debug(ex.getMessage());
			try {
				return ( ExceptionUtilities.serializeException(ex.getMessage(),null));
			} catch (Exception e) {
				logger.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException(
						"Cannot fill response container", e);
			}
		} catch (Exception ex) {
			logger.error("Cannot fill response container", ex);

		}
		return id;
	}
	
	private SbiCommunity recoverCommunityDetails (JSONObject requestBodyJSON) throws EMFUserError, SourceBeanException, IOException  {
		SbiCommunity com  = new SbiCommunity();
		Integer id=null;
		String idStr = (String)requestBodyJSON.opt("communityId");
		if(idStr!=null && !idStr.equals("")){
			id = Integer.valueOf(idStr);
		}
		String name = (String)requestBodyJSON.opt("name");	
		String description = (String)requestBodyJSON.opt("description");	
		String owner = (String)requestBodyJSON.opt("owner");
		String functCode = (String)requestBodyJSON.opt("functCode");
		
		com.setCommunityId(id);
		com.setName(name);
		com.setDescription(description);
		com.setOwner(owner);
		com.setFunctCode(functCode);
		return com;
	}
}
