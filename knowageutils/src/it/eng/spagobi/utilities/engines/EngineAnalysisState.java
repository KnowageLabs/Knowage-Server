/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.engines;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public abstract class EngineAnalysisState implements IEngineAnalysisState {
	
	Map properties;
	
	/**
	 * Instantiates a new engine analysis state.
	 * 
	 * @param rowData the row data
	 */
	public EngineAnalysisState() {
		properties = new HashMap();
	}
	

	/* (non-Javadoc)
	 * @see it.eng.spagobi.utilities.engines.IEngineAnalysisState#getProperty(java.lang.Object)
	 */
	public Object getProperty(Object pName) {
		return properties.get( pName ); 
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.utilities.engines.IEngineAnalysisState#setProperty(java.lang.Object, java.lang.Object)
	 */
	public void setProperty(Object pName, Object pValue) {
		properties.put( pName, pValue ); 
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.utilities.engines.IEngineAnalysisState#containsProperty(java.lang.Object)
	 */
	public boolean containsProperty(Object pName) {
		return properties.containsKey( pName ); 
	}
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.utilities.engines.IEngineAnalysisState#propertyNameSet()
	 */
	public Set propertyNameSet() {
		return properties.keySet(); 
	}
	
	public String toString() {
		StringBuffer buffer = null;
		Iterator it = null;
		
		buffer = new StringBuffer();
		it = propertyNameSet().iterator();
		while( it.hasNext() ) {
			Object pName = it.next();
			Object pValue = getProperty( pName );
			buffer.append( pName.toString() + "=" + pValue.toString() + "; ");
		}
		
		return buffer.toString();
	}
	
}
