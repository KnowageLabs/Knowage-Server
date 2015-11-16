/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
