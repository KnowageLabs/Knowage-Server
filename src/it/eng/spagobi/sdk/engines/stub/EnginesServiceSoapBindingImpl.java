/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.sdk.engines.stub;

import it.eng.spagobi.sdk.engines.impl.EnginesServiceImpl;
import it.eng.spagobi.services.dataset.service.DataSetServiceImpl;

public class EnginesServiceSoapBindingImpl implements it.eng.spagobi.sdk.engines.stub.EnginesService{
    public it.eng.spagobi.sdk.engines.bo.SDKEngine[] getEngines() throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
       	EnginesServiceImpl supplier=new EnginesServiceImpl();
    	return supplier.getEngines();
     }

    public it.eng.spagobi.sdk.engines.bo.SDKEngine getEngine(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException {
       	EnginesServiceImpl supplier=new EnginesServiceImpl();
    	return supplier.getEngine(in0);
    }

}
