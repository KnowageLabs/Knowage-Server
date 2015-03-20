/**
 * SpagoBI - The Business Intelligence Free Platform
 *
 * Copyright (C) 2004 - 2008 Engineering Ingegneria Informatica S.p.A.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 **/
package it.eng.spagobi.engines.datamining.services;

import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.utilities.engines.AbstractEngineAction;

import org.apache.log4j.Logger;

/**
 * The Class AbstractConsoleEngineAction.
 * 
 * @author Monica Franceschini
 */
public class AbstractDataMiningEngineAction extends AbstractEngineAction {

	/** Logger component. */
	private static transient Logger logger = Logger.getLogger(AbstractDataMiningEngineAction.class);

	/**
	 * Gets the console engine instance.
	 * 
	 * @return the console engine instance
	 */
	public DataMiningEngineInstance getDataMiningEngineInstance() {
		return (DataMiningEngineInstance) getEngineInstance();
	}

}
