/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.analysisstateloaders;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class QbeEngineAnalysisStateLoaderFactory {
	
	private static Map loaderRegistry;
	
	static {
		loaderRegistry = new HashMap();
		
		loaderRegistry.put("6", 
				new Version6QbeEngineAnalysisStateLoader(
				)
			);
		
		loaderRegistry.put("5", 
			new Version6QbeEngineAnalysisStateLoader(
				new Version5QbeEngineAnalysisStateLoader()
			)
		);
		
		loaderRegistry.put("4", 
			new Version6QbeEngineAnalysisStateLoader(
				new Version5QbeEngineAnalysisStateLoader(
						new Version4QbeEngineAnalysisStateLoader()
				)
			)
		);
		
		loaderRegistry.put("3", 
			new Version6QbeEngineAnalysisStateLoader(
				new Version5QbeEngineAnalysisStateLoader(
					new Version4QbeEngineAnalysisStateLoader(
						new Version3QbeEngineAnalysisStateLoader()
					)
				)
			)
		);
		
		loaderRegistry.put("2", 
			new Version6QbeEngineAnalysisStateLoader(
				new Version5QbeEngineAnalysisStateLoader(
					new Version4QbeEngineAnalysisStateLoader(
						new Version3QbeEngineAnalysisStateLoader(
							new Version2QbeEngineAnalysisStateLoader()
						)
					)
				)
			)
		);
		loaderRegistry.put("1", 
			new Version6QbeEngineAnalysisStateLoader(
				new Version5QbeEngineAnalysisStateLoader(
					new Version4QbeEngineAnalysisStateLoader(	
						new Version3QbeEngineAnalysisStateLoader(
							new Version2QbeEngineAnalysisStateLoader(
								new Version1QbeEngineAnalysisStateLoader()
							)
						)
					)
				)
			)
		);
		loaderRegistry.put("0", 
			new Version6QbeEngineAnalysisStateLoader(
				new Version5QbeEngineAnalysisStateLoader(
					new Version4QbeEngineAnalysisStateLoader(
						new Version3QbeEngineAnalysisStateLoader(
							new Version2QbeEngineAnalysisStateLoader(
								new Version1QbeEngineAnalysisStateLoader(
									new Version0QbeEngineAnalysisStateLoader()
								)
							)
						)
					)
				)
			)
		);
	}
	
	private static QbeEngineAnalysisStateLoaderFactory instance;
	public static QbeEngineAnalysisStateLoaderFactory getInstance() {
		if(instance == null) {
			instance = new QbeEngineAnalysisStateLoaderFactory();
		}
		return instance;
	}
	
	private QbeEngineAnalysisStateLoaderFactory() {}
	
	public IQbeEngineAnalysisStateLoader getLoader(String encodingFormatVersion) {
		return (IQbeEngineAnalysisStateLoader)loaderRegistry.get(encodingFormatVersion);
	}
}
