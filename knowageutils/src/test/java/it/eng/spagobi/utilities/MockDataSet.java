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
package it.eng.spagobi.utilities;

import it.eng.spagobi.tools.dataset.bo.AbstractDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.datasource.bo.IDataSource;

import java.util.Map;

public class MockDataSet extends AbstractDataSet {

	@SuppressWarnings("rawtypes")
	@Override
	public Map getUserProfileAttributes() {

		return null;
	}

	@Override
	public void setUserProfileAttributes(Map<String, Object> attributes) {

	}

	@Override
	public IDataStore getDataStore() {

		return null;
	}

	@Override
	public void setAbortOnOverflow(boolean abortOnOverflow) {

	}

	@Override
	public void addBinding(String bindingName, Object bindingValue) {

	}

	@Override
	public IDataStore test() {

		return null;
	}

	@Override
	public IDataStore test(int offset, int fetchSize, int maxResults) {

		return null;
	}

	@Override
	public String getSignature() {

		return null;
	}

	@Override
	public IDataStore decode(IDataStore datastore) {

		return null;
	}

	@Override
	public boolean isCalculateResultNumberOnLoadEnabled() {

		return false;
	}

	@Override
	public void setCalculateResultNumberOnLoad(boolean enabled) {

	}

	@Override
	public void setDataSource(IDataSource dataSource) {

	}

	@Override
	public IDataSource getDataSource() {

		return null;
	}

}