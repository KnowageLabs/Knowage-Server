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

package it.eng.spagobi.engines.datamining;

import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

/**
 * @author Monica Franceschini
 */

/**
 * The Class DataMiningEngineException.
 */
public class DataMiningEngineRuntimeException extends SpagoBIEngineRuntimeException {

	/**
	 * The hints. List hints;
	 */

	DataMiningEngineInstance engineInstance;

	/**
	 * Builds a <code>DataMiningException</code>.
	 * 
	 * @param message
	 *            Text of the exception
	 */
	public DataMiningEngineRuntimeException(String message) {
		super(message);
	}

	/**
	 * Builds a <code>DataMiningEngineException</code>.
	 * 
	 * @param message
	 *            Text of the exception
	 * @param ex
	 *            previous Throwable object
	 */
	public DataMiningEngineRuntimeException(String message, Throwable ex) {
		super(message, ex);
	}

	@Override
	public DataMiningEngineInstance getEngineInstance() {
		return engineInstance;
	}

	public void setEngineInstance(DataMiningEngineInstance engineInstance) {
		this.engineInstance = engineInstance;
	}

}
