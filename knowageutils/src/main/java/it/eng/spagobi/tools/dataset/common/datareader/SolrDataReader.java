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

import com.jayway.jsonpath.JsonPath;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import org.apache.log4j.Logger;
import org.json.JSONException;

import java.text.ParseException;
import java.util.List;

public class SolrDataReader extends JSONPathDataReader {

	static private Logger logger = Logger.getLogger(SolrDataReader.class);
	protected int resultNumber = -1;

	public SolrDataReader(String jsonPathItems) {
		this(jsonPathItems, null);
	}

	public SolrDataReader(String jsonPathItems, List<JSONPathAttribute> jsonPathAttributes) {
		super(jsonPathItems, jsonPathAttributes, false, false);
	}

	@Override
	protected void addFieldMetadata(IMetaData dataStoreMeta, List<Object> parsedData) {
			super.addFieldMetadata(dataStoreMeta, parsedData);
	}

	@Override
	protected void addData(String data, IDataStore dataStore, IMetaData dataStoreMeta, List<Object> parsedData, boolean skipPagination)
			throws ParseException, JSONException {
			super.addData(data, dataStore, dataStoreMeta, parsedData, true);
			logger.debug("Insert [" + dataStore.getRecordsCount() + "] records");
	}

	@Override
	public IDataStore read(Object data) {
			IDataStore ds = super.read(data);
			Object parsed = JsonPath.read((String) data, "$.response.numFound");
			ds.getMetaData().setProperty("resultNumber", parsed);
			return ds;
	}

	public int getResultNumber() {
		return resultNumber;
	}

	public void setResultNumber(int resultNumber) {
		this.resultNumber = resultNumber;
	}

}
