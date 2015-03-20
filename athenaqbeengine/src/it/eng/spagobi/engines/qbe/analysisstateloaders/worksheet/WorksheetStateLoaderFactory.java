/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.analysisstateloaders.worksheet;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Zerbetto Davide (davide.zerbetto@eng.it)
 */
public class WorksheetStateLoaderFactory {
	
	private static Map loaderRegistry;
	
	static {
		loaderRegistry = new HashMap();
		AbstractWorksheetStateLoader l0 = new Version0WorksheetStateLoader();
		AbstractWorksheetStateLoader l1 = new Version1WorksheetStateLoader();
		l0.setNextLoader(l1);
		loaderRegistry.put("0", l0);
		loaderRegistry.put("1", l1);
	}
	
	private static WorksheetStateLoaderFactory instance;
	public static WorksheetStateLoaderFactory getInstance() {
		if(instance == null) {
			instance = new WorksheetStateLoaderFactory();
		}
		return instance;
	}
	
	private WorksheetStateLoaderFactory() {}
	
	public IWorksheetStateLoader getLoader(String encodingFormatVersion) {
		return (IWorksheetStateLoader) loaderRegistry.get(encodingFormatVersion);
	}
}
