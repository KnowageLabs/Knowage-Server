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

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.module.list.basic.AbstractBasicListModule;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.paginator.basic.ListIFace;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.services.DelegatedHibernateConnectionListService;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.utilities.HibernateSessionManager;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.services.common.SsoServiceInterface;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class SelectDatasetLookupModule extends AbstractBasicListModule {

	static private Logger logger = Logger.getLogger(SelectDatasetLookupModule.class);

		/* (non-Javadoc)
		 * @see it.eng.spago.dispatching.service.list.basic.IFaceBasicListService#getList(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
		 */
		public ListIFace getList(SourceBean request, SourceBean response) throws Exception {
			//Start writing log in the DB
			Session aSession =null;
			try {
				aSession = HibernateSessionManager.getCurrentSession();
				//Connection jdbcConnection = aSession.connection();
				Connection jdbcConnection = HibernateSessionManager.getConnection(aSession);
				IEngUserProfile profile = UserUtilities.getUserProfile();
				AuditLogUtilities.updateAudit(((HttpServletRequest)getRequestContainer().getRequestContainer().getInternalRequest()),  profile, "DISTRIBUTION_LIST.OPEN", null, "OK");
			} catch (HibernateException he) {
				throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
			} finally {
				if (aSession!=null){
					if (aSession.isOpen()) aSession.close();
				}
			}
			//End writing log in the DB

			RequestContainer aRequestContainer = RequestContainer.getRequestContainer();
			SessionContainer aSessionContainer = aRequestContainer.getSessionContainer();

			IEngUserProfile userProfile =UserUtilities.getUserProfile();
			String userId="";
			if (userProfile!=null) userId=(String)((UserProfile)userProfile).getUserId();
			//sets the userid as input parameter for the query fo statements.xml
			aSessionContainer.setAttribute(SsoServiceInterface.USER_ID ,userId);

			return DelegatedHibernateConnectionListService.getList(this, request, response);
		}
	
	}

