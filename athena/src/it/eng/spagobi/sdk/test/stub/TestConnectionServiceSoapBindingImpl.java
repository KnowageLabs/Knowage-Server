/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.sdk.test.stub;

import it.eng.spagobi.sdk.test.impl.TestConnectionServiceImpl;

public class TestConnectionServiceSoapBindingImpl implements it.eng.spagobi.sdk.test.stub.TestConnectionService{
    public boolean connect() throws java.rmi.RemoteException {
    	TestConnectionServiceImpl impl = new TestConnectionServiceImpl();
    	return impl.connect();
    }

}
