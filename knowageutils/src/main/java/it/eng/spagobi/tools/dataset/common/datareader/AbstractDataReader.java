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

import java.math.BigDecimal;

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

	@Override
	public boolean isPaginationSupported() {
		return isOffsetSupported() && isFetchSizeSupported() && isMaxResultsSupported();
	}

	@Override
	public boolean isPaginationRequested() {
		return getOffset() != 0 || getFetchSize() != -1;
	}

	@Override
	public boolean isOffsetSupported() {
		return false;
	}

	@Override
	public int getOffset() {
		return offset;
	}

	@Override
	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public boolean isFetchSizeSupported() {
		return false;
	}

	@Override
	public int getFetchSize() {
		return fetchSize;
	}

	@Override
	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}

	@Override
	public boolean isMaxResultsSupported() {
		return false;
	}

	@Override
	public int getMaxResults() {
		return maxResults;
	}

	@Override
	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	@Override
	public boolean isCalculateResultNumberEnabled() {
		return calculateResultNumberEnabled;
	}

	@Override
	public void setCalculateResultNumberEnabled(boolean enabled) {
		this.calculateResultNumberEnabled = enabled;
	}

	protected Class getNewMetaType(Class oldType, Class newType) {
		if (oldType == null)
			return newType;
		if (oldType == String.class)
			return String.class;
		if (newType == Integer.class) {
			if (oldType == Double.class || oldType == Long.class || oldType == BigDecimal.class)
				return oldType;
			else
				return newType;
		} else
			return newType;
	}
}
