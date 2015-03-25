/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.tools.dataset.wsconnectors.stub;

public class WSDataSetServiceSoapBindingImpl implements it.eng.spagobi.tools.dataset.wsconnectors.stub.IWsConnector{
    public java.lang.String readDataSet(java.lang.String in0, java.util.Map in1, java.lang.String in2) throws java.rmi.RemoteException {
        if(in2.equalsIgnoreCase("a"))
    	return "<ROWS><ROW name='io' value='30'/></ROWS>";
        if(in2.equalsIgnoreCase("b"))
        	return "<ROWS><ROW name='io' value='80'/></ROWS>";
        return null;
    }

}
	