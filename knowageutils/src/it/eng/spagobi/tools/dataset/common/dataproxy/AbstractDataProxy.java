/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.common.dataproxy;

import java.util.Map;

public abstract class AbstractDataProxy implements IDataProxy {

	Map parameters;
	Map profile;
	int offset;
	int fetchSize;
	int maxResults;
	boolean calculateResultNumberOnLoad;
	String statement;
	String resPath;

	public String getResPath() {
		return resPath;
	}

	public void setResPath(String resPath) {
		this.resPath = resPath;
	}

	public Map getParameters() {
		return parameters;
	}

	public void setParameters(Map parameters) {
		this.parameters = parameters;
	}

	public Map getProfile() {
		return profile;
	}

	public void setProfile(Map profile) {
		this.profile = profile;
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
		return true;
	}
	
	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}
	
	public boolean isCalculateResultNumberOnLoadEnabled() {
		return calculateResultNumberOnLoad;
	}
	
	public void setCalculateResultNumberOnLoad(boolean enabled) {
		calculateResultNumberOnLoad = enabled;
	}
	
	public long getResultNumber() {
		return -1;
	}
	
	public String getStatement() {
		return statement;
	}

	public void setStatement(String statement) {
		this.statement = statement;
	}
	
}
