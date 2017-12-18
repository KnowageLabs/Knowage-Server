/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.talend;



import java.io.File;

import it.eng.spagobi.engines.talend.runtime.RuntimeRepository;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

/**
 * @author Andrea Gioia
 *
 */
public class TalendEngine  {
	
	private static TalendEngineVersion version;
	private static TalendEngineConfig config;	
		
	
	private static RuntimeRepository runtimeRepository;
		
	
	static { 
		TalendEngine.version = TalendEngineVersion.getInstance();
		TalendEngine.config = TalendEngineConfig.getInstance();
		
		File rrRootDir = TalendEngineConfig.getInstance().getRuntimeRepositoryRootDir();
		TalendEngine.setRuntimeRepository( new RuntimeRepository(rrRootDir) );		
	}
	
	
	public static RuntimeRepository getRuntimeRepository() throws SpagoBIEngineException {
		if(runtimeRepository == null || !runtimeRepository.getRootDir().exists()) {
			throw new SpagoBIEngineException("Runtime-Repository not available",
					"repository.not.available");
		}
		return TalendEngine.runtimeRepository;
	}


	private static void setRuntimeRepository(RuntimeRepository runtimeRepository) {
		TalendEngine.runtimeRepository = runtimeRepository;
	}


	public static TalendEngineVersion getVersion() {
		return TalendEngine.version;
	}


	public static TalendEngineConfig getConfig() {
		return TalendEngine.config;
	}

	
	
	
	
}
