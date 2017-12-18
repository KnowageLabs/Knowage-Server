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
package it.eng.spago.engines.datamining.work;

import org.apache.log4j.Logger;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;

import commonj.work.Work;

/*
 * This class load R instance implementing a single Work used by the WorkManager.
 * */
public class DataminingWriteWork implements Work {

	static private Logger logger = Logger.getLogger(DataminingWriteWork.class);
	REngine rEngine = null;

	public void run() {
		rEngine = REngine.getLastEngine();

		if (rEngine == null) {
			try {
				/*
				 * attention : setting to --save the engine doesn't remove object from the current environment,so it's unrelevant the workspace saving if you
				 * don't remove manualli the R objects first through the rm command:re = new REngine(new String[] { "--save" }, false, null);
				 */
				rEngine = REngine.engineForClass("org.rosuda.REngine.JRI.JRIEngine", new String[] { "--vanilla" }, null, false);
				logger.debug("new R engine created");

			} catch (Exception e) {
				logger.error("Error during creation of new R instance");
			}
		} else {
			// Clean workspace
			try {
				rEngine.parseAndEval("rm(list=ls())");
			} catch (REngineException e) {
				logger.error("Error during creation of new R instance");
			} catch (REXPMismatchException e) {
				logger.error("Error during creation of new R instance");
			}
		}
	}

	public REngine getrEngine() {
		return rEngine;
	}

	public void setrEngine(REngine rEngine) {
		this.rEngine = rEngine;
	}

	public boolean isDaemon() {
		return true;
	}

	public void release() {
		rEngine = null;
		return;
	}

}
