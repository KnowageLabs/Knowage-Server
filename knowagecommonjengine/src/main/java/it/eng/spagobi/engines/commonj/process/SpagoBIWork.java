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
package it.eng.spagobi.engines.commonj.process;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import commonj.work.Work;

public class SpagoBIWork implements Work{
	/** pid of the work */ 
	String pid;
	/** label of the dcument */ 
	String sbiLabel;
	/** boolean to check to see if work has been stopped, set to false by release method*/
	volatile boolean running=false;
	/** here are put allr equests parameters */ 
	Map sbiParameters=new HashMap();
	/**names of those in sbiParameters that are analytical drivers*/
	Vector<String> analyticalParameters;
	/** Logger */
	static private Logger logger = Logger.getLogger(SpagoBIWork.class);

	public boolean isDaemon() {
		// TODO Auto-generated method stub
		return false;
	}


	public void release() {

		running=false;
	}

	public void run() {
		running=true;		
	}


	public boolean isRunning() {
		return running;
	}


	public void setRunning(boolean running) {
		this.running = running;
	}


	public Map getSbiParameters() {
		return sbiParameters;
	}


	public void setSbiParameters(Map sbiParameters) {
		this.sbiParameters = sbiParameters;
	}



	public Vector<String> getAnalyticalParameters() {
		return analyticalParameters;
	}


	public void setAnalyticalParameters(Vector<String> analyticalParameters) {
		this.analyticalParameters = analyticalParameters;
	}


	public String getPid() {
		return pid;
	}


	public void setPid(String pid) {
		this.pid = pid;
	}


	public String getSbiLabel() {
		return sbiLabel;
	}


	public void setSbiLabel(String sbiLabel) {
		this.sbiLabel = sbiLabel;
	}



}
