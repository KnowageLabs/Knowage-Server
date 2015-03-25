/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

**/
package it.eng.spagobi.tools.notification;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.tools.dataset.bo.IDataSet;

import java.security.Security;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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

import org.apache.log4j.Logger;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class DatasetNotificationManager implements INotificationManager {

	
	static private Logger logger = Logger.getLogger(DatasetNotificationManager.class);
	private IMessageBuilder msgBuilder = null;

	public DatasetNotificationManager(){}
	
	public DatasetNotificationManager(IMessageBuilder msgBuilder){
		this.msgBuilder  = msgBuilder;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.tools.notification.INotificationManager#handleEvent(it.eng.spagobi.tools.notification.AbstractEvent)
	 */
	public void handleEvent(AbstractEvent event) {
		
		try {
			if (event instanceof DatasetNotificationEvent){
				String eventName = event.getEventName();
				DatasetNotificationEvent datasetEvent = (DatasetNotificationEvent)event;
				if (eventName.equals(EventConstants.DATASET_EVENT_LICENCE_CHANGED)){
					notifyLicenceChange(datasetEvent);
				} else if (eventName.equals(EventConstants.DATASET_EVENT_METADATA_CHANGED)){
					notifyDatasetMetadataChanged(datasetEvent);
				} else if (eventName.equals(EventConstants.DATASET_EVENT_FILE_CHANGED)){
					notifyDatasetFileChanged(datasetEvent);
				} else if (eventName.equals(EventConstants.DATASET_EVENT_DELETED_DATASET)){
					notifyDatasetDeleted(datasetEvent);
				} else if(eventName.equals(EventConstants.DATASET_EVENT_NAME_CHANGED)){
					notifyDatasetNameChanged(datasetEvent);
				} else if(eventName.equals(EventConstants.DATASET_EVENT_DESCRIPTION_CHANGED)){
					notifyDatasetDescriptionChanged(datasetEvent);
				} else if(eventName.equals(EventConstants.DATASET_EVENT_CATEGORY_CHANGED)){
					notifyDatasetCategoryChanged(datasetEvent);
				} else if(eventName.equals(EventConstants.DATASET_EVENT_SCOPE_CHANGED)){
					notifyDatasetScopeChanged(datasetEvent);
				}
				
			} else {
				logger.debug("Dataset Notification Manager cannot handle the event "+event.getEventName());
			}
		} catch (Exception e){
			logger.error("Error handling event "+event.getEventName(), e);
		}

	}
	
	public void handleMultipleEvents(List<AbstractEvent> events){
		try {
			notifyDatasetChanges(events);

		} catch (Exception ex){
			logger.error("Error handling multiple events in DatasetNotification Manager", ex);
		}
	}
	
	private void notifyDatasetChanges(List<AbstractEvent> events) throws Exception{
		String information = "";
		DatasetNotificationEvent datasetEvent = null;
		
		for (AbstractEvent event : events){
			if (event instanceof DatasetNotificationEvent){
				String datasetName = "";
				datasetEvent = (DatasetNotificationEvent)event;
				if (event.getArgument() instanceof IDataSet){
					IDataSet dataset = (IDataSet)event.getArgument();
					datasetName = dataset.getName();
				}
				
				String eventName = event.getEventName();
				information = information + msgBuilder.getMessage("SBIDev.DataSet.notify.msg.1", "messages") + " '" +datasetName+ "' " +
						msgBuilder.getMessage("SBIDev.DataSet.notify.msg.2", "messages")+ " "  ;
				if (eventName.equals(EventConstants.DATASET_EVENT_LICENCE_CHANGED)){
//					information = information +"The dataset "+datasetLabel+" that you are using in a Map, has changed his licence. \n";
					information = information + msgBuilder.getMessage("SBIDev.DataSet.notify.msg.3.lic", "messages");

				} else if (eventName.equals(EventConstants.DATASET_EVENT_METADATA_CHANGED)){
//					information = information +"The dataset "+datasetLabel+" that you are using in a Map, has changed the associated metadata. \n";
					information = information +	msgBuilder.getMessage("SBIDev.DataSet.notify.msg.3.meta", "messages");

				} else if (eventName.equals(EventConstants.DATASET_EVENT_FILE_CHANGED)){
//					information = information +"The dataset "+datasetLabel+" that you are using in a Map, has changed the associated file. \n";
					information = information + msgBuilder.getMessage("SBIDev.DataSet.notify.msg.3.file", "messages");

				} else if (eventName.equals(EventConstants.DATASET_EVENT_DELETED_DATASET)){
//					information = information +"The dataset "+datasetLabel+" that you are using in a Map, has been deleted. \n";
					information = information + msgBuilder.getMessage("SBIDev.DataSet.notify.msg.3.delete", "messages");

				} else if(eventName.equals(EventConstants.DATASET_EVENT_NAME_CHANGED)){
//					information = information +"The dataset "+datasetLabel+" that you are using in a Map, has changed his name. \n";
					information = information + msgBuilder.getMessage("SBIDev.DataSet.notify.msg.3.name", "messages");
					
				} else if(eventName.equals(EventConstants.DATASET_EVENT_DESCRIPTION_CHANGED)){
//					information = information +"The dataset "+datasetLabel+" that you are using in a Map, has changed his description. \n";
					information = information + msgBuilder.getMessage("SBIDev.DataSet.notify.msg.3.descr", "messages");

				} else if(eventName.equals(EventConstants.DATASET_EVENT_CATEGORY_CHANGED)){
//					information = information +"The dataset "+datasetLabel+" that you are using in a Map, has changed his category. \n";
					information = information + msgBuilder.getMessage("SBIDev.DataSet.notify.msg.3.cat", "messages");
				
				} else if(eventName.equals(EventConstants.DATASET_EVENT_SCOPE_CHANGED)){
//					information = information +"The dataset "+datasetLabel+" that you are using in a Map, has changed his scope. \n";
					information = information + msgBuilder.getMessage("SBIDev.DataSet.notify.msg.3.scope", "messages");
					//adds maps informations
				}
			}
		}
		
		if ((datasetEvent != null) &&(!information.isEmpty() )){
			notifyMapAuthorsMail(datasetEvent, msgBuilder.getMessage("SBIDev.DataSet.notify.msg.title"),information );
		}
	}
	
	private void notifyMapAuthorsMail(DatasetNotificationEvent datasetEvent, String subject, String emailContent) throws Exception{
		
		Set<String> emailsAddressOfAuthors = datasetEvent.retrieveEmailAddressesOfMapAuthors();
		if (datasetEvent.getArgument() instanceof IDataSet){
			if (!emailsAddressOfAuthors.isEmpty()){
		    	String[] recipients = emailsAddressOfAuthors.toArray(new String[0]);
		    		    	
		    	//send mail
		    	try {
			    	sendMail(recipients, subject, emailContent);
			    	logger.debug("Mail sent to Map Authors ");
		    	} catch (Exception e){
					logger.error("Error notifying map authors", e);
					throw new Exception(
							"Error notifying map authors", e);
		    	}
			}
		}
	}
	
	private void notifyDatasetNameChanged(DatasetNotificationEvent datasetEvent)throws Exception {
		if (datasetEvent.getArgument() instanceof IDataSet){
			IDataSet dataset = (IDataSet)datasetEvent.getArgument();
//			String subject = "The dataset "+dataset.getName()+" has changed the name";
//	    	String emailContent = "The dataset "+dataset.getName()+" that you are using in a Map, has changed the name";
			
			String subject = msgBuilder.getMessage("SBIDev.DataSet.notify.msg.1", "messages") + " '" +dataset.getName()+ "' " +
					msgBuilder.getMessage("SBIDev.DataSet.notify.msg.3.name", "messages") ;

	    	String emailContent = msgBuilder.getMessage("SBIDev.DataSet.notify.msg.1", "messages")+ " '"  +dataset.getName()+ "' " +
	    			msgBuilder.getMessage("SBIDev.DataSet.notify.msg.2", "messages") + " " +
					msgBuilder.getMessage("SBIDev.DataSet.notify.msg.3.name", "messages") ;
	    	try {
				notifyMapAuthorsMail(datasetEvent,subject,emailContent);
	    	} catch (Exception e){
				logger.error("Error notifying map authors about dataset name change with email", e);
	    	}
		}
	}
	
	private void notifyDatasetDescriptionChanged(DatasetNotificationEvent datasetEvent)throws Exception {
		if (datasetEvent.getArgument() instanceof IDataSet){
			IDataSet dataset = (IDataSet)datasetEvent.getArgument();
//			String subject = "The dataset "+dataset.getName()+" has changed the description";
//	    	String emailContent = "The dataset "+dataset.getName()+" that you are using in a Map, has changed the description";
			String subject = msgBuilder.getMessage("SBIDev.DataSet.notify.msg.1", "messages") + " '" +dataset.getName()+ "' " +
					msgBuilder.getMessage("SBIDev.DataSet.notify.msg.3.descr", "messages") ;

	    	String emailContent = msgBuilder.getMessage("SBIDev.DataSet.notify.msg.1", "messages") + " '" +dataset.getName()+ "' " +
	    			msgBuilder.getMessage("SBIDev.DataSet.notify.msg.2", "messages") + " " +
					msgBuilder.getMessage("SBIDev.DataSet.notify.msg.3.descr", "messages") ;
	    	try {
				notifyMapAuthorsMail(datasetEvent,subject,emailContent);
	    	} catch (Exception e){
				logger.error("Error notifying map authors about dataset description change with email", e);
	    	}
		}
	}
	
	private void notifyDatasetCategoryChanged(DatasetNotificationEvent datasetEvent)throws Exception {
		if (datasetEvent.getArgument() instanceof IDataSet){
			IDataSet dataset = (IDataSet)datasetEvent.getArgument();
//			String subject = "The dataset "+dataset.getName()+" has changed the category";
//	    	String emailContent = "The dataset "+dataset.getName()+" that you are using in a Map, has changed the category";
			String subject = msgBuilder.getMessage("SBIDev.DataSet.notify.msg.1", "messages") + " '" +dataset.getName()+ "' " +
					msgBuilder.getMessage("SBIDev.DataSet.notify.msg.3.cat", "messages") ;

	    	String emailContent = msgBuilder.getMessage("SBIDev.DataSet.notify.msg.1", "messages") + " '" +dataset.getName()+ "' " +
	    			msgBuilder.getMessage("SBIDev.DataSet.notify.msg.2", "messages") + " " +
					msgBuilder.getMessage("SBIDev.DataSet.notify.msg.3.cat", "messages") ;
	    	try {
				notifyMapAuthorsMail(datasetEvent,subject,emailContent);
	    	} catch (Exception e){
				logger.error("Error notifying map authors about dataset category change with email", e);
	    	}
		}
	}
	
	private void notifyDatasetScopeChanged(DatasetNotificationEvent datasetEvent)throws Exception {
		if (datasetEvent.getArgument() instanceof IDataSet){
			IDataSet dataset = (IDataSet)datasetEvent.getArgument();
//			String subject = "The dataset "+dataset.getName()+" has changed the scope";
//	    	String emailContent = "The dataset "+dataset.getName()+" that you are using in a Map, has changed the scope";
			String subject = msgBuilder.getMessage("SBIDev.DataSet.notify.msg.1", "messages") + " '" +dataset.getName()+ "' " +
					msgBuilder.getMessage("SBIDev.DataSet.notify.msg.3.scope", "messages") ;

	    	String emailContent = msgBuilder.getMessage("SBIDev.DataSet.notify.msg.1", "messages") + " '" +dataset.getName()+ "' " +
	    			msgBuilder.getMessage("SBIDev.DataSet.notify.msg.2", "messages")+ " " +
					msgBuilder.getMessage("SBIDev.DataSet.notify.msg.3.scope", "messages") ;
	    	try {
				notifyMapAuthorsMail(datasetEvent,subject,emailContent);
	    	} catch (Exception e){
				logger.error("Error notifying map authors about dataset scope change with email", e);
	    	}
		}
	}
	
	private void notifyDatasetMetadataChanged(DatasetNotificationEvent datasetEvent)throws Exception {
		if (datasetEvent.getArgument() instanceof IDataSet){
			IDataSet dataset = (IDataSet)datasetEvent.getArgument();
//			String subject = "The dataset "+dataset.getName()+" has changed the metadata";
//	    	String emailContent = "The dataset "+dataset.getName()+" that you are using in a Map, has changed the associated metadata";
			String subject = msgBuilder.getMessage("SBIDev.DataSet.notify.msg.1", "messages") + " '" +dataset.getName()+ "' " +
					msgBuilder.getMessage("SBIDev.DataSet.notify.msg.3.meta", "messages") ;

	    	String emailContent = msgBuilder.getMessage("SBIDev.DataSet.notify.msg.1", "messages") + " '" +dataset.getName()+ "' " +
	    			msgBuilder.getMessage("SBIDev.DataSet.notify.msg.2", "messages")+ " " +
					msgBuilder.getMessage("SBIDev.DataSet.notify.msg.3.meta", "messages") ;
	    	try {
				notifyMapAuthorsMail(datasetEvent,subject,emailContent);
	    	} catch (Exception e){
				logger.error("Error notifying map authors about dataset metadata change with email", e);
	    	}
		}
	}
	
	
	private void notifyDatasetFileChanged(DatasetNotificationEvent datasetEvent)throws Exception {
		if (datasetEvent.getArgument() instanceof IDataSet){
			IDataSet dataset = (IDataSet)datasetEvent.getArgument();
//			String subject = "The dataset "+dataset.getName()+" has changed the file";
//	    	String emailContent = "The dataset "+dataset.getName()+" that you are using in a Map, has changed the associated file";
			String subject = msgBuilder.getMessage("SBIDev.DataSet.notify.msg.1", "messages") + " '" +dataset.getName()+ "' " +
							 msgBuilder.getMessage("SBIDev.DataSet.notify.msg.3.file", "messages") ;

	    	String emailContent = msgBuilder.getMessage("SBIDev.DataSet.notify.msg.1", "messages") + " '" +dataset.getName()+ "' " +
	    			msgBuilder.getMessage("SBIDev.DataSet.notify.msg.2", "messages") + " " +
					msgBuilder.getMessage("SBIDev.DataSet.notify.msg.3.file", "messages") ;
	    	try {
				notifyMapAuthorsMail(datasetEvent,subject,emailContent);
	    	} catch (Exception e){
				logger.error("Error notifying map authors about file dataset change with email", e);
	    	}
		}
	}

	
	private void notifyDatasetDeleted(DatasetNotificationEvent datasetEvent)throws Exception {
		if (datasetEvent.getArgument() instanceof IDataSet){
			IDataSet dataset = (IDataSet)datasetEvent.getArgument();
//			String subject = "The dataset "+dataset.getName()+" has been deleted";
//	    	String emailContent = "The dataset "+dataset.getName()+" that you are using in a Map, has been deleted.";
			String subject = msgBuilder.getMessage("SBIDev.DataSet.notify.msg.1", "messages") + " '" +dataset.getName()+ " " +
					msgBuilder.getMessage("SBIDev.DataSet.notify.msg.3.delete", "messages") ;

	    	String emailContent = msgBuilder.getMessage("SBIDev.DataSet.notify.msg.1", "messages") + " '" +dataset.getName()+ "' " +
	    			msgBuilder.getMessage("SBIDev.DataSet.notify.msg.2", "messages") + " " +
					msgBuilder.getMessage("SBIDev.DataSet.notify.msg.3.delete", "messages") ;
	    	try {
				notifyMapAuthorsMail(datasetEvent,subject,emailContent);
	    	} catch (Exception e){
				logger.error("Error notifying map authors about dataset deletion with email", e);
	    	}
	    }
		
	}	
	
	private void notifyLicenceChange(DatasetNotificationEvent datasetEvent) throws Exception {
		if (datasetEvent.getArgument() instanceof IDataSet){
			IDataSet dataset = (IDataSet)datasetEvent.getArgument();
//	    	String subject = "The dataset "+dataset.getName()+" has changed is licence";
//	    	String emailContent = "The dataset "+dataset.getName()+" that you are using in a Map, has changed his licence";
			String subject = msgBuilder.getMessage("SBIDev.DataSet.notify.msg.1", "messages") + " '" +dataset.getName()+ " '" +
					msgBuilder.getMessage("SBIDev.DataSet.notify.msg.3.lic", "messages") ;

	    	String emailContent = msgBuilder.getMessage("SBIDev.DataSet.notify.msg.1", "messages") + " '" +dataset.getName()+ " '" +
	    			msgBuilder.getMessage("SBIDev.DataSet.notify.msg.2", "messages") +
					msgBuilder.getMessage("SBIDev.DataSet.notify.msg.3.lic", "messages") ;
	    	try {
				notifyMapAuthorsMail(datasetEvent,subject,emailContent);
	    	} catch (Exception e){
				logger.error("Error notifying map authors about licence change with email", e);
	    	}
		}

	}
	

	private void sendMail(String[] emailAddresses, String subject, String emailContent) throws Exception{

		final String DEFAULT_SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
		final String CUSTOM_SSL_FACTORY = "it.eng.spagobi.commons.services.DummySSLSocketFactory";

		String smtphost = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.user.smtphost");
		String smtpport = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.user.smtpport");
		String smtpssl = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.user.useSSL"); 
		logger.debug(smtphost+" "+smtpport+" use SSL: "+smtpssl);

		//Custom Trusted Store Certificate Options
		String trustedStorePath = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.trustedStore.file"); 
		String trustedStorePassword = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.trustedStore.password"); 

		int smptPort=25;

		if( (smtphost==null) || smtphost.trim().equals(""))
			throw new Exception("Smtp host not configured");
		if( (smtpport==null) || smtpport.trim().equals("")){
			throw new Exception("Smtp host not configured");
		}else{
			smptPort=Integer.parseInt(smtpport);
		}

		String from = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.user.from");
		if( (from==null) || from.trim().equals(""))
			from = "spagobi@eng.it";
		String user = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.user.user");
		if( (user==null) || user.trim().equals("")){
			logger.debug("Smtp user not configured");	
			user=null;
		}
		String pass = SingletonConfig.getInstance().getConfigValue("MAIL.PROFILES.user.password");
		if( (pass==null) || pass.trim().equals("")){
			logger.debug("Smtp password not configured");	
		}

		//Set the host smtp address
		Properties props = new Properties();
		props.put("mail.smtp.host", smtphost);
		props.put("mail.smtp.port", Integer.toString(smptPort));
		//Set timeout limit for mail server to respond
		props.put("mail.smtp.timeout", "5000");             
        props.put("mail.smtp.connectiontimeout", "5000"); 

		// open session
		Session session=null;
		// create autheticator object
		Authenticator auth = null;
		if (user!=null) {
			auth = new SMTPAuthenticator(user, pass);
			props.put("mail.smtp.auth", "true");
			//SSL Connection
			if (smtpssl.equals("true")){
				Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());	            
				props.put("mail.smtps.auth", "true");
				props.put("mail.smtps.socketFactory.port", Integer.toString(smptPort));
				if ((!StringUtilities.isEmpty(trustedStorePath)) ) {            	
					/* Dynamic configuration of trustedstore for CA
					 * Using Custom SSLSocketFactory to inject certificates directly from specified files
					 */

					props.put("mail.smtps.socketFactory.class", CUSTOM_SSL_FACTORY);

				} else {

					props.put("mail.smtps.socketFactory.class", DEFAULT_SSL_FACTORY);
				}
				props.put("mail.smtp.socketFactory.fallback", "false"); 
			}

			session = Session.getInstance(props, auth);
			logger.info("Session.getInstance(props, auth)");

		}else{
			session = Session.getInstance(props);
			logger.info("Session.getInstance(props)");
		}

		// create a message
		Message msg = new MimeMessage(session);
		// set the from and to address
		InternetAddress addressFrom = new InternetAddress(from);
		msg.setFrom(addressFrom);
		InternetAddress[] addressTo = new InternetAddress[emailAddresses.length];
		for (int i = 0; i < emailAddresses.length; i++)  {
			addressTo[i] = new InternetAddress(emailAddresses[i]);
		}
		msg.setRecipients(Message.RecipientType.BCC, addressTo);  

		// Setting the Subject and Content Type
		msg.setSubject(subject);
		// create and fill the first message part
		MimeBodyPart mbp1 = new MimeBodyPart();
		mbp1.setText(emailContent);
		// create the Multipart and add its parts to it
		Multipart mp = new MimeMultipart();
		mp.addBodyPart(mbp1);
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


	}
	// Private Classes ----------------------------------------------------------------------------
	
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

}
