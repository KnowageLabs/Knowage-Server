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
package it.eng.spagobi.tools.scheduler.dispatcher;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;

import it.eng.knowage.mail.MailSessionBuilder;
import it.eng.knowage.mail.MailSessionBuilder.SessionFacade;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.tools.distributionlist.bo.DistributionList;
import it.eng.spagobi.tools.distributionlist.bo.Email;
import it.eng.spagobi.tools.scheduler.to.DispatchContext;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class DistributionListDocumentDispatchChannel implements IDocumentDispatchChannel {

	private DispatchContext dispatchContext;

	// logger component
	private static Logger logger = Logger.getLogger(DistributionListDocumentDispatchChannel.class);

	public DistributionListDocumentDispatchChannel(DispatchContext dispatchContext) {
		this.dispatchContext = dispatchContext;
	}

	@Override
	public void setDispatchContext(DispatchContext dispatchContext) {
		this.dispatchContext = dispatchContext;
	}

	@Override
	public void close() {

	}

	@Override
	public boolean canDispatch(BIObject document)  {
		return true;
	}

	@Override
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

			SessionFacade facade = MailSessionBuilder.newInstance()
				.usingSchedulerProfile()
				.build();

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

			// create a message
			Message msg = facade.createNewMimeMessage();

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
			facade.sendMessage(msg);

			if(jobExecutionContext.getNextFireTime()== null){
				String triggername = jobExecutionContext.getTrigger().getKey().getName();
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
