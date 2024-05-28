/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.talend.runtime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import it.eng.spagobi.engines.talend.TalendEngineConfig;
import it.eng.spagobi.engines.talend.exception.ContextNotFoundException;
import it.eng.spagobi.engines.talend.exception.JobExecutionException;
import it.eng.spagobi.engines.talend.exception.JobNotFoundException;
import it.eng.spagobi.engines.talend.utils.TalendScriptAccessUtils;
import it.eng.spagobi.services.proxy.EventServiceProxy;
import it.eng.spagobi.utilities.engines.AuditServiceProxy;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.threadmanager.WorkManager;

public class PerlJobRunner implements IJobRunner {

	private static final Logger LOGGER = Logger.getLogger(PerlJobRunner.class);

	private final RuntimeRepository runtimeRepository;

	public static final String DEFAULT_CONTEXT = "Default";

	PerlJobRunner(RuntimeRepository runtimeRepository) {
		this.runtimeRepository = runtimeRepository;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.talend.runtime.IJobRunner#run(it.eng.spagobi.engines.talend.runtime.Job, java.util.Map,
	 * it.eng.spagobi.utilities.callbacks.audit.AuditAccessUtils, java.lang.String)
	 */
	@Override
	public void run(Job job, Map parameters)
			throws JobNotFoundException, ContextNotFoundException, JobExecutionException {

		File contextTempScriptFile = null;

		LOGGER.debug("Starting run method with parameters: " + "project = [" + job.getProject() + "] ; "
				+ "job name = [" + job.getName() + "] ; " + "context = [" + job.getContext() + "] ; " + "base dir = ["
				+ runtimeRepository.getRootDir() + "].");

		File executableJobDir = runtimeRepository.getExecutableJobDir(job);
		LOGGER.debug("Job base folder: " + executableJobDir);

		if (!runtimeRepository.containsJob(job)) {
			throw new JobNotFoundException(
					"Job executable file " + runtimeRepository.getExecutableJobFile(job) + " does not exist.");
		}

		TalendEngineConfig config = TalendEngineConfig.getInstance();
		String cmd = config.getPerlInstallDir() + File.separatorChar + config.getPerlBinDir() + File.separatorChar
				+ config.getPerlCommand() + " " + TalendScriptAccessUtils.getExecutableFileName(job);

		// if the context is not specified, it looks for the defualt context script file;
		// if it exists, the context is assumed to be the default.
		// Instead, if the context is specified, it looks for the relevant context script file;
		// if it does not exist, an error message is returned.
		File contextFile = null;

		if (job.getContext() == null || job.getContext().trim().equals("")) {
			LOGGER.debug("Context not specified.");
			job.setContext(DEFAULT_CONTEXT);
			String contextFileName = TalendScriptAccessUtils.getContextFileName(job);
			File defaultContextFile = new File(executableJobDir, contextFileName);
			if (defaultContextFile.exists()) {
				LOGGER.debug("Found default script context file.");
				contextFile = defaultContextFile;
			}
		} else {
			String contextFileName = TalendScriptAccessUtils.getContextFileName(job);
			contextFile = new File(executableJobDir, contextFileName);
			if (!contextFile.exists()) {
				throw new ContextNotFoundException("Script context file " + contextFile + " does not exist.");
			}
		}

		// only if the context file exists, we consider document parameter
		if (contextFile != null) {
			if (parameters.isEmpty()) {
				String contextFileName = contextFile.getName();
				if (contextFileName.indexOf(File.separatorChar) != -1) {
					contextFileName = contextFileName.substring(contextFileName.lastIndexOf(File.separatorChar));
				}
				cmd = cmd + " --context=" + contextFileName;
			} else {
				try {
					contextTempScriptFile = createTempContextScriptFile(contextFile, parameters);
				} catch (Exception e) {
					LOGGER.error("Error while creating temp context file", e);
					LOGGER.error("Document parameter cannot be considered.");
				}
				cmd = cmd + " --context=\"" + contextTempScriptFile.getAbsolutePath() + "\"";

			}
		}

		LOGGER.debug("Perl execution command: " + cmd);

		List<File> filesToBeDeleted = new ArrayList<>();
		filesToBeDeleted.add(contextTempScriptFile);

		try {
			WorkManager wm = new WorkManager();
			TalendWork jrt = new TalendWork(cmd, null, executableJobDir, filesToBeDeleted, parameters);
			TalendWorkListener listener = new TalendWorkListener(
					(AuditServiceProxy) parameters.get(EngineConstants.ENV_AUDIT_SERVICE_PROXY),
					(EventServiceProxy) parameters.get(EngineConstants.ENV_EVENT_SERVICE_PROXY));
			wm.run(jrt, listener);
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	private File createTempContextScriptFile(File contextScriptFile, Map parameters) throws Exception {
		try (FileReader reader = new FileReader(contextScriptFile)) {
			StringBuilder filebuff = new StringBuilder();
			char[] buffer = new char[1024];
			int len;
			while ((len = reader.read(buffer)) >= 0) {
				filebuff.append(buffer, 0, len);
			}
			Set entries = parameters.entrySet();
			Iterator it = entries.iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				String parameterName = entry.getKey().toString();
				Object parameterValueObj = entry.getValue();
				LOGGER.debug("Found parameter '" + parameterName + "' with value '" + parameterValueObj + "'.");
				if (parameterValueObj == null || parameterValueObj.toString().trim().equals("")) {
					LOGGER.debug("Parameter '" + parameterName + "' has no value.");
					continue;
				}
				String parameterValue = parameterValueObj.toString();
				int index = filebuff.indexOf("$_context{" + parameterName + "}");
				if (index < 0) {
					LOGGER.error("Parameter '" + parameterName + "' not found in context script file.");
					continue;
				} else {
					int startIndex = filebuff.indexOf("=", index) + 1;
					int endIndex = filebuff.indexOf(";", startIndex);
					filebuff.replace(startIndex, endIndex, parameterValue);
				}
			}
			String contextScriptFilePath = contextScriptFile.getAbsolutePath();
			String contextScriptDirPath = contextScriptFilePath.substring(0,
					contextScriptFilePath.lastIndexOf(File.separatorChar));
			String tempDirPath = contextScriptDirPath + File.separatorChar + ".." + File.separatorChar + ".."
					+ File.separatorChar + "temp";
			File tempDir = new File(tempDirPath);
			if (!tempDir.exists())
				tempDir.mkdir();
			else {
				if (!tempDir.isDirectory())
					tempDir.delete();
			}
			String tempFileName = UUID.randomUUID().toString();
			TalendEngineConfig config = TalendEngineConfig.getInstance();
			File contextScriptTempFile = new File(
					tempDirPath + File.separatorChar + tempFileName + config.getPerlExt());
			try (FileOutputStream fos = new FileOutputStream(contextScriptTempFile)) {
				fos.write(filebuff.toString().getBytes());
				fos.flush();
			}
			return contextScriptTempFile;
		}

	}

}
