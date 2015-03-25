/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.services.event.stub;

import it.eng.spagobi.services.event.service.EventServiceImpl;

public class EventServiceSoapBindingImpl implements it.eng.spagobi.services.event.stub.EventService{
    public java.lang.String fireEvent(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.lang.String in3, java.lang.String in4, java.lang.String in5) throws java.rmi.RemoteException {
	EventServiceImpl service=new EventServiceImpl();
	return service.fireEvent(in0, in1, in2, in3, in4, in5);
    }

}
