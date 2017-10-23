/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.talend.runtime;

import it.eng.spagobi.engines.talend.exception.ContextNotFoundException;
import it.eng.spagobi.engines.talend.exception.JobExecutionException;
import it.eng.spagobi.engines.talend.exception.JobNotFoundException;
import it.eng.spagobi.engines.talend.utils.TalendScriptAccessUtils;
import it.eng.spagobi.engines.talend.utils.ZipUtils;
import it.eng.spagobi.utilities.callbacks.audit.AuditAccessUtils;


import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Andrea Gioia
 *
 */
public class RuntimeRepository {
	private File rootDir;
	
	/**
	 * Instantiates a new runtime repository.
	 * 
	 * @param rootDir the root dir
	 */
	public RuntimeRepository(File rootDir) {
		this.rootDir = rootDir;
	}
	
	/**
	 * Deploy job.
	 * 
	 * @param jobDeploymentDescriptor the job deployment descriptor
	 * @param executableJobFiles the executable job files
	 */
	public void deployJob(JobDeploymentDescriptor jobDeploymentDescriptor, ZipFile executableJobFiles) {
		File jobsDir = new File(rootDir, jobDeploymentDescriptor.getLanguage().toLowerCase());
		File projectDir = new File(jobsDir, jobDeploymentDescriptor.getProject());		
		ZipUtils.unzipSkipFirstLevel(executableJobFiles, projectDir);		
	}
	
	/**
	 * Run job.
	 * 
	 * @param job the job
	 * @param env the environment

	 * 
	 * @throws JobNotFoundException the job not found exception
	 * @throws ContextNotFoundException the context not found exception
	 * @throws JobExecutionException the job execution exception
	 */
	public void runJob(Job job, Map env) throws JobNotFoundException, ContextNotFoundException, JobExecutionException {
		IJobRunner jobRunner;
		
		jobRunner = getJobRunner( job.getLanguage() );
		if(jobRunner != null) {
			jobRunner.run(job, env);
		}		
	}
	
	/**
	 * Gets the job runner.
	 * 
	 * @param jobLanguage the job language
	 * 
	 * @return the job runner
	 */
	public IJobRunner getJobRunner(String jobLanguage) {
		if(jobLanguage.equalsIgnoreCase("java")) {
			return new JavaJobRunner(this);
		} else if(jobLanguage.equalsIgnoreCase("perl")) {
			return new PerlJobRunner(this);
		} else {
			return null;
		}
	}

	/**
	 * Gets the root dir.
	 * 
	 * @return the root dir
	 */
	public File getRootDir() {
		return rootDir;
	}

	/**
	 * Sets the root dir.
	 * 
	 * @param rootDir the new root dir
	 */
	public void setRootDir(File rootDir) {
		this.rootDir = rootDir;
	}
	
	/**
	 * Gets the executable job project dir.
	 * 
	 * @param job the job
	 * 
	 * @return the executable job project dir
	 */
	public File getExecutableJobProjectDir(Job job) {
		File jobsDir = new File(rootDir, job.getLanguage().toLowerCase());
		File projectDir = new File(jobsDir, job.getProject());
		return projectDir;
	}
	
	/**
	 * Gets the executable job dir.
	 * 
	 * @param job the job
	 * 
	 * @return the executable job dir
	 */
	public File getExecutableJobDir(Job job) {
		File jobDir = new File(getExecutableJobProjectDir(job), job.getName());
		return jobDir;
	}
	
	/**
	 * Gets the executable job file.
	 * 
	 * @param job the job
	 * 
	 * @return the executable job file
	 */
	public File getExecutableJobFile(Job job) {
		File jobExecutableFile = new File(getExecutableJobDir(job), TalendScriptAccessUtils.getExecutableFileName(job));	
		return jobExecutableFile;
	}
	
	/**
	 * Contains job.
	 * 
	 * @param job the job
	 * 
	 * @return true, if successful
	 */
	public boolean containsJob(Job job) {
		return getExecutableJobFile(job).exists();
	}
	
	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * 
	 * @throws ZipException the zip exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws ZipException, IOException {
		File rootDir = new File("C:\\Prototipi\\SpagoBI-Demo-1.9.2\\webapps\\SpagoBITalendEngine\\RuntimeRepository");
		File zipFile = new File("C:\\Prototipi\\TalendJob2.zip");
		RuntimeRepository runtimeRepository = new RuntimeRepository(rootDir);
		JobDeploymentDescriptor jobDeploymentDescriptor = new JobDeploymentDescriptor("PP2", "perl");
		runtimeRepository.deployJob(jobDeploymentDescriptor, new ZipFile(zipFile));
	}
}
