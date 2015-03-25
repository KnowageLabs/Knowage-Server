/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.community.service;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.RequestContainerAccess;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.community.bo.CommunityManager;
import it.eng.spagobi.community.dao.ISbiCommunityDAO;
import it.eng.spagobi.community.mapping.SbiCommunity;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.themes.ThemesManager;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/community")
public class MenageCommunityAction {
	protected IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
	protected Locale locale = null;	
	private static Logger logger = Logger.getLogger(MenageCommunityAction.class);

	
	@GET
	@Path("/accept")
	@Produces(MediaType.TEXT_HTML)
	public String accept(@Context HttpServletRequest req) {
		
		RequestContainer reqCont = RequestContainerAccess.getRequestContainer(req);
		if(reqCont != null){
			SessionContainer aSessionContainer = reqCont.getSessionContainer();
	
			SessionContainer permanentSession = aSessionContainer.getPermanentContainer();
			String curr_language=(String)permanentSession.getAttribute(SpagoBIConstants.AF_LANGUAGE);
			String curr_country=(String)permanentSession.getAttribute(SpagoBIConstants.AF_COUNTRY);
					

			if(curr_language!=null && curr_country!=null && !curr_language.equals("") && !curr_country.equals("")){
				locale=new Locale(curr_language, curr_country, "");
			}
		}
		
		IEngUserProfile profile = (IEngUserProfile) req.getSession()
				.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		String userToAccept = (String)req.getParameter("userToAccept");
		String community = (String)req.getParameter("community");
		String owner = (String)req.getParameter("owner");
		String result=msgBuilder.getMessage("community.save.membership.ok.1", "messages", locale)+ "<b> "+userToAccept+ "</b> " +
					  msgBuilder.getMessage("community.save.membership.ok.2", "messages", locale)+ "<b> "+community + "</b>";
		
		if(profile.getUserUniqueIdentifier().equals(owner)){

			
			SbiCommunity sbiComm;
			try {
				ISbiCommunityDAO communityDao;
				communityDao = DAOFactory.getCommunityDAO();
	
				sbiComm = communityDao.loadSbiCommunityByName(community);
				communityDao.addCommunityMember(sbiComm, userToAccept);
				
				
				//add missing roles to the folder, in order that docs are executable by the accepted user
				CommunityManager cm = new CommunityManager();
				cm.addRolesToFunctionality(userToAccept, sbiComm.getFunctCode());
	
			} catch (EMFUserError e) {
				logger.error(e.getMessage());
				result= msgBuilder.getMessage("community.save.membership.ko", "messages", locale);
			}		
		}else{
			result= msgBuilder.getMessage("community.save.membership.cannot", "messages", locale);
		}
		
		result = createHTMLPage(result, req);
		logger.debug("OUT");
		return result;
	}
	
	@GET
	@Path("/user")
	@Produces(MediaType.APPLICATION_JSON)
	public String getCommunity(@Context HttpServletRequest req) {
		ISbiCommunityDAO commDao = null;
		List<SbiCommunity> communities;
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		
		String communitiesJSONStr = "";
		try {
			commDao = DAOFactory.getCommunityDAO();
			
			communities = commDao.loadSbiCommunityByUser((String)profile.getUserUniqueIdentifier());
			if(communities != null){
				String innerList = communityDeser(communities);
				communitiesJSONStr ="{root:"+innerList+"}";
			}

		} catch (Throwable t) {
			throw new SpagoBIServiceException(
					"An unexpected error occured while instatiating the dao", t);
		}

		return communitiesJSONStr;
		
	}
	private String communityDeser(List<SbiCommunity> communities) throws JSONException, EMFUserError{

		JSONArray jsonComm= new JSONArray();
		for(int i=0; i<communities.size(); i++){
			SbiCommunity com = communities.get(i);
			Integer id = com.getCommunityId();
			String name = com.getName();
			String descr = com.getDescription();
			String owner = com.getOwner();
			String functCode = com.getFunctCode();
			
			JSONObject obj = new JSONObject();
			obj.put("communityId", id);
			obj.put("name", name);
			obj.put("description", descr);
			obj.put("owner", owner);
			obj.put("functCode", functCode);
			LowFunctionality folder = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByCode(functCode, false);
			if(folder!= null){
				obj.put("functId", folder.getId());
			}
			
			jsonComm.put(obj);
		}
		
		return jsonComm.toString();		
	}
	
	private String createHTMLPage (String msg, HttpServletRequest req){
		StringBuffer sb = new StringBuffer();
		
		String currTheme = (String)req.getAttribute("currTheme");
    	if(currTheme==null)currTheme=ThemesManager.getDefaultTheme();			
		logger.debug("currTheme: "+currTheme);
		
		
		String contextName = ChannelUtilities.getSpagoBIContextName(req);
		String schema = req.getScheme();
		String server= req.getServerName();
		String port= req.getServerPort()+"";
		String url = schema	+ "://"+server + ":" + port	+ "/" + contextName;
		
		sb.append("<HTML>");
		sb.append("<HEAD>");
		sb.append("<TITLE>Community Membership Request</TITLE>");
		sb.append("</HEAD>");
		sb.append("<BODY>");
		sb.append("<link rel='stylesheet' type='text/css' href=\""+contextName+"/themes/"+currTheme+"/css/home40/standard.css\"/>");
		sb.append("	<span style='float:left; width: 100%; text-align:center;'>");
		sb.append("	<form method=\"get\" action=\""+url+"\" class=\"reserved-area-form login\">");
		sb.append("		<main class='main main-msg' id='main'>");
		sb.append(" 	<div class='aux'> ");
		sb.append("			<span class='ops'><h2>"+msgBuilder.getMessage("community.save.membership.title", "messages", locale)+"</h2></span>");
		sb.append("				<p>");
		sb.append(					msg);
		sb.append("				</p>");
		sb.append("				<div class=\"submit\">");
		sb.append("					<input type=\"submit\" value=\""+msgBuilder.getMessage("community.save.membership.backToSite", "messages", locale)+"\">");
		sb.append("				</div>");
		sb.append("		</div>");
		sb.append("		</main> ");
		sb.append("	</span>");
		sb.append("</BODY>");
		String htmlTxt = sb.toString();
		
		return htmlTxt;
		
	}
}
