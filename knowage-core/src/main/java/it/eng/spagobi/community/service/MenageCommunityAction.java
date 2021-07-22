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
package it.eng.spagobi.community.service;

import java.util.List;
import java.util.Locale;
import java.util.Locale.Builder;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.RequestContainerAccess;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.community.bo.CommunityManager;
import it.eng.spagobi.community.dao.ISbiCommunityDAO;
import it.eng.spagobi.community.mapping.SbiCommunity;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.themes.ThemesManager;

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
		if (reqCont != null) {
			SessionContainer aSessionContainer = reqCont.getSessionContainer();

			SessionContainer permanentSession = aSessionContainer.getPermanentContainer();

			String currLanguage = (String) permanentSession.getAttribute(SpagoBIConstants.AF_LANGUAGE);
			String currCountry = (String) permanentSession.getAttribute(SpagoBIConstants.AF_COUNTRY);
			String currScript = (String) permanentSession.getAttribute(SpagoBIConstants.AF_SCRIPT);
			if (currLanguage != null && currCountry != null) {
				Builder tmpLocale = new Locale.Builder().setLanguage(currLanguage).setRegion(currCountry);

				if (StringUtils.isNotBlank(currScript)) {
					tmpLocale.setScript(currScript);
				}

				locale = tmpLocale.build();
			} else
				locale = GeneralUtilities.getDefaultLocale();

		}

		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		String userToAccept = req.getParameter("userToAccept");
		String community = req.getParameter("community");
		String owner = req.getParameter("owner");
		String result = msgBuilder.getMessage("community.save.membership.ok.1", "messages", locale) + "<b> " + userToAccept + "</b> "
				+ msgBuilder.getMessage("community.save.membership.ok.2", "messages", locale) + "<b> " + community + "</b>";

		if (((UserProfile) profile).getUserId().equals(owner)) {

			SbiCommunity sbiComm;
			try {
				ISbiCommunityDAO communityDao;
				communityDao = DAOFactory.getCommunityDAO();

				sbiComm = communityDao.loadSbiCommunityByName(community);
				communityDao.addCommunityMember(sbiComm, userToAccept);

				// add missing roles to the folder, in order that docs are executable by the accepted user
				CommunityManager cm = new CommunityManager();
				cm.addRolesToFunctionality(userToAccept, sbiComm.getFunctCode());

			} catch (EMFUserError e) {
				logger.error(e.getMessage());
				result = msgBuilder.getMessage("community.save.membership.ko", "messages", locale);
			}
		} else {
			result = msgBuilder.getMessage("community.save.membership.cannot", "messages", locale);
		}

		result = createHTMLPage(result, req);
		logger.debug("OUT");
		return result;
	}

	@GET
	@Path("/user")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.USER_SAVE_DOCUMENT_FUNCTIONALITY })
	public String getCommunity(@Context HttpServletRequest req) {
		ISbiCommunityDAO commDao = null;
		List<SbiCommunity> communities;
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);

		String communitiesJSONStr = "";
		try {
			commDao = DAOFactory.getCommunityDAO();

			communities = commDao.loadSbiCommunityByUser((String) ((UserProfile) profile).getUserId());
			if (communities != null) {
				String innerList = communityDeser(communities);
				communitiesJSONStr = "{root:" + innerList + "}";
			}

		} catch (Throwable t) {
			throw new SpagoBIServiceException("An unexpected error occured while instatiating the dao", t);
		}

		return communitiesJSONStr;

	}

	private String communityDeser(List<SbiCommunity> communities) throws JSONException, EMFUserError {

		JSONArray jsonComm = new JSONArray();
		for (int i = 0; i < communities.size(); i++) {
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
			if (folder != null) {
				obj.put("functId", folder.getId());
			}

			jsonComm.put(obj);
		}

		return jsonComm.toString();
	}

	private String createHTMLPage(String msg, HttpServletRequest req) {
		StringBuffer sb = new StringBuffer();

		String currTheme = (String) req.getAttribute("currTheme");
		if (currTheme == null)
			currTheme = ThemesManager.getDefaultTheme();
		logger.debug("currTheme: " + currTheme);

		String contextName = ChannelUtilities.getSpagoBIContextName(req);
		String schema = req.getScheme();
		String server = req.getServerName();
		String port = req.getServerPort() + "";
		String url = schema + "://" + server + ":" + port + "/" + contextName;

		sb.append("<HTML>");
		sb.append("<HEAD>");
		sb.append("<TITLE>Community Membership Request</TITLE>");
		sb.append("</HEAD>");
		sb.append("<BODY>");
		sb.append("<link rel='stylesheet' type='text/css' href=\"" + contextName + "/themes/" + currTheme + "/css/home40/standard.css\"/>");
		sb.append("	<span style='float:left; width: 100%; text-align:center;'>");
		sb.append("	<form method=\"get\" action=\"" + url + "\" class=\"reserved-area-form login\">");
		sb.append("		<main class='main main-msg' id='main'>");
		sb.append(" 	<div class='aux'> ");
		sb.append("			<span class='ops'><h2>" + msgBuilder.getMessage("community.save.membership.title", "messages", locale) + "</h2></span>");
		sb.append("				<p>");
		sb.append(msg);
		sb.append("				</p>");
		sb.append("				<div class=\"submit\">");
		sb.append("					<input type=\"submit\" value=\"" + msgBuilder.getMessage("community.save.membership.backToSite", "messages", locale)
				+ "\">");
		sb.append("				</div>");
		sb.append("		</div>");
		sb.append("		</main> ");
		sb.append("	</span>");
		sb.append("</BODY>");
		String htmlTxt = sb.toString();

		return htmlTxt;

	}
}
