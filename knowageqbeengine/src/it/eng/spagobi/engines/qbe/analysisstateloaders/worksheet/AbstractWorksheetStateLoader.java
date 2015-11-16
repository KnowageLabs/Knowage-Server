/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.analysisstateloaders.worksheet;

import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import org.json.JSONObject;

/**
 * @author Zerbetto Davide (davide.zerbetto@eng.it)
 */
public abstract class AbstractWorksheetStateLoader implements IWorksheetStateLoader {
	
	IWorksheetStateLoader nextLoader;
	
	public AbstractWorksheetStateLoader() {}

	public AbstractWorksheetStateLoader(IWorksheetStateLoader loader) {
		setNextLoader(loader);
	}
	
	public JSONObject load(String rowData) {
		JSONObject result;
		
		try {
			// load data
			result = new JSONObject(rowData);
			result = this.load(result);
		}catch(Throwable t) {
			throw new SpagoBIEngineRuntimeException("Impossible to load from rowData [" + rowData + "]", t);
		}
		
		return result;
	}
	
	public JSONObject load(JSONObject jsonObject) {
		JSONObject result;
		
		try {
			result = this.convert(jsonObject);
			// make next converts
			if (nextLoader != null) {
				result = nextLoader.load(result);
			}
		} catch(Throwable t) {
			throw new SpagoBIEngineRuntimeException("Impossible to load from JSON object [" + jsonObject + "]", t);
		}
		
		return result;
	}
	
	abstract public JSONObject convert(JSONObject data);
	
	
	public IWorksheetStateLoader getNextLoader() {
		return nextLoader;
	}

	void setNextLoader(IWorksheetStateLoader nextLoader) {
		this.nextLoader = nextLoader;
	}
}
