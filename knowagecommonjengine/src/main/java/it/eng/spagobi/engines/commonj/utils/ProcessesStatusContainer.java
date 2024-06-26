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
