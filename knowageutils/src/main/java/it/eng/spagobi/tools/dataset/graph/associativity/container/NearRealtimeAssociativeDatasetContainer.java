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

package it.eng.spagobi.tools.dataset.graph.associativity.container;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.log4j.Logger;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.graph.Tuple;
import it.eng.spagobi.tools.dataset.graph.associativity.utils.AssociativeLogicUtils;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 *
 */

public class NearRealtimeAssociativeDatasetContainer extends AssociativeDatasetContainer {

	static protected Logger logger = Logger.getLogger(NearRealtimeAssociativeDatasetContainer.class);

	public NearRealtimeAssociativeDatasetContainer(IDataSet dataSet, Map<String, String> parameters) {
		super(dataSet, parameters);
	}

	@Override
	public String getTableName() {
		return DataStore.DEFAULT_SCHEMA_NAME + "." + DataStore.DEFAULT_TABLE_NAME;
	}

	@Override
	public IDataSource getDataSource() {
		/*
		 * Set dataSource to null. By giving a null to AbstractJDBCDataset.encapsulateColumnName it returns an empty string, which is what we want to achieve
		 */
		return null;
	}

	private IDataStore getDataStore() {
		dataSet.loadData();
		return dataSet.getDataStore();
	}

	@Override
	public Set<Tuple> getTupleOfValues(List<String> columnNames) throws ClassNotFoundException, NamingException, SQLException {
		String query = buildQuery(columnNames);
		IDataStore dataStore = getDataStore();
		if (dataStore != null) {
			logger.debug("Executing query with MetaModel: " + query);
			org.apache.metamodel.data.DataSet rs = dataStore.getMetaModelResultSet(query);
			return AssociativeLogicUtils.getTupleOfValues(rs);
		} else {
			throw new SpagoBIRuntimeException(
					"Error while retrieving the DataStore for real-time dataset with label [" + dataSet + "]. It is impossible to get values from it.");
		}
	}

	@Override
	public String encapsulateColumnName(String columnName) {
		return DataStore.DEFAULT_TABLE_NAME + "." + columnName;
	}

	@Override
	public boolean isNearRealtime() {
		return true;
	}

}
