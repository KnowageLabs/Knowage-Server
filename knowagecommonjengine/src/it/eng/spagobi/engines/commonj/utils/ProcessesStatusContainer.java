/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.commonj.utils;

import it.eng.spagobi.engines.commonj.runtime.CommonjWorkContainer;

import java.util.HashMap;


public class ProcessesStatusContainer {


	private static ProcessesStatusContainer istanza;
	/** Maps process Pid to its container*/
	public HashMap<String, CommonjWorkContainer> pidContainerMap;

	/** Maps process Pid to its parameters*/
	public HashMap<String, java.util.Map> pidParametersMap;

	
	
	private ProcessesStatusContainer()
	{
		pidContainerMap = new HashMap<String, CommonjWorkContainer>();
	}

	public static synchronized ProcessesStatusContainer getInstance()
	{
		if (istanza == null)
		{
			istanza = new ProcessesStatusContainer();
		}

		return istanza;
	}

	public HashMap<String, CommonjWorkContainer> getPidContainerMap() {
		return pidContainerMap;
	}

	public void setPidContainerMap(
			HashMap<String, CommonjWorkContainer> pidContainerMap) {
		this.pidContainerMap = pidContainerMap;
	}


	
}
