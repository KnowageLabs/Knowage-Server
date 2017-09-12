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
package it.eng.spagobi.engines.commonj.runtime;

import it.eng.spagobi.engines.commonj.exception.WorkExecutionException;
import it.eng.spagobi.engines.commonj.exception.WorkNotFoundException;
import it.eng.spagobi.utilities.callbacks.audit.AuditAccessUtils;


import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

public class WorksRepository {
	private File rootDir;

	private static transient Logger logger = Logger.getLogger(WorksRepository.class);

	/**
	 * Instantiates a new runtime repository.
	 * 
	 * @param rootDir the root dir
	 */
	public WorksRepository(File rootDir) {
		this.rootDir = rootDir;
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
	 * Gets the executable work project dir.
	 * 
	 * @param work the work
	 * 
	 * @return the executable work project dir
	 */

	public File getExecutableWorkProjectDir(CommonjWork work) {
		logger.debug("IN");
		File worksDir = new File(rootDir, work.getWorkName());
		logger.debug("OUT");
		return worksDir;
	}


	/**
	 * Gets the executable work dir.
	 * 
	 * @param work the work
	 * 
	 * @return the executable work dir
	 */
	public File getExecutableWorkDir(CommonjWork work) {
		logger.debug("IN");
		File workDir = new File(rootDir, work.getWorkName());
		logger.debug("OUT");
		return workDir;
	}


	/**
	 * Gets the executable work file.
	 * 
	 * @param work the work
	 * 
	 * @return the executable work file
	 */
	public File getExecutableWorkFile(CommonjWork work) {
		File workExecutableFile = new File(getExecutableWorkDir(work), work.getWorkName());	
		return workExecutableFile;
	}

	/**
	 * Contains work.
	 * 
	 * @param work the work
	 * 
	 * @return true, if successful
	 */
	public boolean containsWork(CommonjWork work) {

		File workFolder=new File(rootDir, work.getWorkName());	
		boolean exists=workFolder.exists();
		return exists;
	}



}
