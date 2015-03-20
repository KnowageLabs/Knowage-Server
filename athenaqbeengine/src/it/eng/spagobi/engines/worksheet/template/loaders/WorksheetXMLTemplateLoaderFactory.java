/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.template.loaders;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Zerbetto Davide (davide.zerbetto@eng.it)
 */
public class WorksheetXMLTemplateLoaderFactory {
	
	private static Map loaderRegistry;
	
	static {
		loaderRegistry = new HashMap();
		
		loaderRegistry.put("0", 
			new Version0WorksheetXMLTemplateLoader()
		);
	}
	
	private static WorksheetXMLTemplateLoaderFactory instance;
	public static WorksheetXMLTemplateLoaderFactory getInstance() {
		if(instance == null) {
			instance = new WorksheetXMLTemplateLoaderFactory();
		}
		return instance;
	}
	
	private WorksheetXMLTemplateLoaderFactory() {}
	
	public IWorksheetXMLTemplateLoader getLoader(String encodingFormatVersion) {
		return (IWorksheetXMLTemplateLoader) loaderRegistry.get(encodingFormatVersion);
	}
}
