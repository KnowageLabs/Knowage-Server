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

package it.eng.spagobi.engines.network.services;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.InputStream;

import org.apache.log4j.Logger;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class NetworkExportAction extends AbstractNetworkEngineAction{

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(NetworkExportAction.class);

	private static final long serialVersionUID = 7229174935514794865L;
	private static final String EXPORTED_FILE_NAME = "ExportedNetwork";
	
	public void service(SourceBean serviceRequest, SourceBean serviceResponse)  {
		try {
			super.service(serviceRequest, serviceResponse);
			
			InputStream inputStream = this.getHttpRequest().getInputStream();

			String mimeType = getAttributeAsString("type");
			String fileName = EXPORTED_FILE_NAME+ (System.currentTimeMillis())+"."+mimeType;
			writeBackToClient(inputStream, null, false, fileName, mimeType);
			
			
		} catch (Exception e) {
			logger.error("Error exporting the network",e);
			throw new SpagoBIRuntimeException("Error exporting the network",e);
		}

		
	}
}
