/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.commonj;



import java.io.File;

import it.eng.spagobi.engines.commonj.runtime.WorksRepository;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

public class CommonjEngine  {
	
	private static CommonjEngineConfig config;	
		
	
	private static WorksRepository worksRepository;
		
	
	static { 
		CommonjEngine.config = CommonjEngineConfig.getInstance();
		
		File rrRootDir = CommonjEngineConfig.getInstance().getWorksRepositoryRootDir();
		CommonjEngine.setWorksRepository( new WorksRepository(rrRootDir) );		
	}
	
	
	public static WorksRepository getWorksRepository() throws SpagoBIEngineException {
		if(worksRepository == null || !worksRepository.getRootDir().exists()) {
			throw new SpagoBIEngineException("Works-Repository not available",
					"repository.not.available");
		}
		return CommonjEngine.worksRepository;
	}


	private static void setWorksRepository(WorksRepository worksRepository) {
		CommonjEngine.worksRepository = worksRepository;
	}



	public static CommonjEngineConfig getConfig() {
		return CommonjEngine.config;
	}

	
	
	
	
}
