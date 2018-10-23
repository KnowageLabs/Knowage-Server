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
package it.eng.spagobi.tools.dataset.common.dataproxy;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.log4j.Logger;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spagobi.tools.dataset.common.datareader.IDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class SPARQLDataProxy extends AbstractDataProxy {

	private static final Logger logger = Logger.getLogger(SPARQLDataProxy.class);

	protected final String sparqlEndpoint;
	protected final String sparqlQuery;
	protected final String defaultGraphIRI;
	protected final int executionTimeout;



	public SPARQLDataProxy(String sparqlEndpoint, String sparqlQuery, String defaultGraphIRI, int executionTimeout) {
		super();
		this.sparqlEndpoint = sparqlEndpoint;
		this.sparqlQuery = sparqlQuery;
		this.defaultGraphIRI = defaultGraphIRI;
		this.executionTimeout = executionTimeout;
	}



	@Override
	public IDataStore load(IDataReader dataReader) {
		logger.debug("IN");
		IDataStore dataStore = null;

		try (QueryExecution queryExecution = QueryExecutionFactory.sparqlService(sparqlEndpoint, sparqlQuery, defaultGraphIRI)) {
			ResultSet resultSet = executeSPARQLQuery(queryExecution);
			dataStore = readResultSet(dataReader, dataStore, resultSet);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("An error occurred while executing SPARQL query", e);
		}
		logger.debug("OUT");
		return dataStore;
	}



	private IDataStore readResultSet(IDataReader dataReader, IDataStore dataStore, ResultSet resultSet) {
		Monitor monitor = MonitorFactory.start("Knowage.SPARQLDataProxy.readResultSet");
		try {
			dataStore = dataReader.read(resultSet);
		} catch (Exception t) {
			throw new SpagoBIRuntimeException("An error occurred while parsing resultset", t);
		} finally {
			monitor.stop();
		}
		return dataStore;
	}



	private ResultSet executeSPARQLQuery(QueryExecution queryExecution) {
		queryExecution.setTimeout(executionTimeout * 1000);
		Monitor monitor = MonitorFactory.start("Knowage.SPARQLDataProxy.executeSPARQLQuery");
		ResultSet resultSet = null;
		try {
			resultSet = queryExecution.execSelect();
		} finally {
			monitor.stop();
		}
		return resultSet;
	}


}
