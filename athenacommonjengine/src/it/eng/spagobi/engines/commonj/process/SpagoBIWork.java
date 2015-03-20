/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
