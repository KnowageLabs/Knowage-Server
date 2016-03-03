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
package it.eng.spagobi.engines.worksheet.template.loaders;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import org.apache.log4j.Logger;

public class Version0WorksheetXMLTemplateLoader extends AbstractWorksheetXMLTemplateLoader {

	public final static String FROM_VERSION = "0";
    public final static String TO_VERSION = "1";
    
    public static String TAG_WORKSHEET = "WORKSHEET";
	public static String TAG_WORKSHEET_DEFINITION = "WORKSHEET_DEFINITION";
	public static String TAG_QBE = "QBE";
	public static String ATTRIBUTE_VERSION = "version";
    
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(Version0WorksheetXMLTemplateLoader.class);
	
    public Version0WorksheetXMLTemplateLoader() {
    	super();
    }
    
    public Version0WorksheetXMLTemplateLoader(IWorksheetXMLTemplateLoader loader) {
    	super(loader);
    }
    
	@Override
	public SourceBean convert(SourceBean xml) {
		
		SourceBean toReturn;
		
		logger.debug("IN");
		
		try {
			logger.debug( "Converting from encoding version [" + FROM_VERSION + "] to encoding version [" + TO_VERSION + "] ..." );
			
			SourceBean qbe = xml;
			SourceBean worksheetDefinition = (SourceBean) xml.getAttribute(TAG_WORKSHEET_DEFINITION);
			qbe.delAttribute(TAG_WORKSHEET_DEFINITION);
			
			toReturn = new SourceBean(TAG_WORKSHEET);
			toReturn.setAttribute(ATTRIBUTE_VERSION, TO_VERSION);
			toReturn.setAttribute(worksheetDefinition);
			toReturn.setAttribute(qbe);
			
			logger.debug( "Conversion from encoding version [" + FROM_VERSION + "] to encoding version [" + TO_VERSION + "] terminated succesfully" );
			
		} catch(Throwable t) {
			throw new SpagoBIEngineRuntimeException("Impossible to load from rowData [" + xml + "]", t);
		} finally {
			logger.debug("OUT");
		}
		
		return toReturn;
	}

}
