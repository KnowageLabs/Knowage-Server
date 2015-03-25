/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.services.execute.stub;

import it.eng.spagobi.services.event.service.EventServiceImpl;
import it.eng.spagobi.services.execute.service.ServiceChartImpl;
import it.eng.spagobi.services.execute.service.ServiceKpiValueXml;

public class DocumentExecuteServiceSoapBindingImpl implements it.eng.spagobi.services.execute.stub.DocumentExecuteService{
    public byte[] executeChart(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.util.HashMap in3) throws java.rmi.RemoteException {
    	ServiceChartImpl service=new ServiceChartImpl();
    	return service.executeChart(in0, in1, in2, in3);
    }

    public java.lang.String getKpiValueXML(java.lang.String in0, java.lang.String in1, java.lang.Integer in2) throws java.rmi.RemoteException {
        ServiceKpiValueXml service = new ServiceKpiValueXml();
        return service.getKpiValueXML(in0, in1, in2);
    }

}
