/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe;

import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spagobi.engines.qbe.serializer.SlotDeserializerFactory;
import it.eng.spagobi.engines.qbe.serializer.SlotSerializerFactory;
import it.eng.spagobi.utilities.engines.EngineConstants;

import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class QbeEngine {
	
	private static QbeEngineConfig engineConfig;
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(QbeEngine.class);
	
    
    private static void initEngine() {
    	if(engineConfig == null) {
    		engineConfig = QbeEngineConfig.getInstance();
    	}
	}

	public static QbeEngineInstance createInstance(Object template, Map env) throws QbeEngineException {
		QbeEngineInstance qbeEngineInstance = null;
		
		initDeserializers();
		initSerializers();
		
		logger.debug("IN");
		initEngine();
		
		Locale locale = (Locale)env.get(EngineConstants.ENV_LOCALE);	
		String language = locale.getLanguage();
		String userDateFormatPattern = (String)ConfigSingleton.getInstance().getAttribute("QBE.QBE-DATE-FORMAT." + language);
		if(userDateFormatPattern == null) userDateFormatPattern = (String)ConfigSingleton.getInstance().getAttribute("QBE.QBE-DATE-FORMAT.en");
		env.put(EngineConstants.ENV_USER_DATE_FORMAT, userDateFormatPattern);
		String databaseDateFormatPattern = (String)ConfigSingleton.getInstance().getAttribute("QBE.QBE-DATE-FORMAT.database");
		env.put(EngineConstants.ENV_DB_DATE_FORMAT, databaseDateFormatPattern);
		
		qbeEngineInstance = new QbeEngineInstance(template, env);
		logger.debug("OUT");
		return qbeEngineInstance;
	}
	
	private static void initDeserializers() {
    	SlotDeserializerFactory.getInstance();
	}


	private static void initSerializers() {
    	SlotSerializerFactory.getInstance();
	}

	
}
