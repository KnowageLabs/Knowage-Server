/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
