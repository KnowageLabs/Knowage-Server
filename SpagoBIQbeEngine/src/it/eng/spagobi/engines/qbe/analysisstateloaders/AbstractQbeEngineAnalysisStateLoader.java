/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
