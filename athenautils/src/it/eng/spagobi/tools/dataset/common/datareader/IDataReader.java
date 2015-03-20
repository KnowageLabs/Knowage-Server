/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
