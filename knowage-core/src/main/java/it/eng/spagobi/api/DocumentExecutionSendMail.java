package it.eng.spagobi.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.mail.MailSessionBuilder;
import it.eng.knowage.mail.MailSessionBuilder.SessionFacade;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionController;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ExecutionProxy;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.rest.RestUtilities;

@Path("/1.0/documentexecutionmail")
@ManageAuthorization
public class DocumentExecutionSendMail extends AbstractSpagoBIResource {

	@POST
	@Path("/sendMail")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response sendMailDocument(@Context HttpServletRequest req) throws IOException, JSONException {

		JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);
		String objLabel = requestVal.getString("label");
		String objid = requestVal.getString("docId");
		String userId = requestVal.getString("userId");
		String message = requestVal.getString("MESSAGE");
		String to = requestVal.getString("TO");
		String[] recipients = to.split(",");
		String cc = requestVal.optString("CC");
		String login = requestVal.optString("LOGIN");
		String pass = requestVal.optString("PASSWORD");
		String from = requestVal.optString("REPLAYTO");
		String object = requestVal.optString("OBJECT");
		JSONObject jsonParameters = requestVal.optJSONObject("parameters");

		HashMap<String, Object> resultAsMap = new HashMap<String, Object>();

		logger.debug("IN");

		final String OK = "10";
		String ERROR = "Error. Mail not sent";
		final String TONOTFOUND = "90";
		// String retCode = "";

		try {

			if (to.equals("")) {
				// retCode = TONOTFOUND;
				ERROR = "To Address not found";
				logger.error("To Address not found");
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

			List<BIObjectParameter> listBioParams = new ArrayList<BIObjectParameter>();
			// fill parameters
			String queryStr = "user_id=" + userId + "&ACTION_NAME=SEND_TO_ACTION&SBI_ENVIRONMENT=DOCBROWSER";
			for (BIObjectParameter biParam : biobj.getDrivers()) {
				if (jsonParameters != null && !jsonParameters.isNull(biParam.getParameterUrlName())) {
					// if LOV change ["Mexico","USA"] in "Mexico,USA"
					Object jsonParam = jsonParameters.get(biParam.getParameterUrlName());
					String param = "";
					if (jsonParam instanceof JSONArray) {
						for (int i = 0; i < ((JSONArray) jsonParam).length(); i++) {
							param = param + ((JSONArray) jsonParam).getString(i);
							if (i < ((JSONArray) jsonParam).length() - 1) {
								param = param + ";";
							}
						}
					} else {
						param = jsonParameters.getString(biParam.getParameterUrlName());
					}

					biParam.setParameterValuesDescription(parseDescriptionString(param));
					queryStr = queryStr + "&" + biParam.getParameterUrlName() + "=" + param;
					queryStr = queryStr + "&" + biParam.getParameterUrlName() + "_field_visible_description="
							+ jsonParameters.getString(biParam.getParameterUrlName() + "_field_visible_description");
				}
				listBioParams.add(biParam);
			}

			execCtrl.refreshParameters(biobj, queryStr); // ??
			biobj.setDrivers(listBioParams);

			// exec the document only if all its parameters are filled
			// Why???? if a parameter is not mandatory and the user did not fill it????
			// if (execCtrl.directExecution()) {
			ExecutionProxy proxy = new ExecutionProxy();
			proxy.setBiObject(biobj);

			IEngUserProfile profile = this.getUserProfile();

			documentBytes = proxy.exec(profile, ExecutionProxy.SEND_MAIL_MODALITY, null);
			returnedContentType = proxy.getReturnedContentType();
			fileextension = proxy.getFileExtensionFromContType(returnedContentType);
			// } end if (execCtrl.directExecution()) {
			// SEND MAIL

			SessionFacade facade = MailSessionBuilder.newInstance()
				.usingUserProfile()
				.setFromAddress(from)
				.setUser(login)
				.setPassword(pass)
				.build();

			// create a message
			Message msg = facade.createNewMimeMessage();

			InternetAddress[] addressTo = new InternetAddress[recipients.length];
			for (int i = 0; i < recipients.length; i++) {
				addressTo[i] = new InternetAddress(recipients[i]);
			}
			msg.setRecipients(Message.RecipientType.TO, addressTo);
			if ((cc != null) && !cc.trim().equals("")) {
				recipients = cc.split(",");
				InternetAddress[] addressCC = new InternetAddress[recipients.length];
				for (int i = 0; i < recipients.length; i++) {
					String cc_add = recipients[i];
					if ((cc_add != null) && !cc_add.trim().equals("")) {
						addressCC[i] = new InternetAddress(recipients[i]);
					}
				}
				msg.setRecipients(Message.RecipientType.CC, addressCC);
			}
			// Setting the Subject and Content Type
			msg.setSubject(object);

			// create and fill the first message part
			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setText(message, "utf-8", "html");
			// create the second message part
			MimeBodyPart mbp2 = new MimeBodyPart();
			// attach the file to the message
			SchedulerDataSource sds = new SchedulerDataSource(documentBytes, returnedContentType, "result" + fileextension);
			mbp2.setDataHandler(new DataHandler(sds));
			mbp2.setFileName(sds.getName());
			// create the Multipart and add its parts to it
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp1);
			mp.addBodyPart(mbp2);
			// add the Multipart to the message
			msg.setContent(mp);

			// send message
			facade.sendMessage(msg);

			// retCode = OK;
			resultAsMap.put("success", "Mail Sent");

		} catch (Exception e) {
			logger.error("Error while executing and sending object ", e);
			resultAsMap.put("errors", ERROR);
		} finally {
			// try {
			// response.getOutputStream().write(retCode.getBytes());
			// response.getOutputStream().flush();
			// } catch (Exception ex) {
			// logger.error("Error while sending response to client", ex);
			// }
		}
		logger.debug("OUT");

		return Response.ok(resultAsMap).build();
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
	 * @param BIObjectParameters
	 * @param attributes
	 */
	public void setParametersDescription(List<BIObjectParameter> BIObjectParameters, List<SourceBeanAttribute> attributes) {
		Map<String, String> parameterNameDescriptionMap = new HashMap<String, String>();
		// we create a map: parameter name, parameter description
		for (int i = 0; i < attributes.size(); i++) {
			SourceBeanAttribute sba = attributes.get(i);
			// the name of parameter in the request with the description is parametername+ field_visible_description
			int descriptionPosition = sba.getKey().indexOf("field_visible_description");
			if (descriptionPosition > 0) {
				parameterNameDescriptionMap.put(sba.getKey().substring(0, descriptionPosition - 1), (String) sba.getValue());
			}
		}
		for (int i = 0; i < BIObjectParameters.size(); i++) {
			String bobjName = BIObjectParameters.get(i).getParameterUrlName();
			String value = parameterNameDescriptionMap.get(bobjName);
			if (value != null) {
				BIObjectParameters.get(i).setParameterValuesDescription(parseDescriptionString(value));
			}
		}
	}

	/**
	 * Parse a string with the description of the parameter and return a list with description.. This transformation is necessary because the multivalues
	 * parameters
	 *
	 * @param s
	 *            the string with the description
	 * @return the list of descriptions
	 */
	public List<String> parseDescriptionString(String s) {
		List<String> descriptions = new ArrayList<String>();
		StringTokenizer stk = new StringTokenizer(s, ";");
		while (stk.hasMoreTokens()) {
			descriptions.add(stk.nextToken());
		}
		return descriptions;
	}

}
