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

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.module.list.basic.AbstractBasicListModule;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.paginator.basic.ListIFace;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.services.DelegatedHibernateConnectionListService;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.utilities.HibernateSessionManager;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.services.common.SsoServiceInterface;

import java.sql.Connection;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class SelectDatasetLookupModule extends AbstractBasicListModule {

	static private Logger logger = Logger.getLogger(SelectDatasetLookupModule.class);

		/* (non-Javadoc)
		 * @see it.eng.spago.dispatching.service.list.basic.IFaceBasicListService#getList(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
		 */
		@Override
		public ListIFace getList(SourceBean request, SourceBean response) throws Exception {
			//Start writing log in the DB
			Session aSession =null;
			IEngUserProfile profile = null;
			try {
				aSession = HibernateSessionManager.getCurrentSession();
				//Connection jdbcConnection = aSession.connection();
				Connection jdbcConnection = HibernateSessionManager.getConnection(aSession);
				profile = UserUtilities.getUserProfile();
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

			String userId="";
			if (profile!=null) userId=(String)((UserProfile)profile).getUserId();
			//sets the userid as input parameter for the query fo statements.xml

			boolean isAdmin =  UserUtilities.isAdministrator(profile);
			aSessionContainer.setAttribute(SsoServiceInterface.USER_ID ,userId);
			if(isAdmin){
				aSessionContainer.setAttribute("is_admin" ,1);
			}else{
				aSessionContainer.setAttribute("is_admin" ,0);
			}

			Set<Domain> dd = UserUtilities.getDataSetCategoriesByUser(profile);
			Iterator<Domain> domIter = null;
			if(dd!=null){
				domIter = dd.iterator();
			}
			for(int i=0; i<20; i++){
				int cat = 0;
				if(domIter!=null && domIter.hasNext()){
					cat = domIter.next().getValueId();
				}
				aSessionContainer.setAttribute("cat"+i ,cat);
			}

			return DelegatedHibernateConnectionListService.getList(this, request, response);
		}

	}

