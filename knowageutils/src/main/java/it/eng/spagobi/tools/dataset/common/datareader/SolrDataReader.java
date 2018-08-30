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
import it.eng.spagobi.tools.dataset.common.datastore.*;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.tools.dataset.solr.SolrDataStore;
import it.eng.spagobi.utilities.Helper;
import org.apache.log4j.Logger;
import org.json.JSONException;

import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SolrDataReader extends JSONPathDataReader {

	private static final String KEY_HEADER = "key";
	private static final String VALUE_HEADER = "value";
	static private Logger logger = Logger.getLogger(SolrDataReader.class);
	private boolean hasFacetQuery;
	private boolean hasFacets;
	private int resultNumber = -1;

	public SolrDataReader(String jsonPathItems, boolean hasFacets, boolean hasFacetQuery) {
		this(jsonPathItems, null, false, false);

	}

	public SolrDataReader(String jsonPathItems, List<JSONPathAttribute> jsonPathAttributes, boolean hasFacets, boolean hasFacetQuery) {
		super(jsonPathItems, jsonPathAttributes, false, false);
		this.hasFacetQuery = hasFacetQuery;
		this.hasFacets = hasFacets;

	}

	@Override
	protected void addFieldMetadata(IMetaData dataStoreMeta, List<Object> parsedData) {
		if (!hasFacets) {
			super.addFieldMetadata(dataStoreMeta, parsedData);
		} else {
			// key field
			FieldMetadata fm = new FieldMetadata();
			fm.setAlias(KEY_HEADER);
			fm.setName(KEY_HEADER);
			fm.setType(String.class);
			dataStoreMeta.addFiedMeta(fm);

			fm = new FieldMetadata();
			fm.setAlias(VALUE_HEADER);
			fm.setName(VALUE_HEADER);
			fm.setType(Double.class);
			dataStoreMeta.addFiedMeta(fm);
		}

	}

	@Override
	protected void addData(String data, IDataStore dataStore, IMetaData dataStoreMeta, List<Object> parsedData, boolean skipPagination)
			throws ParseException, JSONException {
		if (!hasFacets) {
			super.addData(data, dataStore, dataStoreMeta, parsedData, true);
		} else {
			int rowFetched = 0;

			if (hasFacetQuery) {// if the facets are calculated using query facet
				resultNumber = 0;
				if (isCalculateResultNumberEnabled()) {
					for (int j = 0; j < parsedData.size(); j++) {
						if (maxResults <= 0 || rowFetched < maxResults) {
							Map<Object, Object> aMap = (Map<Object, Object>) parsedData.get(j);
							resultNumber = resultNumber + aMap.keySet().size();
						}
					}
				}

				for (int j = 0; j < parsedData.size(); j++) {
					if (maxResults <= 0 || rowFetched < maxResults) {
						Map<Object, Object> aMap = (Map<Object, Object>) parsedData.get(j);
						for (Iterator iterator = aMap.keySet().iterator(); iterator.hasNext();) {
							IRecord record = new Record(dataStore);
							Object key = iterator.next();
							record.appendField(new Field(key));
							record.appendField(new Field(aMap.get(key)));
							dataStore.appendRecord(record);
							rowFetched++;
						}
					}
				}
			} else {// facet field

				for (int j = 0; j < parsedData.size(); j++) {
					if (maxResults <= 0 || rowFetched < maxResults) {
						IRecord record = new Record(dataStore);
						IField field = new Field(parsedData.get(j));
						record.appendField(field);

						field = new Field(parsedData.get(j + 1));
						record.appendField(field);
						dataStore.appendRecord(record);
						rowFetched++;
					}
					j = j + 1;
				}
			}

			logger.debug("Insert [" + dataStore.getRecordsCount() + "] records");

		}

	}

	@Override
	public IDataStore read(Object data) {
		if (!hasFacets) {
			IDataStore ds = super.read(data);
			Object parsed = JsonPath.read((String) data, "$.response.numFound");
			ds.getMetaData().setProperty("resultNumber", parsed);
			return ds;
		} else {
			Helper.checkNotNull(data, "data");
			if (!(data instanceof String)) {
				throw new IllegalArgumentException("data must be a string");
			}
			String d = (String) data;
			try {
				IDataStore dataStore = new SolrDataStore();
				IMetaData dataStoreMeta = new MetaData();
				dataStore.setMetaData(dataStoreMeta);
				List<Object> parsedData = getItems(d);
				addFieldMetadata(dataStoreMeta, parsedData);
				addData(d, dataStore, dataStoreMeta, parsedData, true);
				dataStore.getMetaData().setProperty("resultNumber", getResultNumber());
				return dataStore;
			} catch (ParseException e) {
				throw new JSONPathDataReaderException(e);
			} catch (JSONPathDataReaderException e) {
				throw e;
			} catch (Exception e) {
				throw new JSONPathDataReaderException(e);
			}
		}
	}

	public int getResultNumber() {
		return resultNumber;
	}

	public void setResultNumber(int resultNumber) {
		this.resultNumber = resultNumber;
	}

}
