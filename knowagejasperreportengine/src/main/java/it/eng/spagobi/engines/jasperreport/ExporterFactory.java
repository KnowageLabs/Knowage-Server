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
package it.eng.spagobi.engines.jasperreport;

import net.sf.jasperreports.engine.JRExporter;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.services.common.EnginConf;
/**
 * 
 * @deprecated
 */
public class ExporterFactory {	
	
	/**
	 * Gets the exporter.
	 * 
	 * @param format the format
	 * 
	 * @return the exporter
	 */
	static public JRExporter getExporter(String format) {
		JRExporter exporter = null;
		
		SourceBean config = EnginConf.getInstance().getConfig();
		SourceBean exporterConfig = (SourceBean) config.getFilteredSourceBeanAttribute ("EXPORTERS.EXPORTER", "format", format);
		if(exporterConfig == null) return null;
		String exporterClassName = (String)exporterConfig.getAttribute("class");
		if(exporterClassName == null) return exporter;
		
		try {
			exporter = (JRExporter)Class.forName(exporterClassName).newInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return exporter;
	}
	
	/**
	 * Gets the mIME type.
	 * 
	 * @param format the format
	 * 
	 * @return the mIME type
	 */
	static public String getMIMEType(String format) {
		String mimeType = null;
		SourceBean config = EnginConf.getInstance().getConfig();
		SourceBean exporterConfig = (SourceBean) config.getFilteredSourceBeanAttribute ("EXPORTERS.EXPORTER", "format", format);
		if(exporterConfig == null) return null;
		mimeType = (String)exporterConfig.getAttribute("mime");
		return mimeType;
	}
	
	/**
	 * Gets the default type.
	 * 
	 * @return the default type
	 */
	static public String getDefaultType(){
		String defaultType = null;
		SourceBean config = EnginConf.getInstance().getConfig();
		defaultType = (String)config.getAttribute("EXPORTERS.default");
		if(defaultType == null) defaultType = "html";
		return defaultType;
	}
}
