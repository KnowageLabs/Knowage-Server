/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2018 Engineering Ingegneria Informatica S.p.A.
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
 *
 */

package it.eng.spagobi.tools.dataset.graph.associativity.container;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.SolrDataSet;
import it.eng.spagobi.tools.dataset.graph.Tuple;
import it.eng.spagobi.tools.dataset.graph.associativity.utils.AssociativeLogicUtils;
import it.eng.spagobi.tools.dataset.metasql.query.item.AndFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.SimpleFilter;
import it.eng.spagobi.tools.dataset.solr.ExtendedSolrQuery;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.database.DataBaseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;

import javax.naming.NamingException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 *
 */

public class SolrAssociativeDatasetContainer extends AssociativeDatasetContainer {

	private static String PROXY_USER = System.getProperty("http.proxyUser");
	private static String PROXY_PASSWORD = System.getProperty("http.proxyPassword");


	protected SolrAssociativeDatasetContainer(IDataSet dataSet, Map<String, String> parameters) {
		super(dataSet, parameters);
	}

	private SolrQuery getSolrQuery(SolrDataSet solrDataSet, String columnName) {

		ExtendedSolrQuery solrQuery = new ExtendedSolrQuery(solrDataSet.getSolrQuery()).facets(dataSet, columnName);

		if (!filters.isEmpty()) {
			solrQuery.filter(new AndFilter(filters.toArray(new SimpleFilter[0])));
		}
		solrQuery.setRows(0);
		solrQuery.setFacetMinCount(1);
		solrQuery.setFacetLimit(-1);
		return solrQuery;
	}

	@Override
	public Set<Tuple> getTupleOfValues(List<String> columnNames) throws ClassNotFoundException, NamingException, SQLException, DataBaseException, IOException, SolrServerException {
		Assert.assertTrue(columnNames.size() == 1, "Apache Solr cannot manage distinct values as tuple");
		String columnName = columnNames.get(0);

		SolrDataSet solrDataSet = dataSet.getImplementation(SolrDataSet.class);
		SolrQuery query = getSolrQuery(solrDataSet, columnName);
		SolrClient client = getSolrClient(solrDataSet.getSolrUrl());

		QueryResponse response = client.query(solrDataSet.getSolrCollection(), query);
		return AssociativeLogicUtils.getTupleOfValues(response.getFacetField(columnName));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AssociativeDatasetContainer [dataSet=");
		builder.append(dataSet);
		builder.append(", parameters=");
		builder.append(parameters);
		builder.append(", filters=");
		builder.append(filters);
		builder.append(", groups=");
		builder.append(groups);
		builder.append(", resolved=");
		builder.append(resolved);
		builder.append("]");
		return builder.toString();
	}

	private SolrClient getSolrClient(String url) {

		HttpClientBuilder httpBuilder = HttpClientBuilder.create().useSystemProperties();
		if(!StringUtils.isBlank(PROXY_USER) && !StringUtils.isBlank(PROXY_PASSWORD)) {
			CredentialsProvider provider = new BasicCredentialsProvider();
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
					PROXY_USER, PROXY_PASSWORD);
			provider.setCredentials(AuthScope.ANY, credentials);
			httpBuilder.setDefaultCredentialsProvider(provider);
		}
		HttpClient client = httpBuilder.build();

		return new HttpSolrClient.Builder(url)
				.withConnectionTimeout(1000)
				.withSocketTimeout(3000)
				.withHttpClient(client)
				.build();
	}
}
