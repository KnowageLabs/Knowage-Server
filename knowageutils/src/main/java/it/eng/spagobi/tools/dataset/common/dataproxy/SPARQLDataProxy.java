package it.eng.spagobi.tools.dataset.common.dataproxy;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;

import it.eng.spagobi.tools.dataset.common.datareader.IDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class SPARQLDataProxy extends AbstractDataProxy {

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

		IDataStore dataStore = null;
		ResultSet resultSet;
		try {
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(sparqlEndpoint, sparqlQuery, defaultGraphIRI);

		if(executionTimeout > 60) {
			try {
				queryExecution.close();
			} catch (Exception e) {
				throw new SpagoBIRuntimeException("Cannot execute the SPARQL query because execution timeout run out.", e);
			}
		}
		queryExecution.setTimeout(executionTimeout * 1000);
		resultSet = queryExecution.execSelect();

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("An error occurred while executing SPARQL query", e);
		}
		try {
			// read data
			dataStore = dataReader.read(resultSet);
		} catch (Exception t) {
			throw new SpagoBIRuntimeException("An error occurred while parsing resultset", t);
		}
		return dataStore;
	}


}
