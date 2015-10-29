/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.services.core.datamart;

import it.eng.qbe.datasource.jpa.JPADataSource;
import it.eng.qbe.model.structure.filter.IQbeTreeEntityFilter;
import it.eng.qbe.model.structure.filter.IQbeTreeFieldFilter;
import it.eng.qbe.model.structure.filter.QbeTreeAccessModalityEntityFilter;
import it.eng.qbe.model.structure.filter.QbeTreeAccessModalityFieldFilter;
import it.eng.qbe.model.structure.filter.QbeTreeFilter;
import it.eng.qbe.model.structure.filter.QbeTreeOrderEntityFilter;
import it.eng.qbe.model.structure.filter.QbeTreeOrderFieldFilter;
import it.eng.qbe.model.structure.filter.QbeTreeQueryEntityFilter;
import it.eng.qbe.query.Query;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.qbe.services.core.AbstractQbeEngineAction;
import it.eng.spagobi.engines.qbe.tree.ExtJsQbeTreeBuilder;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.JSONArray;

/**
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class GetTreeAction extends AbstractQbeEngineAction {

	private static final long serialVersionUID = 1325035833244562916L;

	// INPUT PARAMETERS
	public static final String QUERY_ID = "parentQueryId";
	public static final String DATAMART_NAME = "datamartName";

	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(GetTreeAction.class);


	@Override
	public void service(SourceBean request, SourceBean response) {


		String queryId = null;
		String datamartName = null;
		Query query = null;

		IQbeTreeEntityFilter entityFilter = null;
		IQbeTreeFieldFilter fieldFilter = null;
		QbeTreeFilter treeFilter = null;

		ExtJsQbeTreeBuilder qbeBuilder = null;
		JSONArray nodes;

		logger.debug("IN");

		try {
			super.service(request, response);

			UserProfile userProfile = (UserProfile) getEnv().get(EngineConstants.ENV_USER_PROFILE);

			queryId = getAttributeAsString(QUERY_ID);
			logger.debug("Parameter [" + QUERY_ID + "] is equals to [" + queryId + "]");

			Assert.assertNotNull(getEngineInstance(), "It's not possible to execute " + this.getActionName() + " service before having properly created an instance of EngineInstance class");

			logger.debug("Filtering entities list ...");
			entityFilter = new QbeTreeAccessModalityEntityFilter();
			logger.debug("Apply entity filter [" + entityFilter.getClass().getName() + "]");
			if(queryId != null) {
				logger.debug("Filtering on query [" + queryId + "] selectd entities");
				query = getEngineInstance().getQueryCatalogue().getQuery(queryId);
				if(query != null){
					entityFilter = new QbeTreeQueryEntityFilter(entityFilter, query);
				}
			}
			entityFilter = new QbeTreeOrderEntityFilter(entityFilter);
			logger.debug("Apply field filter [" + entityFilter.getClass().getName() + "]");

			logger.debug("Filtering fields list ...");
			fieldFilter = new QbeTreeAccessModalityFieldFilter();
			logger.debug("Apply field filter [" + fieldFilter.getClass().getName() + "]");
			fieldFilter = new QbeTreeOrderFieldFilter(fieldFilter);
			logger.debug("Apply field filter [" + fieldFilter.getClass().getName() + "]");

			treeFilter = new  QbeTreeFilter(entityFilter, fieldFilter);

			qbeBuilder = new ExtJsQbeTreeBuilder(treeFilter);

			datamartName = getAttributeAsString(DATAMART_NAME);
			if (datamartName != null) {
				nodes = qbeBuilder.getQbeTree(getDataSource(), getLocale(), datamartName, userProfile);
			} else {
				nodes = new JSONArray();
				Iterator<String> it = ((JPADataSource)getDataSource()).getModelStructure(userProfile).getModelNames().iterator();
				while (it.hasNext()) {
					String modelName = it.next();
					JSONArray temp = qbeBuilder.getQbeTree(getDataSource(), getLocale(), modelName, userProfile);
					for (int i = 0; i < temp.length(); i++) {
						Object object = temp.get(i);
						nodes.put(object);
					}
				}
			}

			try {
				writeBackToClient( new JSONSuccess(nodes) );
			} catch (IOException e) {
				String message = "Impossible to write back the responce to the client";
				throw new SpagoBIEngineServiceException(getActionName(), message, e);
			}

		} catch(Throwable t) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), getEngineInstance(), t);
		} finally {
			logger.debug("OUT");
		}


	}
}
