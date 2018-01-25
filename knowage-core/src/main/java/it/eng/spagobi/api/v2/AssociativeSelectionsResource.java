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
package it.eng.spagobi.api.v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.jgrapht.graph.Pseudograph;
import org.json.JSONArray;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spagobi.api.common.AbstractDataSetResource;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.ConfigurationConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.dataset.associativity.IAssociativityManager;
import it.eng.spagobi.tools.dataset.associativity.strategy.AssociativeStrategyFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.query.item.InFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.Projection;
import it.eng.spagobi.tools.dataset.cache.query.item.SimpleFilter;
import it.eng.spagobi.tools.dataset.common.association.Association;
import it.eng.spagobi.tools.dataset.common.association.Association.Field;
import it.eng.spagobi.tools.dataset.common.association.AssociationGroup;
import it.eng.spagobi.tools.dataset.common.association.AssociationGroupJSONSerializer;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.graph.AssociationAnalyzer;
import it.eng.spagobi.tools.dataset.graph.LabeledEdge;
import it.eng.spagobi.tools.dataset.graph.Tuple;
import it.eng.spagobi.tools.dataset.graph.associativity.Config;
import it.eng.spagobi.tools.dataset.graph.associativity.utils.AssociativeLogicUtils;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceParameterException;
import it.eng.spagobi.utilities.sql.SqlUtils;

@Path("/2.0/associativeSelections")
public class AssociativeSelectionsResource extends AbstractDataSetResource {

	static protected Logger logger = Logger.getLogger(AssociativeSelectionsResource.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getAssociativeSelections(@QueryParam("associationGroup") String associationGroupString, @QueryParam("selections") String selectionsString,
			@QueryParam("datasets") String datasetsString, @QueryParam("nearRealtime") String nearRealtimeDatasetsString) {
		logger.debug("IN");

		Monitor start = MonitorFactory.start("Knowage.AssociativeSelectionsResource.getAssociativeSelections:total");

		try {
			IDataSetDAO dataSetDAO = getDataSetDAO();
			dataSetDAO.setUserProfile(getUserProfile());

			// parse selections
			if (selectionsString == null || selectionsString.isEmpty()) {
				throw new SpagoBIServiceParameterException(this.request.getPathInfo(), "Query parameter [selections] cannot be null or empty");
			}

			JSONObject selectionsObject = new JSONObject(selectionsString);

			// parse association group
			if (associationGroupString == null) {
				throw new SpagoBIServiceParameterException(this.request.getPathInfo(), "Query parameter [associationGroup] cannot be null");
			}

			AssociationGroupJSONSerializer serializer = new AssociationGroupJSONSerializer();

			JSONObject associationGroupObject = new JSONObject(associationGroupString);
			AssociationGroup associationGroup = serializer.deserialize(associationGroupObject);
			fixAssociationGroup(associationGroup);

			// parse documents
			Set<String> documents = new HashSet<>();
			JSONArray associations = associationGroupObject.optJSONArray("associations");
			if (associations != null) {
				for (int associationIndex = 0; associationIndex < associations.length(); associationIndex++) {
					JSONObject association = associations.getJSONObject(associationIndex);
					JSONArray fields = association.optJSONArray("fields");
					if (fields != null) {
						for (int fieldIndex = fields.length() - 1; fieldIndex >= 0; fieldIndex--) {
							JSONObject field = fields.getJSONObject(fieldIndex);
							String type = field.optString("type");
							if ("document".equalsIgnoreCase(type)) {
								String store = field.optString("store");
								documents.add(store);
							}
						}
					}
				}
			}

			// parse dataset parameters
			Map<String, Map<String, String>> datasetParameters = new HashMap<>();
			if (datasetsString != null && !datasetsString.isEmpty()) {
				JSONObject datasetsObject = new JSONObject(datasetsString);
				Iterator<String> datasetsIterator = datasetsObject.keys();
				while (datasetsIterator.hasNext()) {
					String datasetLabel = datasetsIterator.next();

					Map<String, String> parameters = new HashMap<>();
					datasetParameters.put(datasetLabel, parameters);

					JSONObject datasetObject = datasetsObject.getJSONObject(datasetLabel);
					Iterator<String> datasetIterator = datasetObject.keys();
					while (datasetIterator.hasNext()) {
						String param = datasetIterator.next();
						String value = datasetObject.getString(param);
						parameters.put(param, value);
					}
				}
			}

			// parse near realtime datasets
			Set<String> nearRealtimeDatasets = new HashSet<>();
			if (nearRealtimeDatasetsString != null && !nearRealtimeDatasetsString.isEmpty()) {
				JSONArray jsonArray = new JSONArray(nearRealtimeDatasetsString);
				for (int i = 0; i < jsonArray.length(); i++) {
					nearRealtimeDatasets.add(jsonArray.getString(i));
				}
			}

			AssociationAnalyzer analyzer = new AssociationAnalyzer(associationGroup.getAssociations());
			analyzer.process();
			Map<String, Map<String, String>> datasetToAssociationToColumnMap = analyzer.getDatasetToAssociationToColumnMap();
			Pseudograph<String, LabeledEdge<String>> graph = analyzer.getGraph();

			// get datasets from selections
			List<SimpleFilter> filters = new ArrayList<>();
			Map<String, Map<String, Set<Tuple>>> selectionsMap = new HashMap<>();

			Iterator<String> it = selectionsObject.keys();
			while (it.hasNext()) {
				String datasetDotColumn = it.next();
				Assert.assertTrue(datasetDotColumn.indexOf(".") >= 0, "Data not compliant with format <DATASET_LABEL>.<COLUMN> [" + datasetDotColumn + "]");
				String[] tmpDatasetAndColumn = datasetDotColumn.split("\\.");
				Assert.assertTrue(tmpDatasetAndColumn.length == 2, "Impossible to get both dataset label and column");

				String datasetLabel = tmpDatasetAndColumn[0];
				String column = SqlUtils.unQuote(tmpDatasetAndColumn[1]);

				Assert.assertNotNull(datasetLabel, "A dataset label in selections is null");
				Assert.assertTrue(!datasetLabel.isEmpty(), "A dataset label in selections is empty");
				Assert.assertNotNull(column, "A column for dataset [" + datasetLabel + "]  in selections is null");
				Assert.assertTrue(!column.isEmpty(), "A column for dataset [" + datasetLabel + "] in selections is empty");

				IDataSet dataSet = getDataSetDAO().loadDataSetByLabel(datasetLabel);

				Projection projection = new Projection(dataSet, column);
				List<Object> valueObjects = new ArrayList<>();

				Object object = selectionsObject.getJSONArray(datasetDotColumn).get(0);
				if (object instanceof JSONArray) {

					JSONArray jsonArray = (JSONArray) object;
					for (int i = 0; i < jsonArray.length(); i++) {
						Object valueForQuery = DataSetUtilities.getValue(jsonArray.get(i).toString(), projection.getType());
						valueObjects.add(valueForQuery);
					}
				} else {
					Object valueForQuery = DataSetUtilities.getValue(object.toString(), projection.getType());
					valueObjects.add(valueForQuery);
				}

				filters.add(new InFilter(projection, valueObjects));

				if (!selectionsMap.containsKey(datasetLabel)) {
					selectionsMap.put(datasetLabel, new HashMap<String, Set<Tuple>>());
				}
				Map<String, Set<Tuple>> selection = selectionsMap.get(datasetLabel);
				if (!selection.containsKey(column)) {
					selection.put(column, new HashSet<Tuple>());
				}
				Set<Tuple> tupleSet = selection.get(column);
				for (Object value : valueObjects) {
					Tuple tuple = new Tuple();
					tuple.add(value);
					tupleSet.add(tuple);
				}
			}

			logger.debug("Filter list: " + filters);

			String strategy = SingletonConfig.getInstance().getConfigValue(ConfigurationConstants.SPAGOBI_DATASET_ASSOCIATIVE_LOGIC_STRATEGY);
			Config config = AssociativeLogicUtils.buildConfig(strategy, graph, datasetToAssociationToColumnMap, filters, nearRealtimeDatasets,
					datasetParameters, documents);

			IAssociativityManager manager = AssociativeStrategyFactory.createStrategyInstance(config, getUserProfile());
			manager.process();

			// Map<String, Map<String, Set<Tuple>>> selections = AssociationAnalyzer.getSelections(associationGroup, graph, result);
			Map<String, Map<String, Set<Tuple>>> selections = manager.getSelections();

			for (String d : selectionsMap.keySet()) {
				if (!selections.containsKey(d)) {
					selections.put(d, new HashMap<String, Set<Tuple>>());
				}
				Map<String, Set<Tuple>> calcSelections = selections.get(d);
				Map<String, Set<Tuple>> inputSelections = selectionsMap.get(d);
				for (String selectionKey : inputSelections.keySet()) {
					if (calcSelections.containsKey(selectionKey)) { // intersect tuples
						Set<Tuple> oldSelectionTuples = calcSelections.get(selectionKey);
						Set<Tuple> newSelectionTuples = inputSelections.get(selectionKey);
						newSelectionTuples.retainAll(oldSelectionTuples);
						calcSelections.put(selectionKey, newSelectionTuples);
					} else { // add tuples
						calcSelections.put(selectionKey, inputSelections.get(selectionKey));
					}
				}
			}

			String stringFeed = JsonConverter.objectToJson(selections, Map.class);
			return stringFeed;
		} catch (Exception e) {
			String errorMessage = "An error occurred while getting associative selections";
			logger.error(errorMessage, e);
			throw new SpagoBIRestServiceException(errorMessage, buildLocaleFromSession(), e);
		} finally {
			start.stop();
			logger.debug("OUT");
		}
	}

	private void fixAssociationGroup(AssociationGroup associationGroup) {
		IDataSetDAO dataSetDAO = getDataSetDAO();

		Map<String, IMetaData> dataSetLabelToMedaData = new HashMap<>();
		for (Association association : associationGroup.getAssociations()) {
			if (association.getDescription().contains(".")) {
				for (Field field : association.getFields()) {
					String fieldName = field.getFieldName();
					if (fieldName.contains(":")) {
						String dataSetLabel = field.getDataSetLabel();

						IMetaData metadata = null;
						if (dataSetLabelToMedaData.containsKey(dataSetLabel)) {
							metadata = dataSetLabelToMedaData.get(dataSetLabel);
						} else {
							metadata = dataSetDAO.loadDataSetByLabel(dataSetLabel).getMetadata();
							dataSetLabelToMedaData.put(dataSetLabel, metadata);
						}

						for (int i = 0; i < metadata.getFieldCount(); i++) {
							IFieldMetaData fieldMeta = metadata.getFieldMeta(i);
							String alias = fieldMeta.getAlias();
							if (fieldMeta.getName().equals(fieldName)) {
								association.setDescription(association.getDescription().replace(dataSetLabel + "." + fieldName, dataSetLabel + "." + alias));
								field.setFieldName(alias);
								break;
							}
						}
					}
				}
			}
		}
	}

}
