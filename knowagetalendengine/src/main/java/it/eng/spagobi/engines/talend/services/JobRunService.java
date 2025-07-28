/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.talend.services;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import it.eng.spagobi.engines.talend.TalendEngine;
import it.eng.spagobi.engines.talend.exception.ContextNotFoundException;
import it.eng.spagobi.engines.talend.exception.JobExecutionException;
import it.eng.spagobi.engines.talend.exception.JobNotFoundException;
import it.eng.spagobi.engines.talend.runtime.Job;
import it.eng.spagobi.engines.talend.runtime.RuntimeRepository;
import it.eng.spagobi.utilities.engines.AbstractEngineStartServlet;
import it.eng.spagobi.utilities.engines.EngineStartServletIOManager;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

public class JobRunService extends AbstractEngineStartServlet {

	public static final String JS_FILE_ZIP = "JS_File";
	public static final String JS_EXT_ZIP = ".zip";
	
	private static final String MSG_OK = "msgOK";

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(JobRunService.class);

	@Override
	public void service(EngineStartServletIOManager servletIOManager) throws SpagoBIEngineException {

		RuntimeRepository runtimeRepository;
		Job job;

		LOGGER.debug("IN");

		try {

			// servletIOManager.auditServiceStartEvent() called before
			
			try {
				super.doService(servletIOManager);
				job = new Job(servletIOManager.getTemplateAsSourceBean());
				runtimeRepository = TalendEngine.getRuntimeRepository();
				runtimeRepository.runJob(job, servletIOManager.getEnv());
			} catch (JobNotFoundException ex) {
				LOGGER.error(ex.getMessage());

				throw new SpagoBIEngineException("Job not found", "job.not.existing");

			} catch (ContextNotFoundException ex) {
				LOGGER.error(ex.getMessage(), ex);

				throw new SpagoBIEngineException("Context script not found", "context.script.not.existing");

			} catch (JobExecutionException ex) {
				LOGGER.error(ex.getMessage(), ex);

				throw new SpagoBIEngineException("Job execution error", "job.exectuion.error");

			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);

				throw new SpagoBIEngineException("Job execution error", "job.exectuion.error");
			}

			String nextJSP = "/jsp/messageOK.jsp";
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(nextJSP);
			HttpServletRequest req = servletIOManager.getRequest();
			if (req.getAttribute(MSG_OK) != null)
				req.removeAttribute(MSG_OK);
			req.setAttribute(MSG_OK, ("Job: " + job.getName() + " - " + servletIOManager.getLocalizedMessage("etl.process.started")));
			servletIOManager.setRequest(req);
			try {
				dispatcher.forward(servletIOManager.getRequest(), servletIOManager.getResponse());

			} catch (ServletException | IOException e) {
				throw new SpagoBIEngineRuntimeException(e);
			}

			// servletIOManager.tryToWriteBackToClient(servletIOManager.getLocalizedMessage("etl.process.started")) called before

		} finally {
			LOGGER.debug("OUT");
		}
	}
}
