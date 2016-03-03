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
