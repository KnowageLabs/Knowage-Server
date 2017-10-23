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
package it.eng.knowage.engines.svgviewer.datamart.provider;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;
import it.eng.knowage.engines.svgviewer.SvgViewerEngineRuntimeException;
import it.eng.knowage.engines.svgviewer.datamart.provider.configurator.DataMartProviderConfigurator;
import it.eng.knowage.engines.svgviewer.dataset.DataMart;
import it.eng.knowage.engines.svgviewer.dataset.DataSetMetaData;
import it.eng.knowage.engines.svgviewer.dataset.HierarchyMember;
import it.eng.knowage.engines.svgviewer.map.renderer.Layer;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.QuerableBehaviour;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.EngineConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * This class wrap an object of type IDataSet
 *
 */
public class DataMartProvider extends AbstractDataMartProvider {

	/** The data source. */
	private IDataSource dataSource;

	/** The query. */
	private String query;

	private IDataSet ds;

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(DataMartProvider.class);

	public static final String QUERY = "query";

	public DataMartProvider() {
		super();
	}

	@Override
	public void init(Object conf) throws SvgViewerEngineException {
		super.init(conf);
		DataMartProviderConfigurator.configure(this, getConf());
	}

	@Override
	public DataMart getDataMart() throws SvgViewerEngineRuntimeException {

		DataMart dataMart = new DataMart();
		IDataSet dataSet;

		dataSet = (IDataSet) getEnv().get(EngineConstants.ENV_DATASET);
		if (dataSet == null) {
			logger.error("Dataset isn't present into the env object. Check the existence of the template's datasets.");
			throw new SvgViewerEngineRuntimeException(
					"Dataset wasn't loaded into the environment. Please check that datasets specified into the template are existing into the system.");
		}

		Map dataSetPars = getEnv();
		// adds parent as parameter for substitute placeholder $P{xxx} into dataset. The xxx is the attribute placeholder_dataaset got from the template
		if (getSelectedParentName() != null && !getSelectedParentName().equals("")) {
			HierarchyMember activeMember = getHierarchyMember(getSelectedMemberName());
			if (activeMember.getDsPlaceholder() != null && !activeMember.getDsPlaceholder().equals(""))
				logger.debug("Added parameter used from dataset [$P{" + activeMember.getDsPlaceholder() + "}] value [" + getSelectedParentName() + "]");
			dataSetPars.put(activeMember.getDsPlaceholder(), getSelectedParentName());
		}

		dataSet.setParamsMap(getEnv());

		if (dataSet.hasBehaviour(QuerableBehaviour.class.getName())) {
			QuerableBehaviour querableBehaviour = (QuerableBehaviour) dataSet.getBehaviour(QuerableBehaviour.class.getName());
			// querableBehaviour.setQueryTransformer( getDrillQueryTransformer() );
			querableBehaviour.setQueryTransformer(new DrillThroughQueryTransformer(this));
		}

		try {
			logger.warn("trying to load data...");
			Monitor loadDataMonitor = MonitorFactory.start("GeoEngine.DataMartProvider.getDataMart.loadData");
			dataSet.loadData();
			loadDataMonitor.stop();
			logger.warn("success!!");
		} catch (Throwable e) {
			logger.error("failure!!", e);
			throw new SvgViewerEngineRuntimeException("Impossible to load data from dataset", e);
		}

		IDataStore dataStore = dataSet.getDataStore();
		IMetaData dataStoreMeta = dataStore.getMetaData();

		dataMart.setDataStore(dataStore);
		try {
			HierarchyMember activeMember = getHierarchyMember(getSelectedMemberName());
			DataSetMetaData metaData = activeMember.getDsMetaData();
			String firstLayerName = null;
			List<String> lstLayers = new ArrayList();
			for (String key : activeMember.getLayers().keySet()) {
				Layer layer = activeMember.getLayers().get(key);
				if (layer.isSelected()) {
					lstLayers.add(layer.getName());
					logger.debug("Set active layer [" + layer.getName() + "]");
					if (firstLayerName == null) {
						firstLayerName = layer.getName();
					}
				}
			}
			dataMart.setTargetFeatureName(lstLayers);
			// if no layer setted through the template property, sets the first one
			if (dataMart.getTargetFeatureName() == null) {

				dataMart.setTargetFeatureName(lstLayers);
				logger.debug("No layer has property selected to true. So, set as active layer the first one: [" + firstLayerName + "]");
			}

			String columnId = metaData.getGeoIdColumnName();
			String visibilityColumnId = metaData.getVisibilityColumnName();
			String crossTypeColumnId = metaData.getCrossTypeColumnName();
			String labelsColumnId = metaData.getLabelsColumnName();
			String drillColumnId = metaData.getDrillColumnName();
			String parentColumnId = metaData.getParentColumnName();
			String selectedParent = getSelectedParentName();
			String tooltipColumnId = metaData.getTooltipColumnName();
			String infoColumnId = metaData.getInfoColumnName();

			dataStoreMeta.setIdField(dataStoreMeta.getFieldIndex(columnId));
			String[] measureColumnNames = (String[]) metaData.getMeasureColumnNames().toArray(new String[0]);

			Iterator it = dataStore.iterator();
			while (it.hasNext()) {
				IRecord record = (IRecord) it.next();

				// check if parentid is specified to filter the datastore
				if (parentColumnId != null && selectedParent != null) {
					IField parentColumnField = record.getFieldAt(dataStoreMeta.getFieldIndex(parentColumnId));
					String parentColumnFieldValue = (String) parentColumnField.getValue();
					if (!selectedParent.equals(parentColumnFieldValue)) {
						// skip this record because is filtered out
						it.remove();
						continue;
					}
				}

				IField field;
				try {
					field = record.getFieldAt(dataStoreMeta.getFieldIndex(columnId));
				} catch (Exception ex) {
					logger.error("An error occured while getting the columnId [" + columnId + "] from the dataset. Check the query  and the template.");
					throw new SvgViewerEngineRuntimeException("An error occured while getting the GEO columnId [" + columnId
							+ "] from the dataset. Check the query and the template. ", ex);
				}
				String id = "" + field.getValue();
				if ((id == null) || (id.trim().equals(""))) {
					continue;
				}
				dataStoreMeta.getFieldMeta(dataStoreMeta.getFieldIndex(columnId)).setProperty("ROLE", "GEOID");

				for (int i = 0; i < measureColumnNames.length; i++) {
					try {
						field = record.getFieldAt(dataStoreMeta.getFieldIndex(measureColumnNames[i]));
					} catch (Exception ex) {
						logger.error("An error occured while getting the columnId [" + measureColumnNames[i]
								+ "] from the dataset. Check the query  and the template.");
						throw new SvgViewerEngineRuntimeException("An error occured while getting the MEASURE columnId [" + measureColumnNames[i]
								+ "] from the dataset. Check the query and the template. ", ex);
					}
					String value = "" + field.getValue();
					if ((value == null) || (value.trim().equals(""))) {
						continue;
					}
					dataStoreMeta.getFieldMeta(dataStoreMeta.getFieldIndex(measureColumnNames[i])).setProperty("ROLE", "MEASURE");

				}

				IField visibilityField;
				if (visibilityColumnId != null) {
					try {
						visibilityField = record.getFieldAt(dataStoreMeta.getFieldIndex(visibilityColumnId));
					} catch (Exception ex) {
						logger.error("An error occured while getting the columnId [" + visibilityColumnId
								+ "] from the dataset. Check the query  and the template.");
						throw new SvgViewerEngineRuntimeException("An error occured while getting the VISIBILITY columnId [" + visibilityColumnId
								+ "] from the dataset. Check the query and the template. ", ex);
					}
					String value = "" + visibilityField.getValue();
					if (value != null && !value.trim().equals("")) {
						dataStoreMeta.getFieldMeta(dataStoreMeta.getFieldIndex(visibilityColumnId)).setProperty("ROLE", "VISIBILITY");
					}
				}

				IField crossableField;
				if (crossTypeColumnId != null) {
					try {
						crossableField = record.getFieldAt(dataStoreMeta.getFieldIndex(crossTypeColumnId));
					} catch (Exception ex) {
						logger.error("An error occured while getting the columnId [" + crossTypeColumnId
								+ "] from the dataset. Check the query  and the template.");
						throw new SvgViewerEngineRuntimeException("An error occured while getting the VISIBILITY columnId [" + crossTypeColumnId
								+ "] from the dataset. Check the query and the template. ", ex);
					}
					String value = "" + crossableField.getValue();
					if (value != null && !value.trim().equals("")) {
						dataStoreMeta.getFieldMeta(dataStoreMeta.getFieldIndex(crossTypeColumnId)).setProperty("ROLE", "CROSSTYPE");
					}
				}

				IField tooltipField;
				if (tooltipColumnId != null) {
					try {
						tooltipField = record.getFieldAt(dataStoreMeta.getFieldIndex(tooltipColumnId));
					} catch (Exception ex) {
						logger.error("An error occured while getting the columnId [" + tooltipColumnId
								+ "] from the dataset. Check the query  and the template.");
						throw new SvgViewerEngineRuntimeException("An error occured while getting the TOOLTIP columnId [" + tooltipColumnId
								+ "] from the dataset. Check the query and the template. ", ex);
					}
					String value = "" + tooltipField.getValue();
					if (value != null && !value.trim().equals("")) {
						dataStoreMeta.getFieldMeta(dataStoreMeta.getFieldIndex(tooltipColumnId)).setProperty("ROLE", "TOOLTIP");
					}

				}

				if (labelsColumnId != null) {
					IField labelsField;
					try {
						labelsField = record.getFieldAt(dataStoreMeta.getFieldIndex(labelsColumnId));
					} catch (Exception ex) {
						logger.error("An error occured while getting the columnId [" + labelsColumnId
								+ "] from the dataset. Check the query  and the template.");
						throw new SvgViewerEngineRuntimeException("An error occured while getting the LABEL columnId [" + labelsColumnId
								+ "] from the dataset. Check the query and the template. ", ex);
					}
					String value = "" + labelsField.getValue();
					if (value != null && !value.trim().equals("")) {
						dataStoreMeta.getFieldMeta(dataStoreMeta.getFieldIndex(labelsColumnId)).setProperty("ROLE", "LABEL");
					}

				}

				if (drillColumnId != null) {
					IField drillIdField;
					try {
						drillIdField = record.getFieldAt(dataStoreMeta.getFieldIndex(drillColumnId));
					} catch (Exception ex) {
						logger.error("An error occured while getting the columnId [" + drillColumnId + "] from the dataset. Check the query  and the template.");
						throw new SvgViewerEngineRuntimeException("An error occured while getting the DRILL columnId [" + drillColumnId
								+ "] from the dataset. Check the query and the template. ", ex);
					}
					String value = "" + drillIdField.getValue();
					if (value != null && !value.trim().equals("")) {
						dataStoreMeta.getFieldMeta(dataStoreMeta.getFieldIndex(drillColumnId)).setProperty("ROLE", "DRILLID");
					}

				}

				IField infoField;
				if (infoColumnId != null) {
					try {
						infoField = record.getFieldAt(dataStoreMeta.getFieldIndex(infoColumnId));
					} catch (Exception ex) {
						logger.error("An error occured while getting the columnId [" + infoColumnId + "] from the dataset. Check the query  and the template.");
						throw new SvgViewerEngineRuntimeException("An error occured while getting the INFO columnId [" + infoColumnId
								+ "] from the dataset. Check the query and the template. ", ex);
					}
					String value = "" + infoField.getValue();
					if (value != null && !value.trim().equals("")) {
						dataStoreMeta.getFieldMeta(dataStoreMeta.getFieldIndex(infoColumnId)).setProperty("ROLE", "INFO");
					}

				}

			}
			// IDataStoreTransformer dddLinkFieldTransformer = new AddLinkFieldsTransformer(measureColumnNames, getSelectedLevel(), this.getEnv());
			// dddLinkFieldTransformer.transform(dataStore);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new SvgViewerEngineRuntimeException("Impossible to get DataMart. ", e);
		}
		// }

		return dataMart;
	}

	/**
	 * Gets the executable query.
	 *
	 * @return the executable query
	 */
	public String getExecutableQuery() {
		String executableQuery = null;
		IDataSet dataSet;

		dataSet = (IDataSet) getEnv().get(EngineConstants.ENV_DATASET);

		if (dataSet != null) {
			try {
				JSONObject jsonConf = ObjectUtils.toJSONObject(ds.getConfiguration());
				// executableQuery = (String)dataSet.getQuery();
				executableQuery = jsonConf.getString(QUERY);
			} catch (Exception e) {
				logger.error("Error while getting query configuration.  Error: " + e.getMessage());
			}
		} else {
			executableQuery = query;
		}

		if (executableQuery.indexOf("${") == -1)
			return executableQuery;
		while (executableQuery.indexOf("${") != -1) {
			int startInd = executableQuery.indexOf("${") + 2;
			int endInd = executableQuery.indexOf("}", startInd);
			String paramName = executableQuery.substring(startInd, endInd);
			String paramValue = null;
			if (getEnv().containsKey(paramName)) {
				paramValue = getEnv().get(paramName).toString();
			}
			if (paramValue == null) {
				logger.error("Cannot find in service request a valid value for parameter: parameter name " + paramName);

				paramValue = "";
			}
			executableQuery = executableQuery.substring(0, startInd - 2) + paramValue + executableQuery.substring(endInd + 1);
		}
		return executableQuery;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.dataset.provider.AbstractDatasetProvider#getDataDetails(java.lang.String)
	 */
	@Override
	public SourceBean getDataDetails(String featureValue) {
		return null;
	}

	/**
	 * Gets the data source.
	 *
	 * @return the data source
	 */
	protected IDataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Sets the data source.
	 *
	 * @param dataSource
	 *            the new data source
	 */
	public void setDataSource(IDataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Gets the query.
	 *
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * Sets the query.
	 *
	 * @param query
	 *            the new query
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	public IDataSet getDs() {
		return ds;
	}

	public void setDs(IDataSet ds) {
		this.ds = ds;
	}
}
