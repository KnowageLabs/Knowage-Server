/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.network.bo;

import it.eng.spagobi.utilities.engines.EngineAnalysisState;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class NetworkDefinition extends EngineAnalysisState{

	public static final String CURRENT_VERSION = "1";
	
	private String networkXML;
	

	/* (non-Javadoc)
	 * @see it.eng.spagobi.utilities.engines.IEngineAnalysisState#load(byte[])
	 */
	public void load(byte[] rowData) throws SpagoBIEngineException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.utilities.engines.IEngineAnalysisState#store()
	 */
	public byte[] store() throws SpagoBIEngineException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNetworkXML() {
		return networkXML;
	}

	public void setNetworkXML(String networkXML) {
		this.networkXML = networkXML;
	}
	
	


}
