/**
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
package it.eng.spagobi.tools.dataset.utils;

import org.json.JSONException;
import org.json.JSONObject;

import it.eng.qbe.dataset.DerivedDataSet;
import it.eng.qbe.dataset.QbeDataSet;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.QuerableBehaviour;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.database.DataBaseException;
import it.eng.spagobi.utilities.database.DataBaseFactory;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 *
 * @author raselako
 * @email radmila.selakovic@eng.it
 *
 */
public class InlineViewUtility {

	public static String getTableName(IDataSet dataSet) throws DataBaseException {
		String tableName = null;
		if (dataSet instanceof VersionedDataSet) {
			dataSet = ((VersionedDataSet) dataSet).getWrappedDataset();

		}

		Assert.assertTrue(dataSet instanceof AbstractJDBCDataset || dataSet instanceof QbeDataSet,
				"Dataset need to be query or qbe dataset. Your dataset is: " + dataSet.getClass());

		String subQueryAlias = DataBaseFactory.getDataBase(getDataSource(dataSet)).getSubQueryAlias();

		if (dataSet instanceof AbstractJDBCDataset) {
			QuerableBehaviour querableBehaviour = (QuerableBehaviour) dataSet.getBehaviour(QuerableBehaviour.class.getName());
			tableName = "(\n" + querableBehaviour.getStatement().replace(";", "") + "\n) " + subQueryAlias;
		} else {
			if (dataSet instanceof DerivedDataSet) {
				String sqlQuery;
				DerivedDataSet derivedDataSet = (DerivedDataSet) dataSet;
				JSONObject sourceJsonConfig;
				try {
					sourceJsonConfig = new JSONObject(derivedDataSet.getConfiguration());
//					if (sourceJsonConfig.has("sourceDatasetLabel") && StringUtils.isNotEmpty(sourceJsonConfig.getString("sourceDatasetLabel"))) {
//						IDataSet sourceDataset = DAOFactory.getDataSetDAO().loadDataSetByLabel(sourceJsonConfig.getString("sourceDatasetLabel"));
//						derivedDataSet.setSourceDataset(sourceDataset);
//
//					}
//					if (StringUtils.isEmpty(derivedDataSet.getJsonQuery())) {
//						String jsonQuery = sourceJsonConfig.getString("jsonQuery");
//						derivedDataSet.setJsonQuery(jsonQuery);
//
//					}
					sqlQuery = sourceJsonConfig.getString("sqlQuery");
				} catch (JSONException e) {
					throw new SpagoBIRuntimeException("sourceJsonConfig no longer exists for " + derivedDataSet.getLabel() + " Dataset");
				}

				tableName = sqlQuery;
				tableName = "(\n" + tableName + "\n) " + subQueryAlias;
			} else {
				QbeDataSet qbeDataSet = (QbeDataSet) dataSet;
				tableName = qbeDataSet.getStatement().getSqlQueryString();
				tableName = "(\n" + tableName + "\n) " + subQueryAlias;
			}
		}

		return tableName;
	}

	public static IDataSource getDataSource(IDataSet dataset) {
		if (dataset instanceof VersionedDataSet) {
			VersionedDataSet vds = (VersionedDataSet) dataset;
			if (vds.getWrappedDataset() instanceof DerivedDataSet) {
				JSONObject sourceJsonConfig;
				try {
					sourceJsonConfig = new JSONObject(vds.getWrappedDataset().getConfiguration());
					String datasourceLabel = sourceJsonConfig.getString("sourceDataSource");
					return DAOFactory.getDataSourceDAO().findDataSourceByLabel(datasourceLabel);
				} catch (JSONException e) {
					throw new SpagoBIRuntimeException("sourceJsonConfig no longer exists for " + vds.getWrappedDataset().getLabel() + " Dataset");
				}

			}
		}
		return dataset.getDataSource();
	}
}