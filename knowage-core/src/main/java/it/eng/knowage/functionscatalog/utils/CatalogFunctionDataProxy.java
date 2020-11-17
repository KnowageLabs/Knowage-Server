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

package it.eng.knowage.functionscatalog.utils;

import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;

import it.eng.spagobi.tools.dataset.common.dataproxy.AbstractDataProxy;
import it.eng.spagobi.tools.dataset.common.dataproxy.RESTDataProxyException;
import it.eng.spagobi.tools.dataset.common.datareader.IDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.utilities.rest.RestUtilities.HttpMethod;
import it.eng.spagobi.utilities.rest.RestUtilities.Response;

public class CatalogFunctionDataProxy extends AbstractDataProxy {

	private String requestBody;
	private String address;
	private final Map<String, String> requestHeaders;
	private final HttpMethod method;
	private IDataStore dataStore;

	public CatalogFunctionDataProxy(String address, HttpMethod method, Map<String, String> requestHeaders, String requestBody) {
		this.requestBody = requestBody;
		this.address = address;
		this.requestHeaders = requestHeaders;
		this.method = method;
	}

	@Override
	public IDataStore load(IDataReader dataReader) {
		try {
			Helper.checkNotNull(dataReader, "dataReader");
			Response response = RestUtilities.makeRequest(this.method, this.address, this.requestHeaders, this.requestBody);
			String responseBody = response.getResponseBody();
			if (response.getStatusCode() != HttpStatus.SC_OK) {
				throw new RESTDataProxyException(
						String.format("The response status is not ok: status=%d, response=%s", response.getStatusCode(), responseBody));
			}
			Assert.assertNotNull(responseBody, "responseBody is null");
			dataReader.setCalculateResultNumberEnabled(true);
			IDataStore res = dataReader.read(responseBody);
			Assert.assertNotNull(res, "datastore is null");
			return res;
		} catch (RESTDataProxyException e) {
			throw e;
		} catch (Exception e) {
			throw new RESTDataProxyException(e);
		}
	}

	public IDataStore getDataStore() {
		return dataStore;
	}

	protected void setDataStore(IDataStore dataStore) {
		this.dataStore = dataStore;
	}
}
