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

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;

import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.graph.EdgeGroup;
import it.eng.spagobi.tools.dataset.graph.Tuple;
import it.eng.spagobi.tools.dataset.graph.associativity.exceptions.IllegalEdgeGroupException;
import it.eng.spagobi.tools.dataset.graph.associativity.utils.AssociativeLogicUtils;
import it.eng.spagobi.tools.dataset.metasql.query.item.InFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.MultipleProjectionSimpleFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.tools.dataset.metasql.query.item.SimpleFilter;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.utilities.database.DataBaseException;
import it.eng.spagobi.utilities.parameters.ParametersUtilities;

public abstract class AssociativeDatasetContainer implements IAssociativeDatasetContainer {

	private static final Logger logger = Logger.getLogger(AssociativeDatasetContainer.class);
	protected final Set<SimpleFilter> filters = new HashSet<>();
	protected final Set<EdgeGroup> groups = new HashSet<>();
	protected final Set<EdgeGroup> usedGroups = new HashSet<>();
	protected IDataSet dataSet;
	protected Map<String, String> parameters;
	protected boolean resolved = false;

	public AssociativeDatasetContainer(IDataSet dataSet, Map<String, String> parameters) {
		this.dataSet = dataSet;
		this.parameters = parameters;
	}

	@Override
	public IDataSet getDataSet() {
		return dataSet;
	}

	@Override
	public boolean addFilter(SimpleFilter filter) {
		return filters.add(filter);
	}

	private boolean addFilter(List<String> columnNames, Set<Tuple> tuples) {
		return filters.add(buildInFilter(columnNames, tuples));
	}

	@Override
	public boolean update(EdgeGroup group, List<String> columnNames, Set<Tuple> tuples) {
		if (columnNames.isEmpty()) {
			return false;
		} else {
			usedGroups.add(group);
			if (!ParametersUtilities.containsParameter(columnNames)) {
				return addFilter(columnNames, tuples);
			} else {
				if (columnNames.size() == 1) {
					String parameter = columnNames.get(0);
					Set<String> values = new HashSet<>(tuples.size());
					for (Tuple tuple : tuples) {
						values.add(tuple.toStringEncoding("", "", ""));
					}

					parameters.put(ParametersUtilities.getParameterName(parameter), StringUtils.join(values, ","));
//					new DatasetManagementAPI().setDataSetParameter(dataSet, ParametersUtilities.getParameterName(parameter),StringUtils.join(values, ","));
					new DatasetManagementAPI().setDataSetParameters(dataSet, parameters, ParametersUtilities.getParameterName(parameter));
					return true;
				} else {
					throw new IllegalEdgeGroupException("Columns " + columnNames
							+ " contain at least one parameter and more than one association. \nThis is a illegal state for an associative group.");
				}
			}
		}
	}

	@Override
	public Set<EdgeGroup> getGroups() {
		return groups;
	}

	@Override
	public Set<EdgeGroup> getUsedGroups() {
		return usedGroups;
	}

	@Override
	public boolean removeGroup(EdgeGroup group) {
		return groups.remove(group);
	}

	@Override
	public boolean addGroup(EdgeGroup group) {
		return groups.add(group);
	}

	@Override
	public Set<EdgeGroup> getUnresolvedGroups() {
		Set<EdgeGroup> unresolvedGroups = new HashSet<>();
		for (EdgeGroup group : groups) {
			if (!group.isResolved()) {
				unresolvedGroups.add(group);
			}
		}
		return unresolvedGroups;
	}

	@Override
	public boolean isResolved() {
		return resolved;
	}

	@Override
	public void resolve() {
		resolved = true;
	}

	@Override
	public void unresolve() {
		resolved = false;
	}

	@Override
	public void unresolveGroups() {
		for (EdgeGroup group : groups) {
			group.unresolve();
		}
	}

	@Override
	public Map<String, String> getParameters() {
		return parameters;
	}

	@Override
	public abstract Set<Tuple> getTupleOfValues(List<String> columnNames)
			throws ClassNotFoundException, NamingException, SQLException, DataBaseException, IOException, SolrServerException;

	@Override
	public Set<Tuple> getTupleOfValues(String parameter) {
		return AssociativeLogicUtils.getTupleOfValues(parameters.get(ParametersUtilities.getParameterName(parameter)));
	}

	protected MultipleProjectionSimpleFilter buildInFilter(List<String> columnNames, Set<Tuple> tuples) {
		int columnCount = columnNames.size();
		List<Projection> projections = new ArrayList<Projection>(columnCount);
		List<IFieldMetaData> metaData = new ArrayList<IFieldMetaData>(columnCount);
		for (String columnName : columnNames) {
			projections.add(new Projection(dataSet, columnName));
			metaData.add(DataSetUtilities.getFieldMetaData(dataSet, columnName));
		}

		List<Object> values = new ArrayList<Object>();
		for (Tuple tuple : tuples) {
			values.addAll(tuple.getValues());
		}

		return new InFilter(projections, values);
	}
}
