/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.services.sbidocument.stub;

import it.eng.spagobi.services.sbidocument.service.SbiDocumentServiceImpl;

public class SbiDocumentServiceSoapBindingImpl implements it.eng.spagobi.services.sbidocument.stub.SbiDocumentService{
    public it.eng.spagobi.services.sbidocument.bo.SpagobiAnalyticalDriver[] getDocumentAnalyticalDrivers(java.lang.String in0, java.lang.String in1, java.lang.Integer in2, java.lang.String in3, java.lang.String in4) throws java.rmi.RemoteException {
        SbiDocumentServiceImpl impl = new SbiDocumentServiceImpl();
        return impl.getDocumentAnalyticalDrivers(in0, in1, in2, in3, in4);
    }

    public java.lang.String getDocumentAnalyticalDriversJSON(java.lang.String in0, java.lang.String in1, java.lang.Integer in2, java.lang.String in3, java.lang.String in4) throws java.rmi.RemoteException {
        SbiDocumentServiceImpl impl = new SbiDocumentServiceImpl();
        return impl.getDocumentAnalyticalDriversJSON(in0, in1, in2, in3, in4);
    }

}
