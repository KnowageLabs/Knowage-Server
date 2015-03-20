/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.services;

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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.jdbc.Work;

public class AbstractHibernateConnectionCheckListModule extends
		AbstractBasicCheckListModule {

	/* (non-Javadoc)
	 * @see it.eng.spagobi.commons.services.AbstractBasicCheckListModule#createCheckedObjectMap(it.eng.spago.base.SourceBean)
	 */
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
			Connection jdbcConnection = HibernateSessionManager.getConnection(aSession);
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
			
//			aSession.doWork(new MyWork(statement));
			
//			tx.commit();
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
	public ListIFace getList(SourceBean request, SourceBean response) throws Exception {
		return DelegatedHibernateConnectionListService.getList(this, request, response);
	}
	
//	public class MyWork implements Work {
//		
//		private String statement = null;
//		
//		public MyWork (String statement) {
//			this.statement = statement;
//		}
//		
//		public void execute(Connection connection) throws SQLException {
//			try {
//				DataConnection dataConnection = DelegatedHibernateConnectionListService.getDataConnection(connection);
//				SQLCommand sqlCommand = dataConnection.createSelectCommand(statement);
//				DataResult dataResult = sqlCommand.execute();
//				ScrollableDataResult scrollableDataResult = (ScrollableDataResult) dataResult.getDataObject();
//				SourceBean chekedObjectsBean = scrollableDataResult.getSourceBean();
//				List checkedObjectsList = chekedObjectsBean
//						.getAttributeAsList("ROW");
//				for (int i = 0; i < checkedObjectsList.size(); i++) {
//					SourceBean objects = (SourceBean) checkedObjectsList.get(i);
//					String key = getObjectKey(objects);
//					checkedObjectsMap.put(key, key);
//				}
//			} catch (Exception e) {
//				throw new RuntimeException("Error while getting checked objects map", e);
//			}
//		}
//		
//	}
	
}
