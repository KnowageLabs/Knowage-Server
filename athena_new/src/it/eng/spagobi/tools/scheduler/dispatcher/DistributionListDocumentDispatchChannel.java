/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.scheduler.dispatcher;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.config.dao.IEngineDAO;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.distributionlist.bo.DistributionList;
import it.eng.spagobi.tools.distributionlist.bo.Email;
import it.eng.spagobi.tools.massiveExport.services.StartMassiveScheduleAction;
import it.eng.spagobi.tools.scheduler.to.DispatchContext;

import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class DistributionListDocumentDispatchChannel implements IDocumentDispatchChannel {

	private DispatchContext dispatchContext;
	
	// logger component
	private static Logger logger = Logger.getLogger(DistributionListDocumentDispatchChannel.class); 
    final String DEFAULT_SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
    final String CUSTOM_SSL_FACTORY = "it.eng.spagobi.commons.services.DummySSLSocketFactory";
	
	public DistributionListDocumentDispatchChannel(DispatchContext dispatchContext) {
		this.dispatchContext = dispatchContext;
	}
	
	public void setDispatchContext(DispatchContext dispatchContext) {
		this.dispatchContext = dispatchContext;
	}

	public void close() {
		
	}
	
	public boolean canDispatch(BIObject document)  {
		return true;
	}
	
	public boolean dispatch(BIObject document, byte[] executionOutput) {
	
		String contentType;
		String fileExtension;
		String nameSuffix;
		JobExecutionContext jobExecutionContext;
	
		logger.debug("IN");
		
		try{
			
			contentType = dispatchContext.getContentType();
			fileExtension = dispatchContext.getFileExtension();
			nameSuffix = dispatchContext.getNameSuffix();
			jobExecutionContext = dispatchContext.getJobExecutionContext();
			
		    //Custom Trusted Store Certificate Options
		    String trustedStorePath = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.trustedStore.file"); 
		    String trustedStorePassword = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.trustedStore.password"); 		 

			String smtphost = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.scheduler.smtphost");
		    String smtpport = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.scheduler.smtpport");
		    String smtpssl = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.scheduler.useSSL"); 
		    logger.debug(smtphost+" "+smtpport+" use SSL: "+smtpssl);


			if( (smtphost==null) || smtphost.trim().equals(""))
				throw new Exception("Smtp host not configured");
			String from = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.scheduler.from");
			if( (from==null) || from.trim().equals(""))
				from = "spagobi.scheduler@eng.it";
		    
			int smptPort=25;

		    if( (smtpport==null) || smtpport.trim().equals("")){
				throw new Exception("Smtp host not configured");
			}else{
				smptPort=Integer.parseInt(smtpport);
			}
			
			String user = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.scheduler.user");
			String pass = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.scheduler.password");
		
			/*
			if( (user==null) || user.trim().equals(""))
				throw new Exception("Smtp user not configured");
			
			if( (pass==null) || pass.trim().equals(""))
				throw new Exception("Smtp password not configured");
			*/
			
			String mailTos = "";
			List dlIds = dispatchContext.getDlIds();
			Iterator it = dlIds.iterator();
			while(it.hasNext()){

				Integer dlId = (Integer)it.next();
				DistributionList dl = DAOFactory.getDistributionListDAO().loadDistributionListById(dlId);

				List emails = new ArrayList();
				emails = dl.getEmails();
				Iterator j = emails.iterator();
				while(j.hasNext()){
					Email e = (Email) j.next();
					String email = e.getEmail();
					String userTemp = e.getUserId();
					IEngUserProfile userProfile = GeneralUtilities.createNewUserProfile(userTemp);				
					if(ObjectsAccessVerifier.canSee(document, userProfile))	{				
						if (j.hasNext()) {mailTos = mailTos+email+",";}
						else {mailTos = mailTos+email;}
					}

				}
			}


			if( (mailTos==null) || mailTos.trim().equals("")) {	
				throw new Exception("No recipient address found");
			}

			String[] recipients = mailTos.split(",");
			//Set the host smtp address
			Properties props = new Properties();
			props.put("mail.smtp.host", smtphost);
			props.put("mail.smtp.port", Integer.toString(smptPort));

			Session session = null;
			
			if(StringUtilities.isEmpty(user) || StringUtilities.isEmpty(pass)) {
				props.put("mail.smtp.auth", "false");
				session = Session.getInstance(props);
				logger.debug("Connecting to mail server without authentication");
			} else {
				props.put("mail.smtp.auth", "true");
				Authenticator auth = new SMTPAuthenticator(user, pass);
		 	    //SSL Connection
		    	if (smtpssl.equals("true")){
		            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());	            
				    //props.put("mail.smtp.debug", "true");          
				    props.put("mail.smtps.auth", "true");
			        props.put("mail.smtps.socketFactory.port", Integer.toString(smptPort));
		            if ((!StringUtilities.isEmpty(trustedStorePath)) ) {            	
						/* Dynamic configuration of trustedstore for CA
						 * Using Custom SSLSocketFactory to inject certificates directly from specified files
						 */
		            	//System.setProperty("java.security.debug","certpath");
		            	//System.setProperty("javax.net.debug","ssl ");
				        props.put("mail.smtps.socketFactory.class", CUSTOM_SSL_FACTORY);

		            } else {
		            	//System.setProperty("java.security.debug","certpath");
		            	//System.setProperty("javax.net.debug","ssl ");
				        props.put("mail.smtps.socketFactory.class", DEFAULT_SSL_FACTORY);
		            }
			        props.put("mail.smtp.socketFactory.fallback", "false"); 
		    	}
				session = Session.getInstance(props, auth);
				logger.debug("Connecting to mail server with authentication");
			}
			
			// create a message
			Message msg = new MimeMessage(session);
			// set the from and to address
			InternetAddress addressFrom = new InternetAddress(from);
			msg.setFrom(addressFrom);
			InternetAddress[] addressTo = new InternetAddress[recipients.length];
			for (int i = 0; i < recipients.length; i++)  {
				addressTo[i] = new InternetAddress(recipients[i]);
			}
			msg.setRecipients(Message.RecipientType.TO, addressTo);
			// Setting the Subject and Content Type
			IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
			String subject = document.getName() + nameSuffix;
			msg.setSubject(subject);
			// create and fill the first message part
			//MimeBodyPart mbp1 = new MimeBodyPart();
			//mbp1.setText(mailTxt);
			// create the second message part
			MimeBodyPart mbp2 = new MimeBodyPart();
			// attach the file to the message
			SchedulerDataSource sds = new SchedulerDataSource(executionOutput, contentType, document.getName() + nameSuffix + fileExtension);
			mbp2.setDataHandler(new DataHandler(sds));
			mbp2.setFileName(sds.getName());
			// create the Multipart and add its parts to it
			Multipart mp = new MimeMultipart();
			//mp.addBodyPart(mbp1);
			mp.addBodyPart(mbp2);
			// add the Multipart to the message
			msg.setContent(mp);
			// send message
	    	if ((smtpssl.equals("true")) && (!StringUtilities.isEmpty(user)) &&  (!StringUtilities.isEmpty(pass))){
	    		//USE SSL Transport comunication with SMTPS
		    	Transport transport = session.getTransport("smtps");
		    	transport.connect(smtphost,smptPort,user,pass);
		    	transport.sendMessage(msg, msg.getAllRecipients());
		    	transport.close(); 
	    	}
	    	else {
	    		//Use normal SMTP
		    	Transport.send(msg);
	    	}
			
			if(jobExecutionContext.getNextFireTime()== null){
				String triggername = jobExecutionContext.getTrigger().getName();
				dlIds = dispatchContext.getDlIds();
				it = dlIds.iterator();
				while(it.hasNext()){
					Integer dlId = (Integer)it.next();
					DistributionList dl = DAOFactory.getDistributionListDAO().loadDistributionListById(dlId);
					DAOFactory.getDistributionListDAO().eraseDistributionListObjects(dl, (document.getId()).intValue(), triggername);
				}
			}
		} catch (Exception e) {
			logger.error("Error while sending schedule result mail",e);
			return false;
		}finally{
			logger.debug("OUT");
		}
		
		return true;
	}
	
	
	
	private class SMTPAuthenticator extends javax.mail.Authenticator
	{
		private String username = "";
		private String password = "";

		public PasswordAuthentication getPasswordAuthentication()
		{
			return new PasswordAuthentication(username, password);
		}

		public SMTPAuthenticator(String user, String pass) {
			this.username = user;
			this.password = pass;
		}
	}


	private class SchedulerDataSource implements DataSource {

		byte[] content = null;
		String name = null;
		String contentType = null;

		public String getContentType() {
			return contentType;
		}

		public InputStream getInputStream() throws IOException {
			ByteArrayInputStream bais = new ByteArrayInputStream(content);
			return bais;
		}

		public String getName() {
			return name;
		}

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
