/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.talend.runtime;

import it.eng.spagobi.engines.talend.exception.ContextNotFoundException;
import it.eng.spagobi.engines.talend.exception.JobExecutionException;
import it.eng.spagobi.engines.talend.exception.JobNotFoundException;
import it.eng.spagobi.utilities.callbacks.audit.AuditAccessUtils;

import java.util.Map;

import javax.servlet.http.HttpSession;

/**
 * @author Andrea Gioia
 *
 */
public interface IJobRunner {
	
	
	 /**
 	 * Run.
 	 * 
 	 * @param job the job
 	 * @param parameters the parameters
 	 * @param auditAccessUtils the audit access utils
 	 * @param auditId the audit id
 	 * 
 	 * @throws JobNotFoundException the job not found exception
 	 * @throws ContextNotFoundException the context not found exception
 	 * @throws JobExecutionException the job execution exception
 	 */
 	public abstract void run(Job job, Map env) 
	 	throws JobNotFoundException, ContextNotFoundException, JobExecutionException;

}
