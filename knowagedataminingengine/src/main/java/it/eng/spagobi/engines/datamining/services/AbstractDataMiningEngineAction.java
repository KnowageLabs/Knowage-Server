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
