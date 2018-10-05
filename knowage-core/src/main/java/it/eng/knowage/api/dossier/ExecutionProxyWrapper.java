/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.knowage.api.dossier;

import java.util.List;

import it.eng.knowage.engines.dossier.template.placeholder.PlaceHolder;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.utilities.ExecutionProxy;

public class ExecutionProxyWrapper {
	
	private ExecutionProxy executionProxy;
	private ExecutionProxyResponseHandler executionProxyResponseHandler;
	
	
	
	public ExecutionProxyWrapper(BIObject bIObject,String mimeType) {
		executionProxy = new ExecutionProxy();
		executionProxyResponseHandler = new ExecutionProxyResponseHandler();
		executionProxy.setBiObject(bIObject);
		executionProxy.setMimeType(mimeType);
	}
	
	
	
	public byte[] exec(IEngUserProfile profile, String modality, String defaultOutputFormat){
		
		return executionProxy.exec(profile, modality, defaultOutputFormat);
	}

	public void handleByteResponse(byte[] response,String randomKey,List<PlaceHolder> placeHolders){
		
		 executionProxyResponseHandler.handleByteResponse(response, this.executionProxy.getBiObject(), randomKey, placeHolders);
	}

	

	
	

}
