/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.services.sbidocument.stub;

public interface SbiDocumentService extends java.rmi.Remote {
    public it.eng.spagobi.services.sbidocument.bo.SpagobiAnalyticalDriver[] getDocumentAnalyticalDrivers(java.lang.String in0, java.lang.String in1, java.lang.Integer in2, java.lang.String in3, java.lang.String in4) throws java.rmi.RemoteException;
    public java.lang.String getDocumentAnalyticalDriversJSON(java.lang.String in0, java.lang.String in1, java.lang.Integer in2, java.lang.String in3, java.lang.String in4) throws java.rmi.RemoteException;
}
