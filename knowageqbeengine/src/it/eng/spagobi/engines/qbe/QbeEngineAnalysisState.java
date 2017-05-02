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
package it.eng.spagobi.engines.qbe;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.catalogue.QueryCatalogue;
import it.eng.qbe.query.serializer.SerializerFactory;
import it.eng.spagobi.commons.QbeEngineStaticVariables;
import it.eng.spagobi.engines.qbe.analysisstateloaders.IQbeEngineAnalysisStateLoader;
import it.eng.spagobi.engines.qbe.analysisstateloaders.QbeEngineAnalysisStateLoaderFactory;
import it.eng.spagobi.utilities.engines.EngineAnalysisState;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it), Davide Zerbetto (davide.zerbetto@eng.it), Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class QbeEngineAnalysisState extends EngineAnalysisState {

	private Locale locale;
	/** Logger component. */
	private static transient Logger logger = Logger.getLogger(QbeEngineAnalysisState.class);

	public QbeEngineAnalysisState(IDataSource dataSource) {
		super();
		setDataSource(dataSource);
	}

	public QbeEngineAnalysisState(IDataSource dataSource, Locale locale) {
		super();
		setDataSource(dataSource);
		setLocale(locale);
	}

	@Override
	public void load(byte[] rowData) throws SpagoBIEngineException {
		String str = null;
		JSONObject abalysisStateJSON = null;
		JSONObject rowDataJSON = null;
		String encodingFormatVersion;

		logger.debug("IN");

		try {
			str = new String(rowData);
			logger.debug("loading analysis state from row data [" + str + "] ...");

			rowDataJSON = new JSONObject(str);
			try {
				// encodingFormatVersion = rowDataJSON.getString("version");
				encodingFormatVersion = String.valueOf(rowDataJSON.getInt("version")); // Jackson management
			} catch (JSONException e) {
				encodingFormatVersion = "0";
			}

			logger.debug("Row data encoding version  [" + encodingFormatVersion + "]");

			if (encodingFormatVersion.equalsIgnoreCase(QbeEngineStaticVariables.CURRENT_QUERY_VERSION)) {
				abalysisStateJSON = rowDataJSON;
			} else {
				logger.warn("Row data encoding version [" + encodingFormatVersion + "] does not match with the current version used by the engine ["
						+ QbeEngineStaticVariables.CURRENT_QUERY_VERSION + "] ");
				logger.debug("Converting from encoding version [" + encodingFormatVersion + "] to encoding version ["
						+ QbeEngineStaticVariables.CURRENT_QUERY_VERSION + "]....");
				IQbeEngineAnalysisStateLoader analysisStateLoader;
				analysisStateLoader = QbeEngineAnalysisStateLoaderFactory.getInstance().getLoader(encodingFormatVersion);
				if (analysisStateLoader == null) {
					throw new SpagoBIEngineException("Unable to load data stored in format [" + encodingFormatVersion + "] ");
				}
				abalysisStateJSON = analysisStateLoader.load(str);
				logger.debug("Encoding conversion has been executed succesfully");
			}

			JSONObject catalogueJSON = abalysisStateJSON.getJSONObject("catalogue");
			setProperty(QbeEngineStaticVariables.CATALOGUE, catalogueJSON);

			logger.debug("analysis state loaded succsfully from row data");
		} catch (JSONException e) {
			throw new SpagoBIEngineException("Impossible to load analysis state from raw data", e);
		} finally {
			logger.debug("OUT");
		}
	}

	@Override
	public byte[] store() throws SpagoBIEngineException {
		JSONObject catalogueJSON = null;
		JSONObject rowDataJSON = null;
		String rowData = null;

		catalogueJSON = (JSONObject) getProperty(QbeEngineStaticVariables.CATALOGUE);

		try {
			rowDataJSON = new JSONObject();
			rowDataJSON.put("version", QbeEngineStaticVariables.CURRENT_QUERY_VERSION);
			rowDataJSON.put("catalogue", catalogueJSON);

			rowData = rowDataJSON.toString();
		} catch (Throwable e) {
			throw new SpagoBIEngineException("Impossible to store analysis state from catalogue object", e);
		}

		return rowData.getBytes();
	}

	public QueryCatalogue getCatalogue() {
		QueryCatalogue catalogue;
		JSONObject catalogueJSON;
		JSONArray queriesJSON;
		JSONObject queryJSON;
		Query query;

		catalogue = new QueryCatalogue();
		catalogueJSON = (JSONObject) getProperty(QbeEngineStaticVariables.CATALOGUE);
		try {
			queriesJSON = catalogueJSON.getJSONArray("queries");

			for (int i = 0; i < queriesJSON.length(); i++) {
				queryJSON = queriesJSON.getJSONObject(i);
				query = SerializerFactory.getDeserializer("application/json").deserializeQuery(queryJSON, getDataSource());
				catalogue.addQuery(query);
			}
		} catch (Throwable e) {
			throw new SpagoBIEngineRuntimeException("Impossible to deserialize catalogue", e);
		}

		return catalogue;
	}

	public void setCatalogue(QueryCatalogue catalogue) {
		Set queries;
		Query query;
		JSONObject queryJSON;
		JSONArray queriesJSON;
		JSONObject catalogueJSON;
		JSONArray graphJSON;

		catalogueJSON = new JSONObject();
		queriesJSON = new JSONArray();

		try {
			queries = catalogue.getAllQueries(false);
			Iterator it = queries.iterator();
			while (it.hasNext()) {
				query = (Query) it.next();
				queryJSON = (JSONObject) SerializerFactory.getSerializer("application/json").serialize(query, getDataSource(), getLocale());

				queriesJSON.put(queryJSON);
			}

			catalogueJSON.put("queries", queriesJSON);
		} catch (Throwable e) {
			throw new SpagoBIEngineRuntimeException("Impossible to serialize catalogue", e);
		}

		setProperty(QbeEngineStaticVariables.CATALOGUE, catalogueJSON);
	}

	public IDataSource getDataSource() {
		return (IDataSource) getProperty(QbeEngineStaticVariables.DATASOURCE);
	}

	public void setDataSource(IDataSource dataSource) {
		setProperty(QbeEngineStaticVariables.DATASOURCE, dataSource);
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

}
