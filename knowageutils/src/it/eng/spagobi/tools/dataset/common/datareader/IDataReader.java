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
package it.eng.spagobi.tools.dataset.common.datareader;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;

/**
 * @author Angelo Bernabei
 *         angelo.bernabei@eng.it
 */
public interface IDataReader {
    IDataStore read( Object data )throws EMFUserError, EMFInternalError;
    
    // pagination ...
	boolean isPaginationSupported();
	boolean isOffsetSupported();
	int getOffset();
	void setOffset(int offset);
	boolean isFetchSizeSupported();
	int getFetchSize();
	void setFetchSize(int fetchSize);
	boolean isMaxResultsSupported();
	int getMaxResults();
	void setMaxResults(int maxResults);
	
	/**
	 * Return if the calculation of the total result number is enabled or not (may be it is not necessary)
	 */
	public boolean isCalculateResultNumberEnabled();
	
	/**
	 * Set if the calculation of the total result number is enabled or not.
	 * In case this calculation is required, invoke this method with true, otherwise with false.
	 */
	public void setCalculateResultNumberEnabled(boolean enabled);
	
}
