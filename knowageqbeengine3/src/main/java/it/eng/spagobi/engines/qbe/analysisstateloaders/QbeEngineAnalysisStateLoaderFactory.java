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
