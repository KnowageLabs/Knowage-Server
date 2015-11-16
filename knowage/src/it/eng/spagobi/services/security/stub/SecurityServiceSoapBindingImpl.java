/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.services.security.stub;

import it.eng.spagobi.services.security.service.SecurityServiceImpl;

public class SecurityServiceSoapBindingImpl implements it.eng.spagobi.services.security.stub.SecurityService{
    public it.eng.spagobi.services.security.bo.SpagoBIUserProfile getUserProfile(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException {
	SecurityServiceImpl impl=new SecurityServiceImpl();
	return impl.getUserProfile(in0,in1);
    }

    public boolean isAuthorized(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.lang.String in3) throws java.rmi.RemoteException {
	SecurityServiceImpl impl=new SecurityServiceImpl();
	return impl.isAuthorized(in0,in1,in2,in3);
    }

    public boolean checkAuthorization(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException {
	SecurityServiceImpl impl=new SecurityServiceImpl();
	return impl.checkAuthorization(in0,in1,in2);
    }

}
