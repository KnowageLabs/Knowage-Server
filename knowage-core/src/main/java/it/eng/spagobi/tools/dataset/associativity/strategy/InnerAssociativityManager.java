/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.tools.dataset.associativity.strategy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tools.dataset.associativity.AbstractAssociativityManager;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.query.PreparedStatementData;
import it.eng.spagobi.tools.dataset.cache.query.SelectQuery;
import it.eng.spagobi.tools.dataset.cache.query.item.MultipleProjectionSimpleFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.SimpleFilter;
import it.eng.spagobi.tools.dataset.graph.EdgeGroup;
import it.eng.spagobi.tools.dataset.graph.LabeledEdge;
import it.eng.spagobi.tools.dataset.graph.associativity.AssociativeDatasetContainer;
import it.eng.spagobi.tools.dataset.graph.associativity.Config;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 *
 */

public class InnerAssociativityManager extends AbstractAssociativityManager {

	private static Logger logger = Logger.getLogger(InnerAssociativityManager.class);

	public InnerAssociativityManager(Config config, UserProfile userProfile) throws Exception {
		init(config, userProfile);
	}

	@Override
	protected void initProcess() {
		try {
			for (String v1 : graph.vertexSet()) {
				result.getDatasetToEdgeGroup().put(v1, new HashSet<EdgeGroup>());
				for (String v2 : graph.vertexSet()) {
					if (!v1.equals(v2)) {
						Set<LabeledEdge<String>> edges = graph.getAllEdges(v1, v2);
						if (!edges.isEmpty()) {
							EdgeGroup group = new EdgeGroup(edges);
							result.getDatasetToEdgeGroup().get(v1).add(group);

							if (!documentsAndExcludedDatasets.contains(v1)) {
								AssociativeDatasetContainer container = associativeDatasetContainers.get(v1);
								String tableName = container.getTableName();
								IDataSet dataSet = container.getDataSet();
								IDataSource dataSource = container.getDataSource();

								List<String> columnNames = getColumnNames(group.getOrderedEdgeNames(), v1);
								if (!columnNames.isEmpty()) {
									PreparedStatementData data = new SelectQuery(dataSet).selectDistinct().select(columnNames.toArray(new String[0]))
											.from(tableName).getPreparedStatementData(dataSource);
									Set<String> tuple = getTupleOfValues(v1, data.getQuery(), data.getValues());

									if (!result.getEdgeGroupValues().containsKey(group)) {
										result.getEdgeGroupValues().put(group, tuple);
									} else {
										result.getEdgeGroupValues().get(group).retainAll(tuple);
									}
								}
							}

							if (!result.getEdgeGroupToDataset().containsKey(group)) {
								result.getEdgeGroupToDataset().put(group, new HashSet<String>());
								result.getEdgeGroupToDataset().get(group).add(v1);
							} else {
								result.getEdgeGroupToDataset().get(group).add(v1);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error during the initializing of the AssociativeLogicManager", e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void calculateDatasets(String datasetLabel, EdgeGroup fromEdgeGroup, SimpleFilter filter) throws Exception {
		Set<EdgeGroup> groups = result.getDatasetToEdgeGroup().get(datasetLabel);
		AssociativeDatasetContainer container = associativeDatasetContainers.get(datasetLabel);
		String tableName = container.getTableName();
		IDataSet dataSet = container.getDataSet();
		IDataSource dataSource = container.getDataSource();

		// iterate over all the associations
		for (EdgeGroup group : groups) {
			List<String> columnNames = getColumnNames(group.getOrderedEdgeNames(), datasetLabel);
			if (columnNames.size() > 0) {
				PreparedStatementData data = new SelectQuery(dataSet).selectDistinct().select(columnNames.toArray(new String[0])).from(tableName).where(filter)
						.getPreparedStatementData(dataSource);

				Set<String> distinctValues = getTupleOfValues(datasetLabel, data.getQuery(), data.getValues());

				Set<String> baseSet = result.getEdgeGroupValues().get(group);
				Set<String> intersection = new HashSet<String>(CollectionUtils.intersection(baseSet, distinctValues));
				if (!intersection.equals(baseSet)) {
					if (intersection.size() > 0) {
						result.getEdgeGroupValues().put(group, intersection);

						for (String datasetInvolved : result.getEdgeGroupToDataset().get(group)) {
							if (!documentsAndExcludedDatasets.contains(datasetInvolved) && !datasetInvolved.equals(datasetLabel)) {
								columnNames = getColumnNames(group.getOrderedEdgeNames(), datasetInvolved);
								if (columnNames.size() > 0) {
									MultipleProjectionSimpleFilter whereClause = container.buildInFilter(dataSet, columnNames, intersection);
									// it will skip the current dataset, from which the filter is fired
									calculateDatasets(datasetInvolved, group, whereClause);
								}
							}
						}
					} else {
						Set<String> emptySet = new HashSet<String>();
						for (EdgeGroup edgeGroup : result.getEdgeGroupValues().keySet()) {
							result.getEdgeGroupValues().put(edgeGroup, emptySet);
						}
						return;
					}
				}
			}
		}
	}
}
