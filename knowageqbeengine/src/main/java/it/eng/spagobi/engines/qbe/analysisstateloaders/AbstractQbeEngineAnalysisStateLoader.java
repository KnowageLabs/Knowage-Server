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
package it.eng.spagobi.engines.qbe.analysisstateloaders;

import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public abstract class AbstractQbeEngineAnalysisStateLoader implements IQbeEngineAnalysisStateLoader{
	
	IQbeEngineAnalysisStateLoader previousLoader;
	
	public AbstractQbeEngineAnalysisStateLoader() {}

	public AbstractQbeEngineAnalysisStateLoader(IQbeEngineAnalysisStateLoader loader) {
		setPreviousLoader(loader);
	}
	
	public JSONObject load(String rowData) {
		JSONObject result;
		
		try {
			result = previousLoader != null? (JSONObject)previousLoader.load(rowData): new JSONObject(rowData);
			result = this.convert(result);
		}catch(Throwable t) {
			throw new SpagoBIEngineRuntimeException("Impossible to load from rowData [" + rowData + "]", t);
		}
		
		return result;
	}
	
	abstract public JSONObject convert(JSONObject data);
	
	
	public IQbeEngineAnalysisStateLoader getPreviousLoader() {
		return previousLoader;
	}

	void setPreviousLoader(IQbeEngineAnalysisStateLoader previousLoader) {
		this.previousLoader = previousLoader;
	}
}
