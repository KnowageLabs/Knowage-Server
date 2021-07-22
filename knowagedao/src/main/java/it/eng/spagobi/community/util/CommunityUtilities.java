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
package it.eng.spagobi.community.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Locale.Builder;
import java.util.Set;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import it.eng.knowage.mail.MailSessionBuilder;
import it.eng.knowage.mail.MailSessionBuilder.SessionFacade;
import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.RequestContainerAccess;
import it.eng.spago.base.SessionContainer;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.themes.ThemesManager;

public class CommunityUtilities {
	static private Logger logger = Logger.getLogger(CommunityUtilities.class);

	public boolean dispatchMail(String communityName, SbiUser userToAccept, SbiUser owner, String ownerEmail, HttpServletRequest request) {
		Locale locale = null;
		RequestContainer reqCont = RequestContainerAccess.getRequestContainer(request);
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
		String currTheme = ThemesManager.getDefaultTheme();
		logger.debug("currTheme: " + currTheme);

		IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
		// get message
		String msg1 = msgBuilder.getMessage("community.accept.mail.1", "messages", request);
		String msg2 = msgBuilder.getMessage("community.accept.mail.2", "messages", request);
		String msg3 = msgBuilder.getMessage("community.accept.mail.3", "messages", request);
		String msg3_1 = msgBuilder.getMessage("community.accept.mail.3.1", "messages", request);
		String msg3_2 = msgBuilder.getMessage("community.accept.mail.3.2", "messages", request);
		String msg3_3 = msgBuilder.getMessage("community.accept.mail.3.3", "messages", request);
		String msg3_4 = msgBuilder.getMessage("community.accept.mail.3.4", "messages", request);
		String msg3_5 = msgBuilder.getMessage("community.accept.mail.3.5", "messages", request);
		String msg4 = msgBuilder.getMessage("community.accept.mail.4", "messages", request);
		String msg5 = msgBuilder.getMessage("community.accept.mail.5", "messages", request);
		String msgwarn = msgBuilder.getMessage("community.accept.mail.warn", "messages", request);

		String contextName = ChannelUtilities.getSpagoBIContextName(request);

		String mailSubj = "Community " + communityName + " membership request";
		StringBuffer sb = new StringBuffer();
		sb.append("<HTML>");
		sb.append("<HEAD>");
		sb.append("<TITLE>Community Membership Request</TITLE>");
		sb.append("</HEAD>");
		sb.append("<BODY>");
		sb.append("<p style=\"width:100%; text-align:center;\">");

		sb.append(msg1 + "&nbsp; <b>" + owner.getFullName() + "</b>, <br/>  " + msg2 + " <b>" + userToAccept.getFullName() + "</b> " + msg3 + " <b>"
				+ communityName + "</b> community.");
		sb.append("<br/><br/>" + msg3_1 + "<br/><br/>");

		try {
			SbiUser userForAttr = DAOFactory.getSbiUserDAO().loadSbiUserById(userToAccept.getId());
			Set<SbiUserAttributes> userAttributes = userForAttr.getSbiUserAttributeses();
			List<String> lstAttrs = new ArrayList();
			lstAttrs.add("email");
			lstAttrs.add("gender");
			lstAttrs.add("birth_date");
			lstAttrs.add("location");

			Iterator itAttrs = userAttributes.iterator();
			while (itAttrs.hasNext()) {
				SbiUserAttributes userAttr = (SbiUserAttributes) itAttrs.next();
				String attrName = userAttr.getSbiAttribute().getAttributeName();
				if (lstAttrs.contains(attrName) && userAttr.getAttributeValue() != null && userAttr.getAttributeValue() != "") {
					sb.append("- <b>" + attrName + "</b>: " + userAttr.getAttributeValue() + " <br/>");
				}
			}
		} catch (Throwable t) {
			throw new SpagoBIServiceException("An unexpected error occured while executing the subscribe action", t);
		}
		sb.append("<br/>" + msg3_5 + "<br/>");
		sb.append("<br/>" + msg4 + " <b> " + userToAccept.getFullName() + "</b> " + msg5);
		String schema = request.getScheme();
		String server = request.getServerName();
		String port = request.getServerPort() + "";

		sb.append("<br/><a href=\"" + schema + "://" + server + ":" + port + contextName + "/publicjsp/CommunityRequest.jsp?owner=" + owner.getUserId()
				+ "&userToAccept=" + userToAccept.getUserId() + "&community=" + communityName + "&locale=" + locale + "&currTheme=" + currTheme + "\">");
		sb.append("<img alt=\"Accept/Reject\" src=\"" + schema + "://" + server + ":" + port + contextName + "/themes/sbi_default/img/go-community.png\"></a>");

		sb.append("</p>");
		// sb.append("<p style=\"width:100%; text-align:center;\"><b>"+msgwarn+"</b></p>");
		sb.append("</BODY>");
		String mailTxt = sb.toString();

		logger.debug("IN");
		try {

			SessionFacade facade = MailSessionBuilder.newInstance().usingUserProfile().build();

			// create a message
			Message msg = facade.createNewMimeMessage();
			InternetAddress[] addressTo = new InternetAddress[1];
			addressTo[0] = new InternetAddress(ownerEmail);

			msg.setRecipients(Message.RecipientType.TO, addressTo);

			// Setting the Subject
			msg.setSubject(mailSubj);
			msg.setContent(mailTxt, "text/html");

			// send message
			facade.sendMessage(msg);
		} catch (Throwable e) {
			logger.error("Error while sending community membership request mail", e);
			return false;
		} finally {
			logger.debug("OUT");
		}
		return true;
	}

}
