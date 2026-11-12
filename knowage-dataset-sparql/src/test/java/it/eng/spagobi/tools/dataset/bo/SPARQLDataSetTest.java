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
package it.eng.spagobi.tools.dataset.bo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONObject;
import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.dataproxy.SPARQLDataProxy;
import it.eng.spagobi.tools.dataset.common.datareader.SPARQLDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.constants.SPARQLDatasetConstants;

public class SPARQLDataSetTest {

	@Test
	public void factoryLoadsSparqlDatasetFromExistingTypeMapping() throws Exception {
		SpagoBiDataSet configuration = new SpagoBiDataSet();
		configuration.setType(SPARQLDataSet.DATASET_TYPE);
		configuration.setConfiguration(configurationFor("http://127.0.0.1/sparql").toString());

		IDataSet dataSet = DataSetFactory.getDataSet(configuration, "test-user");

		assertTrue(dataSet instanceof SPARQLDataSet);
		SPARQLDataSet sparqlDataSet = (SPARQLDataSet) dataSet;
		assertTrue(sparqlDataSet.getDataProxy() instanceof SPARQLDataProxy);
		assertTrue(sparqlDataSet.getDataReader() instanceof SPARQLDataReader);
	}

	@Test
	public void extractedProviderExecutesQueryAndReadsSparqlResults() throws Exception {
		AtomicBoolean requested = new AtomicBoolean();
		HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/sparql", exchange -> {
			requested.set(true);
			byte[] response = ("{\"head\":{\"vars\":[\"item\",\"label\"]},\"results\":{\"bindings\":["
					+ "{\"item\":{\"type\":\"uri\",\"value\":\"http://example.test/item\"},"
					+ "\"label\":{\"type\":\"literal\",\"value\":\"Example\"}}]}}").getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().set("Content-Type", "application/sparql-results+json");
			exchange.sendResponseHeaders(200, response.length);
			try (OutputStream body = exchange.getResponseBody()) {
				body.write(response);
			}
		});
		server.start();
		try {
			String endpoint = "http://127.0.0.1:" + server.getAddress().getPort() + "/sparql";
			SPARQLDataSet dataSet = new SPARQLDataSet(configurationFor(endpoint));

			IDataStore result = dataSet.getDataProxy().load(dataSet.getDataReader());

			assertTrue(requested.get());
			assertEquals(1, result.getRecordsCount());
			assertEquals("item", result.getMetaData().getFieldMeta(0).getName());
			assertEquals("label", result.getMetaData().getFieldMeta(1).getName());
			assertEquals("http://example.test/item", result.getRecordAt(0).getFields().get(0).getValue());
			assertEquals("Example", result.getRecordAt(0).getFields().get(1).getValue());
		} finally {
			server.stop(0);
		}
	}

	private JSONObject configurationFor(String endpoint) throws Exception {
		JSONObject configuration = new JSONObject();
		configuration.put(SPARQLDatasetConstants.SPARQL_ENDPOINT, endpoint);
		configuration.put(SPARQLDatasetConstants.SPARQL_QUERY, "SELECT ?item ?label WHERE { ?item ?property ?label }");
		configuration.put(SPARQLDatasetConstants.SPARQL_DEFAULT_GRAPH_IRI, "http://example.test/graph");
		configuration.put(SPARQLDatasetConstants.SPARQL_EXECUTION_TIMEOUT, "5");
		return configuration;
	}
}
