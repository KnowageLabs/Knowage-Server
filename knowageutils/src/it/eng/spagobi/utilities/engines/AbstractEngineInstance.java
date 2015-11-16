/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.engines;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public abstract class AbstractEngineInstance implements IEngineInstance {
	private String id;
	private Map env;

	private EngineAnalysisMetadata analysisMetadata;
	
	public AbstractEngineInstance() {
		this( new HashMap() );
	}
	
	public AbstractEngineInstance(Map env) {
		id = "id_" + System.currentTimeMillis();
		setEnv( env );
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public Map getEnv() {
		return env;
	}

	public void setEnv(Map env) {
		this.env = env;
	}

	public EngineAnalysisMetadata getAnalysisMetadata() {
		return analysisMetadata;
	}

	public void setAnalysisMetadata(EngineAnalysisMetadata analysisMetadata) {
		this.analysisMetadata = analysisMetadata;
	}
	
}
