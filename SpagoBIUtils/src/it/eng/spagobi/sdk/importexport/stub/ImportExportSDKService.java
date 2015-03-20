/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */


/**
 * ImportExportSDKService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package it.eng.spagobi.sdk.importexport.stub;

public interface ImportExportSDKService extends java.rmi.Remote {
    public it.eng.spagobi.sdk.importexport.bo.SDKFile importDocuments(it.eng.spagobi.sdk.importexport.bo.SDKFile in0, it.eng.spagobi.sdk.importexport.bo.SDKFile in1, boolean in2) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;
}
