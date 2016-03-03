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
