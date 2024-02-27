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
package it.eng.spagobi.analiticalmodel.execution.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import it.eng.knowage.mail.MailSessionBuilder;
import it.eng.knowage.mail.MailSessionBuilder.SessionFacade;
import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionController;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ExecutionProxy;
import it.eng.spagobi.commons.utilities.UserUtilities;

public class ExecuteAndSendAction extends AbstractHttpAction {

	private static final Logger LOGGER = Logger.getLogger(ExecuteAndSendAction.class);

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.commons.services.BaseProfileAction#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	@Override
	public void service(SourceBean request, SourceBean responseSb) throws Exception {
		LOGGER.debug("IN");

		freezeHttpResponse();
		HttpServletResponse response = getHttpResponse();
		HttpServletRequest req = getHttpRequest();

		final String OK = "10";
		final String ERROR = "20";
		final String TONOTFOUND = "90";
		String retCode = "";

		try {

			// GET PARAMETER
			String objid = "";
			String objLabel = "";
			String to = "";
			String cc = "";
			String object = "";
			String message = "";
			String queryStr = "";
			String userId = "";
			String login = "";
			String pass = "";
			String from = "";

			// Creo una lista con un suo iteratore con dentro i parametri della
			// request
			List params = request.getContainedAttributes();
			ListIterator it = params.listIterator();

			while (it.hasNext()) {

				Object par = it.next();
				SourceBeanAttribute p = (SourceBeanAttribute) par;
				String parName = p.getKey();
				LOGGER.debug("got parName=" + parName);
				if (parName.equals("objlabel")) {
					objLabel = (String) request.getAttribute("objlabel");
					LOGGER.debug("got objLabel from Request=" + objLabel);
				} else if (parName.equals("objid")) {
					objid = (String) request.getAttribute("objid");
					LOGGER.debug("got objid from Request=" + objid);
				} else if (parName.equals("to")) {
					to = (String) request.getAttribute("to");
					LOGGER.debug("got to from Request=" + to);
				} else if (parName.equals("cc")) {
					cc = (String) request.getAttribute("cc");
					LOGGER.debug("got cc from Request=" + cc);
				} else if (parName.equals("object")) {
					object = (String) request.getAttribute("object");
					LOGGER.debug("got object from Request=" + object);
				} else if (parName.equals("message")) {
					message = (String) request.getAttribute("message");
					LOGGER.debug("got message from Request=" + message);
				} else if (parName.equals("userid")) {
					userId = (String) request.getAttribute("userid");
					LOGGER.info("got userId from Request=" + userId);
				} else if (parName.equals("login")) {
					login = (String) request.getAttribute("login");
					LOGGER.info("got user from Request" + login);
				} else if (parName.equals("pwd")) {
					pass = (String) request.getAttribute("pwd");
					LOGGER.info("got pwd from Request");
				} else if (parName.equals("replyto")) {
					from = (String) request.getAttribute("replyto");
					LOGGER.info("got email to reply to, from Request" + from);
				} else if (parName.equals("NEW_SESSION")) {
					continue;
				} else {
					String value = (String) request.getAttribute(parName);
					queryStr += parName + "=" + value + "&";
				}
			}

			if (to.equals("")) {
				retCode = TONOTFOUND;
				LOGGER.error("To Address not found");
				throw new Exception("To Address not found");
			}

			String returnedContentType = "";
			String fileextension = "";
			byte[] documentBytes = null;

			IBIObjectDAO biobjdao = DAOFactory.getBIObjectDAO();
			BIObject biobj = null;
			if (objLabel != null && !objLabel.trim().equals("")) {
				biobj = biobjdao.loadBIObjectByLabel(objLabel);
			} else {
				biobj = biobjdao.loadBIObjectById(new Integer(objid));
			}
			// create the execution controller
			ExecutionController execCtrl = new ExecutionController();
			execCtrl.setBiObject(biobj);

			// fill parameters
			execCtrl.refreshParameters(biobj, queryStr);

			// set the description of the biobject parameters
			setParametersDescription(biobj.getDrivers(), params);

			// exec the document only if all its parameters are filled
			// Why???? if a parameter is not mandatory and the user did not fill it????
			// if (execCtrl.directExecution()) {
			ExecutionProxy proxy = new ExecutionProxy();
			proxy.setBiObject(biobj);

			IEngUserProfile profile = null;
			RequestContainer reqCont = RequestContainer.getRequestContainer();
			SessionContainer sessCont = reqCont.getSessionContainer();
			SessionContainer permSess = sessCont.getPermanentContainer();
			profile = (IEngUserProfile) permSess.getAttribute(IEngUserProfile.ENG_USER_PROFILE);

			if (profile == null) {

				profile = UserUtilities.getUserProfile(req);

			}

			documentBytes = proxy.exec(profile, ExecutionProxy.SEND_MAIL_MODALITY, null);
			returnedContentType = proxy.getReturnedContentType();
			fileextension = proxy.getFileExtensionFromContType(returnedContentType);
			// } end if (execCtrl.directExecution()) {
			// SEND MAIL

			SessionFacade facade = MailSessionBuilder.newInstance().usingUserProfile().build();

			// create a message
			Message msg = facade.createNewMimeMessage();

			String[] recipients = to.split(",");
			InternetAddress[] addressTo = new InternetAddress[recipients.length];
			for (int i = 0; i < recipients.length; i++) {
				addressTo[i] = new InternetAddress(recipients[i]);
			}
			msg.setRecipients(Message.RecipientType.TO, addressTo);
			if ((cc != null) && !cc.trim().equals("")) {
				recipients = cc.split(",");
				InternetAddress[] addressCC = new InternetAddress[recipients.length];
				for (int i = 0; i < recipients.length; i++) {
					String ccAdd = recipients[i];
					if ((ccAdd != null) && !ccAdd.trim().equals("")) {
						addressCC[i] = new InternetAddress(recipients[i]);
					}
				}
				msg.setRecipients(Message.RecipientType.CC, addressCC);
			}
			// Setting the Subject and Content Type
			msg.setSubject(object);

			// create and fill the first message part
			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setText(message);
			// create the second message part
			MimeBodyPart mbp2 = new MimeBodyPart();
			// attach the file to the message
			SchedulerDataSource sds = new SchedulerDataSource(documentBytes, returnedContentType,
					"result" + fileextension);
			mbp2.setDataHandler(new DataHandler(sds));
			mbp2.setFileName(sds.getName());
			// create the Multipart and add its parts to it
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp1);
			mp.addBodyPart(mbp2);
			// add the Multipart to the message
			msg.setContent(mp);
			// send message
			EMFErrorHandler errorHandler = getErrorHandler();
			if (errorHandler.isOKBySeverity(EMFErrorSeverity.ERROR)) {
				facade.sendMessage(msg);
				retCode = OK;
			} else {
				LOGGER.error("Error while executing and sending object " + errorHandler.getStackTrace());
				retCode = ERROR;
			}

		} catch (Exception e) {
			LOGGER.error("Error while executing and sending object ", e);
			if (retCode.equals("")) {
				retCode = ERROR;
			}
		} finally {
			try {
				response.getOutputStream().write(retCode.getBytes());
				response.getOutputStream().flush();
			} catch (Exception ex) {
				LOGGER.error("Error while sending response to client", ex);
			}
		}
		LOGGER.debug("OUT");
	}

	private class SchedulerDataSource implements DataSource {

		byte[] content = null;
		String name = null;
		String contentType = null;

		@Override
		public String getContentType() {
			return contentType;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			ByteArrayInputStream bais = new ByteArrayInputStream(content);
			return bais;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return null;
		}

		public SchedulerDataSource(byte[] content, String contentType, String name) {
			this.content = content;
			this.contentType = contentType;
			this.name = name;
		}

	}

	/**
	 * Add the description to the BIObjectparameters
	 *
	 * @param biObjectParameters
	 * @param attributes
	 */
	public void setParametersDescription(List<BIObjectParameter> biObjectParameters,
			List<SourceBeanAttribute> attributes) {
		Map<String, String> parameterNameDescriptionMap = new HashMap<>();
		// we create a map: parameter name, parameter description
		for (int i = 0; i < attributes.size(); i++) {
			SourceBeanAttribute sba = attributes.get(i);
			// the name of parameter in the request with the description is parametername+ field_visible_description
			int descriptionPosition = sba.getKey().indexOf("field_visible_description");
			if (descriptionPosition > 0) {
				parameterNameDescriptionMap.put(sba.getKey().substring(0, descriptionPosition - 1),
						(String) sba.getValue());
			}
		}
		for (int i = 0; i < biObjectParameters.size(); i++) {
			String bobjName = biObjectParameters.get(i).getParameterUrlName();
			String value = parameterNameDescriptionMap.get(bobjName);
			if (value != null) {
				biObjectParameters.get(i).setParameterValuesDescription(parseDescriptionString(value));
			}
		}
	}

	/**
	 * Parse a string with the description of the parameter and return a list with description.. This transformation is necessary because the multivalues parameters
	 *
	 * @param s the string with the description
	 * @return the list of descriptions
	 */
	public List<String> parseDescriptionString(String s) {
		List<String> descriptions = new ArrayList<>();
		StringTokenizer stk = new StringTokenizer(s, ";");
		while (stk.hasMoreTokens()) {
			descriptions.add(stk.nextToken());
		}
		return descriptions;
	}

}
