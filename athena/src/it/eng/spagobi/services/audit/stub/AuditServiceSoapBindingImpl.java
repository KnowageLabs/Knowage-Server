/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.services.audit.stub;

import it.eng.spagobi.services.audit.service.AuditServiceImpl;

public class AuditServiceSoapBindingImpl implements it.eng.spagobi.services.audit.stub.AuditService{
    public java.lang.String log(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.lang.String in3, java.lang.String in4, java.lang.String in5, java.lang.String in6, java.lang.String in7) throws java.rmi.RemoteException {
	AuditServiceImpl service=new AuditServiceImpl();
	return service.log(in0, in1, in2, in3, in4, in5, in6, in7);
    }

}
