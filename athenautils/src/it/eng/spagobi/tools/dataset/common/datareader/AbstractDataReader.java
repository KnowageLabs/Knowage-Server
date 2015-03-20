/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.datareader;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public abstract class AbstractDataReader implements IDataReader {
	int offset;
	int fetchSize;
	int maxResults;
	boolean calculateResultNumberEnabled;
	
	public AbstractDataReader() {
		offset = -1;
		fetchSize = -1;
		maxResults = -1;
	}
	
	public boolean isPaginationSupported() {
		return isOffsetSupported() && isMaxResultsSupported();
	}
	
	public boolean isOffsetSupported() {
		return false;
	}
	
	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public boolean isFetchSizeSupported() {
		return false;
	}
	
	public int getFetchSize() {
		return fetchSize;
	}

	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}

	public boolean isMaxResultsSupported() {
		return false;
	}
	
	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	public boolean isCalculateResultNumberEnabled() {
		return calculateResultNumberEnabled;
	}

	public void setCalculateResultNumberEnabled(boolean enabled) {
		this.calculateResultNumberEnabled = enabled;
	}
}
