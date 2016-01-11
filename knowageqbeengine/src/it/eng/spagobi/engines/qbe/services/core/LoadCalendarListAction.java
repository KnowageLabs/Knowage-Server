/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.services.core;

import it.eng.qbe.query.Query;
import it.eng.qbe.statement.IStatement;
import it.eng.qbe.statement.QbeDatasetFactory;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class LoadCalendarListAction extends AbstractQbeEngineAction {

	// private static final long serialVersionUID = 1080081357363500954L;
	public static final String SERVICE_NAME = "LOAD_CALENDAR_LIST_ACTION";

	@Override
	public String getActionName() {
		return SERVICE_NAME;
	}

	// INPUT PARAMETERS
	public static final String FIELD_NAME = "fieldId";
	public static final String PARENT_ENTITY_UNIQUE_NAME = "entityId";
	public static final String COLUMN = "column";

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(SetDefaultHierarchyAction.class);

	@Override
	public void service(SourceBean request, SourceBean response) {

		String column;

		logger.debug("IN");

		try {
			super.service(request, response);
			column = this.getAttributeAsString(COLUMN);
			Query q = new Query();
			q.setId(UUID.randomUUID().toString()); // this is required for serialization/deserialization and cloning
			q.setDistinctClauseEnabled(true);
			q.addSelectFiled(column, "NONE", "Valori", true, true, false, "asc", null);

			// q.setDescription(query);
			IStatement stat = this.getDataSource().createStatement(q);
			IDataSet dataSet = QbeDatasetFactory.createDataSet(stat);
			dataSet.loadData();
			IDataStore dataStore = dataSet.getDataStore();
			JSONDataWriter dataSetWriter = new JSONDataWriter();
			JSONObject gridDataFeed = (JSONObject) dataSetWriter.write(dataStore);

			try {
				writeBackToClient(new JSONSuccess(gridDataFeed));
			} catch (IOException e) {
				String message = "Impossible to write back the responce to the client";
				throw new SpagoBIEngineServiceException(getActionName(), message, e);
			}

		} catch (Throwable t) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), getEngineInstance(), t);
		} finally {
			logger.debug("OUT");
		}
	}

}