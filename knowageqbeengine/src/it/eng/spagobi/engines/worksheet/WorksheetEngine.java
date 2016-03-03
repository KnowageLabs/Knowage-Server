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
package it.eng.spagobi.engines.worksheet;

import it.eng.spagobi.engines.qbe.QbeEngine;
import it.eng.spagobi.engines.qbe.crosstable.serializer.CrosstabDeserializerFactory;
import it.eng.spagobi.engines.qbe.crosstable.serializer.CrosstabSerializerFactory;
import it.eng.spagobi.engines.worksheet.serializer.AttributeDeserializerFactory;
import it.eng.spagobi.engines.worksheet.serializer.AttributeSerializerFactory;
import it.eng.spagobi.engines.worksheet.serializer.MeasureDeserializerFactory;
import it.eng.spagobi.engines.worksheet.serializer.MeasureSerializerFactory;
import it.eng.spagobi.engines.worksheet.serializer.WorkSheetDeserializerFactory;
import it.eng.spagobi.engines.worksheet.serializer.WorkSheetSerializerFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;

import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class WorksheetEngine {
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(QbeEngine.class);

	public static WorksheetEngineInstance createInstance(Object object, Map env) throws WorksheetEngineException {
		WorksheetEngineInstance worksheetEngineInstance = null;
		initDeserializers();
		initSerializers();
		logger.debug("IN");
		worksheetEngineInstance = new WorksheetEngineInstance(object, env);
		logger.debug("OUT");
		return worksheetEngineInstance;
	}
	
	private static void initDeserializers() {
    	WorkSheetDeserializerFactory.getInstance();
    	CrosstabDeserializerFactory.getInstance();
    	AttributeDeserializerFactory.getInstance();
    	MeasureDeserializerFactory.getInstance();
	}


	private static void initSerializers() {
    	WorkSheetSerializerFactory.getInstance();
    	CrosstabSerializerFactory.getInstance();
    	AttributeSerializerFactory.getInstance();
    	MeasureSerializerFactory.getInstance();
	}

}
