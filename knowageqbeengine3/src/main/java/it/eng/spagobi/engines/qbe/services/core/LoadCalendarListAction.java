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