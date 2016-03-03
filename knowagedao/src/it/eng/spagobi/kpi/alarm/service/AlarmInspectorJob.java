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
package it.eng.spagobi.kpi.alarm.service;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.kpi.alarm.bo.AlertSendingItem;
import it.eng.spagobi.kpi.alarm.dao.SbiAlarmContactDAOHibImpl;
import it.eng.spagobi.kpi.alarm.dao.SbiAlarmEventDAOHibImpl;
import it.eng.spagobi.kpi.alarm.metadata.SbiAlarm;
import it.eng.spagobi.kpi.alarm.metadata.SbiAlarmContact;
import it.eng.spagobi.kpi.alarm.metadata.SbiAlarmEvent;
import it.eng.spagobi.tools.scheduler.jobs.AbstractSpagoBIJob;
import it.eng.spagobi.tools.scheduler.to.DispatchContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class AlarmInspectorJob extends AbstractSpagoBIJob implements Job {

	static private Logger logger = Logger.getLogger(AlarmInspectorJob.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */

	SbiAlarmEventDAOHibImpl sae = new SbiAlarmEventDAOHibImpl();
	SbiAlarmContactDAOHibImpl sac = new SbiAlarmContactDAOHibImpl();

	private Map<SbiAlarmContact, List<AlertSendingItem>> alertSendingSessionMap = new HashMap<SbiAlarmContact, List<AlertSendingItem>>();
	private List<AlertSendingItem> alertSendingSessionList = null;
	private AlertSendingItem alertSendingItem = null;
	final String DEFAULT_SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
	final String CUSTOM_SSL_FACTORY = "it.eng.spagobi.commons.services.DummySSLSocketFactory";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		logger.debug("IN");
		try {
			this.setTenant(jobExecutionContext);
			this.executeInternal(jobExecutionContext);
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
	}

	private void executeInternal(JobExecutionContext jex) throws JobExecutionException {
		logger.debug("IN");
		org.hibernate.Session hsession = null;
		List<SbiAlarmEvent> activeSbiAlarmEventList = null;
		SbiAlarm sbiAlarm = null;

		try {
			hsession = sae.getSession();

			activeSbiAlarmEventList = sae.findActive(hsession);
			for (SbiAlarmEvent sbiAlarmEvent : activeSbiAlarmEventList) {

				if (logger.isInfoEnabled())
					logger.info("Found AlarmEvent: " + sbiAlarmEvent.getKpiName());

				sbiAlarm = sbiAlarmEvent.getSbiAlarms();

				String resource = sbiAlarmEvent.getResources();

				// creo un item e gli imposto l'evento e l'allarme
				alertSendingItem = new AlertSendingItem(sbiAlarm, sbiAlarmEvent);

				if (logger.isDebugEnabled())
					logger.debug("Created AlertSendingItem: " + alertSendingItem);

				List<SbiAlarmContact> sbiAlarmContactList = new ArrayList<SbiAlarmContact>();
				List<SbiAlarmContact> associatedContactList = new ArrayList<SbiAlarmContact>(sbiAlarm.getSbiAlarmContacts());

				if (resource != null) {
					if (logger.isDebugEnabled())
						logger.debug("Resource enhanced: " + resource);

					for (SbiAlarmContact associatedContact : associatedContactList) {
						if (resource.equals(associatedContact.getResources()) || associatedContact.getResources() == null) {
							sbiAlarmContactList.add(associatedContact);

							if (logger.isDebugEnabled())
								logger.debug("Contact '" + associatedContact + "' added.");
						}
					}
				} else {
					if (logger.isDebugEnabled())
						logger.debug("Resource not enhanced.");

					for (SbiAlarmContact associatedContact : associatedContactList) {
						String rr = associatedContact.getResources();
						if (associatedContact.getResources() == null || associatedContact.getResources().equals("")) {
							sbiAlarmContactList.add(associatedContact);

							if (logger.isDebugEnabled())
								logger.debug("Contact '" + associatedContact + "' added.");
						}
					}
				}
				if (logger.isDebugEnabled())
					logger.debug("Distribution list: " + sbiAlarmContactList + "\n");

				for (SbiAlarmContact sbiAlarmContact : sbiAlarmContactList) {
					alertSendingSessionList = alertSendingSessionMap.get(sbiAlarmContact);
					if (alertSendingSessionList == null) {

						if (logger.isDebugEnabled())
							logger.debug("alertSendingSessionList null");

						alertSendingSessionList = new ArrayList<AlertSendingItem>();
					}

					alertSendingSessionList.add(alertSendingItem);

					if (logger.isDebugEnabled())
						logger.debug("Contact '" + sbiAlarmContact.getName() + "' added to alertSendingSessionList.");

					alertSendingSessionMap.put(sbiAlarmContact, alertSendingSessionList);
				}

				// Se l'event è autodisabilitante
				if (sbiAlarm.isAutoDisabled()) {
					// if(sbiAlarm.isSingleEvent()){
					if (logger.isDebugEnabled())
						logger.debug("Single alarm '" + sbiAlarm.getLabel() + "' disabled.");
					sbiAlarmEvent.setActive(false);
					sae.update(sbiAlarmEvent);
				}
			}

			startEmailSession(alertSendingSessionMap);

		} catch (Throwable e) {
			logger.error("Error while executiong job ", e);
			e.printStackTrace();
		} finally {
			if (hsession != null)
				hsession.close();
			logger.debug("OUT");
		}
	}

	private void startEmailSession(Map<SbiAlarmContact, List<AlertSendingItem>> alertSendingSessionMap) {
		logger.debug("IN");

		IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();

		Set<SbiAlarmContact> keySet = alertSendingSessionMap.keySet();
		DispatchContext sInfo = new DispatchContext();

		if (logger.isDebugEnabled())
			logger.debug("Distribution list parsing.");

		for (SbiAlarmContact sbiAlarmContact : keySet) {
			if (logger.isDebugEnabled())
				logger.debug("Found contact '" + sbiAlarmContact.getName() + "'.");

			List<AlertSendingItem> alertSendingList = alertSendingSessionMap.get(sbiAlarmContact);

			SbiAlarm sbiAlarm = null;
			SbiAlarmEvent sbiAlarmEvent = null;

			StringBuffer subject = new StringBuffer();
			StringBuffer text = new StringBuffer();
			for (AlertSendingItem alertSendingItem : alertSendingList) {
				sbiAlarm = alertSendingItem.getSbiAlarm();
				sbiAlarmEvent = alertSendingItem.getSbiAlarmEvent();

				if (logger.isDebugEnabled())
					logger.debug("Found alarm " + sbiAlarm.getName() + ".");

				subject.append(sbiAlarm.getLabel());

				text.append("<font size=\"4\">" + msgBuilder.getMessage("sbi.kpi.alarm.mail.body.alarm") + " </font><font color=\"red\" size=\"4\"><b>");
				text.append(sbiAlarm.getName());
				text.append("</b></font><ul>");

				text.append("<li><font size=\"2\">" + msgBuilder.getMessage("sbi.kpi.alarm.mail.body.label") + ": ");
				text.append(sbiAlarm.getLabel());
				text.append("</font></li>");
				text.append("<li><font size=\"2\">" + msgBuilder.getMessage("sbi.kpi.alarm.mail.body.text") + ": ");
				text.append(sbiAlarm.getText());
				text.append("</font></li>");
				text.append("<li><font size=\"2\">" + msgBuilder.getMessage("sbi.kpi.alarm.mail.body.description") + ": ");
				text.append(sbiAlarm.getDescr());
				text.append("</font></li>");
				text.append("</ul><br>");
				text.append("<font size=\"3\">" + msgBuilder.getMessage("sbi.kpi.alarm.mail.body.kpi.details") + ":</font><ul>");
				text.append("<li><font size=\"2\">" + msgBuilder.getMessage("sbi.kpi.alarm.mail.body.kpi.name") + ": ");
				text.append(sbiAlarmEvent.getKpiName());
				text.append("</font></li>");
				text.append("<li><font size=\"2\">" + msgBuilder.getMessage("sbi.kpi.alarm.mail.body.kpi.date") + ": ");
				text.append(sbiAlarmEvent.getEventTs());
				text.append("</font></li>");
				text.append("<li><font size=\"2\">" + msgBuilder.getMessage("sbi.kpi.alarm.mail.body.kpi.value") + ": ");
				text.append(sbiAlarmEvent.getKpiValue());
				text.append("</font></li>");
				text.append("<li><font size=\"2\">" + msgBuilder.getMessage("sbi.kpi.alarm.mail.body.kpi.threshold") + ": ");
				text.append(sbiAlarmEvent.getThresholdValue());
				text.append("</font></li>");

				String res = sbiAlarmEvent.getResources();
				if (res != null) {
					text.append("<li><font size=\"2\">" + msgBuilder.getMessage("sbi.kpi.alarm.mail.body.kpi.resources") + ":");
					text.append(res);
					text.append("</font></li>");
				}

				text.append("</ul><hr width=\"90%\">");
			}

			String link = sbiAlarm.getUrl();

			text.append(link);

			String email = sbiAlarmContact.getEmail();
			if (email != null) {
				sInfo.setMailTos(email);
				sInfo.setMailSubj(msgBuilder.getMessage("sbi.kpi.alarm.mail.subject") + ": " + new Date() + " [" + sbiAlarmContact.getName() + "]");
				sInfo.setMailTxt(text.toString());
			}

			if (logger.isDebugEnabled())
				logger.debug("Sending email to: " + sInfo.getMailTos());

			sendMail(sInfo, null, null, null);
		}

		logger.debug("OUT");

	}

	private void sendMail(DispatchContext sInfo, byte[] response, String retCT, String fileExt) {
		logger.debug("IN");
		try {

			String smtphost = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.kpi_alarm.smtphost");

			String smtpport = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.kpi_alarm.smtpport");

			String smtpssl = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.kpi_alarm.useSSL");
			logger.debug(smtphost + " " + smtpport + " use SSL: " + smtpssl);
			// Custom Trusted Store Certificate Options
			String trustedStorePath = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.trustedStore.file");
			String trustedStorePassword = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.trustedStore.password");
			int smptPort = 25;

			if ((smtphost == null) || smtphost.trim().equals(""))
				throw new Exception("Smtp host not configured");
			if ((smtpport == null) || smtpport.trim().equals("")) {
				throw new Exception("Smtp host not configured");
			} else {
				smptPort = Integer.parseInt(smtpport);
			}

			String from = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.kpi_alarm.from");

			if ((from == null) || from.trim().equals(""))
				from = "spagobi.scheduler@eng.it";

			String user = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.kpi_alarm.user");
			if ((user == null) || user.trim().equals("")) {
				logger.debug("Smtp user not configured");
				user = "";
			}
			// throw new Exception("Smtp user not configured");
			String pass = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.kpi_alarm.password");
			if ((pass == null) || pass.trim().equals("")) {
				pass = "";
				logger.debug("Smtp password not configured");
			}
			String mailTos = sInfo.getMailTos();

			if ((mailTos == null) || mailTos.trim().equals("")) {
				throw new Exception("No recipient address found");

			}

			String mailSubj = sInfo.getMailSubj();
			String mailTxt = sInfo.getMailTxt();

			String[] recipients = mailTos.split(",");

			// Set the host smtp address
			Properties props = new Properties();
			props.put("mail.smtp.host", smtphost);
			props.put("mail.smtp.port", Integer.toString(smptPort));
			props.put("mail.smtp.auth", "true");

			// open session
			Session session = null;
			// create autheticator object
			Authenticator auth = null;
			if (user.equals("")) {
				auth = new SMTPAuthenticator(user, pass);
				props.put("mail.smtp.auth", "false");
				// session = Session.getDefaultInstance(props, auth);
				session = Session.getInstance(props);
				logger.info("Session.getDefaultInstance(props)");
			} else {
				auth = new SMTPAuthenticator(user, pass);
				props.put("mail.smtp.auth", "true");
				if (smtpssl.equals("true")) {
					Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
					// props.put("mail.smtp.debug", "true");
					props.put("mail.smtps.auth", "true");
					props.put("mail.smtps.socketFactory.port", Integer.toString(smptPort));
					if ((!StringUtilities.isEmpty(trustedStorePath))) {
						/*
						 * Dynamic configuration of trustedstore for CA Using Custom SSLSocketFactory to inject certificates directly from specified files
						 */
						// System.setProperty("java.security.debug","certpath");
						// System.setProperty("javax.net.debug","ssl ");
						props.put("mail.smtps.socketFactory.class", CUSTOM_SSL_FACTORY);

					} else {
						// System.setProperty("java.security.debug","certpath");
						// System.setProperty("javax.net.debug","ssl ");
						props.put("mail.smtps.socketFactory.class", DEFAULT_SSL_FACTORY);
					}
					props.put("mail.smtp.socketFactory.fallback", "false");
				}

				// session = Session.getDefaultInstance(props, auth);
				session = Session.getInstance(props, auth);
				// session.setDebug(true);
				// session.setDebugOut(null);

				logger.info("Session.getInstance(props, auth)");
			}
			// create a message
			MimeMessage msg = new MimeMessage(session);

			// set the from and to address
			InternetAddress addressFrom = new InternetAddress(from);
			msg.setFrom(addressFrom);
			InternetAddress[] addressTo = new InternetAddress[recipients.length];
			for (int i = 0; i < recipients.length; i++) {
				addressTo[i] = new InternetAddress(recipients[i]);
			}
			msg.setRecipients(Message.RecipientType.TO, addressTo);

			// Setting the Subject and Content Type
			// IMessageBuilder msgBuilder =
			// MessageBuilderFactory.getMessageBuilder();
			String subject = mailSubj;
			msg.setSubject(subject);

			// create and fill the first message part
			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setText(mailTxt);

			// create the second message part
			// MimeBodyPart mbp2 = new MimeBodyPart();

			// attach the file to the message
			// SchedulerDataSource sds = new SchedulerDataSource(response,
			// retCT, sbiAlarmEvent.getKpiName() + fileExt);
			// mbp2.setDataHandler(new DataHandler(sds));
			// mbp2.setFileName(sds.getName());

			// create the Multipart and add its parts to it
			MimeMultipart mp = new MimeMultipart();
			mp.addBodyPart(mbp1);
			// mp.addBodyPart(mbp2);

			// add the Multipart to the message
			msg.setContent(mailTxt, "text/html");

			// send message
			if ((smtpssl.equals("true")) && (!StringUtilities.isEmpty(user)) && (!StringUtilities.isEmpty(pass))) {
				// USE SSL Transport comunication with SMTPS
				Transport transport = session.getTransport("smtps");
				transport.connect(smtphost, smptPort, user, pass);
				transport.sendMessage(msg, msg.getAllRecipients());
				transport.close();
			} else {
				// Use normal SMTP
				Transport.send(msg);
			}
		} catch (Exception e) {
			logger.error("Error while sending schedule result mail", e);
		} catch (Throwable t) {
			logger.error("Error while sending schedule result mail", t);
		} finally {
			logger.debug("OUT");
		}
	}

	private class SMTPAuthenticator extends javax.mail.Authenticator {
		private String username = "";
		private String password = "";

		@Override
		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(username, password);
		}

		public SMTPAuthenticator(String user, String pass) {
			this.username = user;
			this.password = pass;
		}
	}

	@SuppressWarnings("unused")
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
}
