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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;

import it.eng.knowage.mailsender.IMailSender;
import it.eng.knowage.mailsender.dto.MessageMailDto;
import it.eng.knowage.mailsender.dto.ProfileNameMailEnum;
import it.eng.knowage.mailsender.dto.TypeMailEnum;
import it.eng.knowage.mailsender.factory.FactoryMailSender;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
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

			MessageMailDto messageMailDto = new MessageMailDto();
			messageMailDto.setProfileName((ProfileNameMailEnum.SCHEDULER));

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
			messageMailDto.setRecipients(recipients);
			// Setting the Subject and Content Type
			String subject = document.getName() + nameSuffix;

			messageMailDto.setSubject(subject);
			messageMailDto.setTypeMailEnum(TypeMailEnum.MULTIPART);
			messageMailDto.setAttach(executionOutput);
			messageMailDto.setFileExtension(fileExtension);
			messageMailDto.setContentType(contentType);
			messageMailDto.setNameSuffix(nameSuffix);
			messageMailDto.setFileExtension(fileExtension);
			messageMailDto.setContainedFileName(document.getName());

			// send message
			FactoryMailSender.getMailSender(SingletonConfig.getInstance().getConfigValue(IMailSender.MAIL_SENDER)).sendMail(messageMailDto);

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

}
