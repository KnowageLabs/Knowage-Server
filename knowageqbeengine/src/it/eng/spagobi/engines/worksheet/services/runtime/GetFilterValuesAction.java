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
package it.eng.spagobi.engines.worksheet.services.runtime;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.worksheet.services.AbstractWorksheetEngineAction;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class GetFilterValuesAction extends AbstractWorksheetEngineAction {	
	
	private static final long serialVersionUID = 118095916184707515L;
	
	// INPUT PARAMETERS
	public static final String FIELD_NAME = "fieldName";
	public static final String SHEET = "sheetName";

	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(GetFilterValuesAction.class);
    public static transient Logger auditlogger = Logger.getLogger("audit.query");
    
	
	public void service(SourceBean request, SourceBean response)  {				
				
		JSONObject gridDataFeed = null;
		Monitor errorHitsMonitor = null;
		Monitor totalTimeMonitor = null;
		
		it.eng.spagobi.tools.dataset.common.datastore.IDataStore clone;
		
		totalTimeMonitor = MonitorFactory.start("WorksheetEngine.getFilterValuesAction.totalTime");

		logger.debug("IN");
		
		try {
		
			super.service(request, response);	

			String sheetName = this.getAttributeAsString( SHEET );
			logger.debug("Parameter [" + SHEET + "] is equals to [" + sheetName + "]");
			String fieldName = getAttributeAsString( FIELD_NAME );
			logger.debug("Parameter [" + FIELD_NAME + "] is equals to [" + fieldName + "]");

			JSONDataWriter dataSetWriter = new JSONDataWriter();
			
			clone = getUserSheetFilterValues(sheetName, fieldName);
			
			this.adjustDates(clone);
			
			gridDataFeed = (JSONObject) dataSetWriter.write(clone);
			
			try {
				writeBackToClient( new JSONSuccess(gridDataFeed) );
			} catch (IOException e) {
				String message = "Impossible to write back the responce to the client";
				throw new SpagoBIEngineServiceException(getActionName(), message, e);
			}
			
		} catch(Throwable t) {
			errorHitsMonitor = MonitorFactory.start("WorksheetEngine.errorHits");
			errorHitsMonitor.stop();
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), getEngineInstance(), t);
		} finally {
			if (totalTimeMonitor != null) totalTimeMonitor.stop();
			logger.debug("OUT");
		}	
	}


	/* 
	 * Converts dates into long values and description into a localized date
	 * This is because the client widget Ext.ux.form.SuperBoxSelect is not able to handle dates properly
	 */
	private void adjustDates(IDataStore dataStore) {
		IMetaData metadata = dataStore.getMetaData();
		IFieldMetaData fieldMetadata = metadata.getFieldMeta(0);
		Class type = fieldMetadata.getType();
		if (Date.class.isAssignableFrom(type)) {
			fieldMetadata.setType(Long.class);
			long count = dataStore.getRecordsCount();
			for (long i = 0; i < count; i++) {
				IField field = dataStore.getRecordAt((int) i).getFieldAt(0);
				Date value = (Date) field.getValue();
				field.setValue(value.getTime());
			}
		}
	}
	
}
