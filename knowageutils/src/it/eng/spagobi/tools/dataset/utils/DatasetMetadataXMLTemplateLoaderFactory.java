/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

**/
package it.eng.spagobi.tools.dataset.utils;


import java.util.HashMap;
import java.util.Map;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class DatasetMetadataXMLTemplateLoaderFactory {

	private static Map loaderRegistry;
	
	static {
		loaderRegistry = new HashMap();
		
		loaderRegistry.put("0", 
			new Version0DatasetMetadataXMLTemplateLoader()
		);
	}
	
	private static DatasetMetadataXMLTemplateLoaderFactory instance;
	public static DatasetMetadataXMLTemplateLoaderFactory getInstance() {
		if(instance == null) {
			instance = new DatasetMetadataXMLTemplateLoaderFactory();
		}
		return instance;
	}
	
	private DatasetMetadataXMLTemplateLoaderFactory() {}
	
	public IDatasetMetadataXMLTemplateLoader getLoader(String encodingFormatVersion) {
		return (IDatasetMetadataXMLTemplateLoader) loaderRegistry.get(encodingFormatVersion);
	}
}
