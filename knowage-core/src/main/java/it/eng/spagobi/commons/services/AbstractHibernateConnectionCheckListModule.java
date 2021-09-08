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
package it.eng.spagobi.commons.services;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import it.eng.spago.base.SourceBean;
import it.eng.spago.dbaccess.sql.DataConnection;
import it.eng.spago.dbaccess.sql.SQLCommand;
import it.eng.spago.dbaccess.sql.result.DataResult;
import it.eng.spago.dbaccess.sql.result.ScrollableDataResult;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.paginator.basic.ListIFace;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.HibernateSessionManager;
import it.eng.spagobi.commons.utilities.SpagoBITracer;

public class AbstractHibernateConnectionCheckListModule extends
		AbstractBasicCheckListModule {

	/* (non-Javadoc)
	 * @see it.eng.spagobi.commons.services.AbstractBasicCheckListModule#createCheckedObjectMap(it.eng.spago.base.SourceBean)
	 */
	@Override
	public void createCheckedObjectMap(SourceBean request) throws Exception {
		checkedObjectsMap = new HashMap();

		// get CHECKED_QUERY query parameters

		String[] parameters = getQueryParameters("CHECKED_QUERY", request);

		// get CHECKED_QUERY statment
		String statement = getQueryStatement("CHECKED_QUERY", parameters);

		Session aSession = null;
		Transaction tx = null;

		// exec CHECKED_QUERY
		ScrollableDataResult scrollableDataResult = null;
		SQLCommand sqlCommand = null;
		DataConnection dataConnection = null;
		DataResult dataResult = null;
		try {
			aSession = HibernateSessionManager.getCurrentSession();
			tx = aSession.beginTransaction();
			//Connection jdbcConnection = aSession.connection();
			try (Connection jdbcConnection = HibernateSessionManager.getConnection(aSession)) {
				dataConnection = DelegatedHibernateConnectionListService.getDataConnection(jdbcConnection);
				sqlCommand = dataConnection.createSelectCommand(statement);
				dataResult = sqlCommand.execute();
				scrollableDataResult = (ScrollableDataResult) dataResult.getDataObject();
				SourceBean chekedObjectsBean = scrollableDataResult.getSourceBean();
				List checkedObjectsList = chekedObjectsBean
						.getAttributeAsList("ROW");
				for (int i = 0; i < checkedObjectsList.size(); i++) {
					SourceBean objects = (SourceBean) checkedObjectsList.get(i);
					String key = getObjectKey(objects);
					checkedObjectsMap.put(key, key);
				}

//				aSession.doWork(new MyWork(statement));

//				tx.commit();
			}
		} catch (HibernateException he) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE,
		            this.getClass().getName(),
		            "execCheckedQuery",
		            he.getMessage());
			if (tx != null)
				tx.rollback();
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} catch (Exception e) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass()
					.getName(), "createCheckedObjectMap", e.getMessage(), e);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) aSession.close();
			}
		}
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.commons.services.AbstractBasicCheckListModule#getList(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	@Override
	public ListIFace getList(SourceBean request, SourceBean response) throws Exception {
		return DelegatedHibernateConnectionListService.getList(this, request, response);
	}

}
