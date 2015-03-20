/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */


package it.eng.spagobi.services.artifact.stub;

import it.eng.spagobi.services.artifact.service.ArtifactServiceImpl;

public class ArtifactServiceSoapBindingImpl implements it.eng.spagobi.services.artifact.stub.ArtifactService{
    public javax.activation.DataHandler getArtifactContentByNameAndType(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.lang.String in3) throws java.rmi.RemoteException {
    		ArtifactServiceImpl service=new ArtifactServiceImpl();
        	return service.getArtifactContentByNameAndType(in0, in1, in2, in3);
    }

    public javax.activation.DataHandler getArtifactContentById(java.lang.String in0, java.lang.String in1, java.lang.Integer in2) throws java.rmi.RemoteException {
    	ArtifactServiceImpl service=new ArtifactServiceImpl();
        return service.getArtifactContentById(in0, in1, in2);
    }
    
    public it.eng.spagobi.services.artifact.bo.SpagoBIArtifact[] getArtifactsByType(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException {
    	ArtifactServiceImpl service=new ArtifactServiceImpl();
        return service.getArtifactsByType(in0, in1, in2);
    }

}
