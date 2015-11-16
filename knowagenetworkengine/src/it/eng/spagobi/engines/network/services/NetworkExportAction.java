/* SpagoBI, the Open Source Business Intelligencae suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.network.services;

import it.eng.spago.base.SourceBean;

import java.io.InputStream;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class NetworkExportAction extends AbstractNetworkEngineAction{


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
			// TODO: handle exception
			System.out.println("eeee");
		}

		
	}
}
