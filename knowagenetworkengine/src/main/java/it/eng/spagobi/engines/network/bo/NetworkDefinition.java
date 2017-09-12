/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
