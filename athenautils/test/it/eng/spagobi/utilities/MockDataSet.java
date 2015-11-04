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