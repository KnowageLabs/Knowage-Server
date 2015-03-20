/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.analysisstateloaders.formbuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Zerbetto Davide (davide.zerbetto@eng.it)
 */
public class FormStateLoaderFactory {
	
	private static Map loaderRegistry;
	
	static {
		loaderRegistry = new HashMap();
		
		loaderRegistry.put("0", 
			new Version0FormStateLoader()
		);
	}
	
	private static FormStateLoaderFactory instance;
	public static FormStateLoaderFactory getInstance() {
		if(instance == null) {
			instance = new FormStateLoaderFactory();
		}
		return instance;
	}
	
	private FormStateLoaderFactory() {}
	
	public IFormStateLoader getLoader(String encodingFormatVersion) {
		return (IFormStateLoader) loaderRegistry.get(encodingFormatVersion);
	}
}
