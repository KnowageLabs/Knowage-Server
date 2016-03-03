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
package it.eng.spagobi.engines.qbe.services.core;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.query.Query;
import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.utilities.engines.AbstractEngineAction;
import it.eng.spagobi.utilities.engines.EngineConstants;

import org.apache.log4j.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractQbeEngineAction.
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public abstract class AbstractQbeEngineAction extends AbstractEngineAction {
	
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(AbstractQbeEngineAction.class);
    
	
    public QbeEngineInstance getEngineInstance() {
    	return (QbeEngineInstance)getAttributeFromSession( EngineConstants.ENGINE_INSTANCE );
    }
    
	public IDataSource getDataSource() {
		QbeEngineInstance qbeEngineInstance  = null;
    	qbeEngineInstance = getEngineInstance();
    	if(qbeEngineInstance == null) {
    		return null;
    	}
    	return qbeEngineInstance.getDataSource();
	}

	public void setDataSource(IDataSource dataSource) {
		QbeEngineInstance qbeEngineInstance  = null;
    	qbeEngineInstance = getEngineInstance();
    	if(qbeEngineInstance == null) {
    		return;
    	}
    	qbeEngineInstance.setDataSource(dataSource);
	}
	
	
	public Query getQuery() {
		QbeEngineInstance qbeEngineInstance  = null;
    	qbeEngineInstance = getEngineInstance();
    	if(qbeEngineInstance == null) {
    		return null;
    	}
    	return qbeEngineInstance.getActiveQuery();
	}	
	
}
