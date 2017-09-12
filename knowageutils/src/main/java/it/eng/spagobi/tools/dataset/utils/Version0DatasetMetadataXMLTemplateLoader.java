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
package it.eng.spagobi.tools.dataset.utils;

import org.apache.log4j.Logger;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;


/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class Version0DatasetMetadataXMLTemplateLoader extends
		AbstractDatasetMetadataXMLTemplateLoader {

	public final static String FROM_VERSION = "0";
    public final static String TO_VERSION = "1";
    
	public static final String META = "META"; 
	public static final String DATASET = "DATASET"; 
	public static final String ATTRIBUTE_VERSION = "version"; 



    
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(Version0DatasetMetadataXMLTemplateLoader.class);
	
    public Version0DatasetMetadataXMLTemplateLoader() {
    	super();
    }
    
    public Version0DatasetMetadataXMLTemplateLoader(IDatasetMetadataXMLTemplateLoader loader) {
    	super(loader);
    }
    
	
	
	@Override
	public SourceBean convert(SourceBean xml) {
		SourceBean toReturn;
		
		logger.debug("IN");
		try {
			logger.debug( "Converting from encoding version [" + FROM_VERSION + "] to encoding version [" + TO_VERSION + "] ..." );
			
			
			SourceBean datasetMetadataOld = xml;
			
			toReturn = new SourceBean(META);
			SourceBean sbDataset = new SourceBean(DATASET);
			SourceBeanAttribute version = new SourceBeanAttribute(ATTRIBUTE_VERSION, TO_VERSION);
			toReturn.setAttribute(version);
			toReturn.setAttribute(datasetMetadataOld);
			toReturn.setAttribute(sbDataset);

			
			
			logger.debug( "Conversion from encoding version [" + FROM_VERSION + "] to encoding version [" + TO_VERSION + "] terminated succesfully" );

			
		} catch(Throwable t) {
			throw new SpagoBIEngineRuntimeException("Impossible to load from rowData [" + xml + "]", t);
		} finally {
			logger.debug("OUT");
		}
		
		return toReturn;
		
		
		
	}

}
