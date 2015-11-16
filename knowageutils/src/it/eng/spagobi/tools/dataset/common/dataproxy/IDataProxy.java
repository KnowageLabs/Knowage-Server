/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.dataproxy;

import it.eng.spagobi.tools.dataset.common.datareader.IDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;

import java.util.Map;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public interface IDataProxy {
	
	IDataStore load(IDataReader dataReader);
	
	// for querable dataset...
	public String getStatement();
	public void setStatement(String statement);
	
	public String getResPath();
	public void setResPath(String resPath);
	
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
	boolean isCalculateResultNumberOnLoadEnabled();
	
	/**
	 * Set if the calculation of the total result number is enabled or not.
	 * In case this calculation is required, invoke this method with true, otherwise with false.
	 */
	void setCalculateResultNumberOnLoad(boolean enabled);
	long getResultNumber();
	
	// profilation ...
	Map getParameters();
	void setParameters(Map parameters);
	Map getProfile();
	void setProfile(Map profile);
}
