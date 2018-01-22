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

import static it.eng.spagobi.tools.glossary.util.Util.getNumberOrNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.util.JSON;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.api.common.DataSetResourceAbstractResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.cache.FilterCriteria;
import it.eng.spagobi.tools.dataset.cache.Operand;
import it.eng.spagobi.tools.dataset.cache.ProjectionCriteria;
import it.eng.spagobi.tools.dataset.common.datawriter.CockpitJSONDataWriter;
import it.eng.spagobi.tools.dataset.common.datawriter.IDataWriter;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;
import it.eng.spagobi.tools.dataset.dao.DataSetFactory;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.dao.ISbiDataSetDAO;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSetId;
import it.eng.spagobi.tools.dataset.persist.IPersistedManager;
import it.eng.spagobi.tools.dataset.persist.PersistedHDFSManager;
import it.eng.spagobi.tools.dataset.persist.PersistedTableManager;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.StringUtils;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.utilities.sql.SqlUtils;

/**
 * @author Alessandro Daniele (alessandro.daniele@eng.it)
 *
 */
@Path("/2.0/datasets")

@ManageAuthorization
public class DataSetResource extends DataSetResourceAbstractResource {

	static protected Logger logger = Logger.getLogger(DataSetResource.class);

	public String getNotDerivedDataSets(@QueryParam("callback") String callback) {
		logger.debug("IN");

		IDataSetDAO dsDAO = getDataSetDAO();

		List<IDataSet> toBeReturned = dsDAO.loadNotDerivedDataSets(getUserProfile());

		try {
			logger.debug("OUT");
			if (callback == null || callback.isEmpty())

				return ((JSONArray) SerializerFactory.getSerializer("application/json").serialize(toBeReturned, buildLocaleFromSession())).toString();

			else {
				String jsonString = ((JSONArray) SerializerFactory.getSerializer("application/json").serialize(toBeReturned, buildLocaleFromSession()))
						.toString();

				return callback + "(" + jsonString + ")";
			}
		} catch (SerializationException e) {
			throw new SpagoBIRestServiceException(getLocale(), e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getDataSets(@QueryParam("includeDerived") String includeDerived, @QueryParam("callback") String callback,
			@QueryParam("asPagedList") Boolean paged, @QueryParam("Page") String pageStr, @QueryParam("ItemPerPage") String itemPerPageStr,
			@QueryParam("label") String search, @QueryParam("seeTechnical") Boolean seeTechnical, @QueryParam("ids") String ids) {
		logger.debug("IN");

		if ("no".equalsIgnoreCase(includeDerived)) {
			return getNotDerivedDataSets(callback);
		}

		if (Boolean.TRUE.equals(paged)) {
			return getDatasetsAsPagedList(pageStr, itemPerPageStr, search, seeTechnical, ids);
		}

		ISbiDataSetDAO dsDAO;
		try {
			dsDAO = DAOFactory.getSbiDataSetDAO();
		} catch (EMFUserError e) {
			logger.error("Error while looking for datasets", e);
			throw new SpagoBIRuntimeException("Error while looking for datasets", e);
		}

		List<SbiDataSet> dataSets = dsDAO.loadSbiDataSets();
		List<SbiDataSet> toBeReturned = new ArrayList<SbiDataSet>();

		for (SbiDataSet dataset : dataSets) {
			IDataSet iDataSet = DataSetFactory.toDataSet(dataset);
			if (DataSetUtilities.isExecutableByUser(iDataSet, getUserProfile()))
				toBeReturned.add(dataset);
		}

		logger.debug("OUT");
		if (callback == null || callback.isEmpty())
			return JsonConverter.objectToJson(toBeReturned, toBeReturned.getClass());
		else {
			String jsonString = JsonConverter.objectToJson(toBeReturned, toBeReturned.getClass());

			return callback + "(" + jsonString + ")";
		}
	}

	@Override
	@GET
	@Path("/{label}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getDataSet(@PathParam("label") String label) {
		return super.getDataSet(label);
	}

	@Override
	@GET
	@Path("/{label}/content")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Response execute(@PathParam("label") String label, String body) {
		return super.execute(label, body);
	}

	@POST
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Response addDataSet(String body) {
		SbiDataSet sbiDataset = (SbiDataSet) JsonConverter.jsonToValidObject(body, SbiDataSet.class);

		sbiDataset.setId(new SbiDataSetId(null, 1, getUserProfile().getOrganization()));
		sbiDataset.setOwner((String) getUserProfile().getUserUniqueIdentifier());
		IDataSet dataset = DataSetFactory.toDataSet(sbiDataset);

		try {
			DAOFactory.getDataSetDAO().insertDataSet(dataset);

			if (dataset.isPersistedHDFS()) {
				IPersistedManager ptm = new PersistedHDFSManager(getUserProfile());
				ptm.persistDataSet(dataset);
			}

			if (dataset.isPersisted()) {
				IPersistedManager ptm = new PersistedTableManager(getUserProfile());
				ptm.persistDataSet(dataset);
			}

		} catch (Exception e) {
			logger.error("Error while creating the dataset: " + e.getMessage(), e);
			throw new SpagoBIRuntimeException("Error while creating the dataset: " + e.getMessage(), e);
		}

		try {
			return Response.created(new URI("1.0/datasets/" + dataset.getLabel().replace(" ", "%20"))).build();
		} catch (URISyntaxException e) {
			logger.error("Error while creating the resource url, maybe an error in the label", e);
			throw new SpagoBIRuntimeException("Error while creating the resource url, maybe an error in the label", e);
		}
	}

	@PUT
	@Path("/{label}")
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Response modifyDataSet(@PathParam("label") String label, String body) {
		IDataSet dataset = null;

		try {
			dataset = getDatasetManagementAPI().getDataSet(label);
		} catch (Exception e) {
			logger.error("Error while creating the dataset: " + e.getMessage(), e);
			throw new SpagoBIRuntimeException("Error while creating the dataset: " + e.getMessage(), e);
		}

		SbiDataSet sbiDataset = (SbiDataSet) JsonConverter.jsonToValidObject(body, SbiDataSet.class);

		int version = 1;
		if (dataset instanceof VersionedDataSet) {
			version = ((VersionedDataSet) dataset).getVersionNum();
		}

		sbiDataset.setId(new SbiDataSetId(dataset.getId(), version + 1, dataset.getOrganization()));
		sbiDataset.setOwner(dataset.getOwner());
		sbiDataset.setLabel(label);

		IDataSet newDataset = DataSetFactory.toDataSet(sbiDataset);

		try {
			DAOFactory.getDataSetDAO().modifyDataSet(newDataset);
		} catch (Exception e) {
			logger.error("Error while creating the dataset: " + e.getMessage(), e);
			throw new SpagoBIRuntimeException("Error while creating the dataset: " + e.getMessage(), e);
		}

		return Response.ok().build();
	}

	@Override
	@DELETE
	@Path("/{label}")
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Response deleteDataset(@PathParam("label") String label) {
		return super.deleteDataset(label);
	}

	public String getDatasetsAsPagedList(String pageStr, String itemPerPageStr, String search, Boolean seeTechnical, String ids) {

		try {
			ISbiDataSetDAO dao = DAOFactory.getSbiDataSetDAO();
			IEngUserProfile profile = this.getUserProfile();
			// TODO check if profile is null
			dao.setUserProfile(profile);

			Integer page = getNumberOrNull(pageStr);
			Integer item_per_page = getNumberOrNull(itemPerPageStr);
			search = search != null ? search : "";

			Integer[] idArray = getIdsAsIntegers(ids);

			List<SbiDataSet> dataset = null;
			if (UserUtilities.isAdministrator(getUserProfile())) {
				dataset = dao.loadPaginatedSearchSbiDataSet(search, page, item_per_page, null, null, idArray);
			} else {
				dataset = dao.loadPaginatedSearchSbiDataSet(search, page, item_per_page, getUserProfile(), seeTechnical, idArray);
			}

			JSONObject jo = new JSONObject();
			JSONArray ja = new JSONArray();
			for (SbiDataSet ds : dataset) {
				ja.put(JSON.parse(JsonConverter.objectToJson(ds, SbiDataSet.class)));
			}
			jo.put("item", ja);
			jo.put("itemCount", dao.countSbiDataSet(search, idArray));

			return jo.toString();
		} catch (Exception e) {
			logger.error("Error while getting the list of documents", e);
			throw new SpagoBIRuntimeException("Error while getting the list of documents", e);
		} finally {
			logger.debug("OUT");
		}
	}

	@Override
	protected List<FilterCriteria> getFilterCriteria(String datasetLabel, JSONObject selectionsObject, boolean isNearRealtime,
			Map<String, String> columnAliasToColumnName) throws JSONException {
		List<FilterCriteria> filterCriterias = new ArrayList<>();

		if (selectionsObject.has(datasetLabel)) {
			IDataSet dataSet = getDataSetDAO().loadDataSetByLabel(datasetLabel);
			boolean isAnEmptySelection = false;

			JSONObject datasetSelectionObject = selectionsObject.getJSONObject(datasetLabel);
			Iterator<String> it = datasetSelectionObject.keys();
			while (!isAnEmptySelection && it.hasNext()) {
				String columns = it.next();

				// check two case: if click selection contained wil be JSON array, and operator is =, if Json Object is from filter
				Object filterObject = datasetSelectionObject.get(columns);
				String filterOperator = null;
				JSONArray values = null;

				if (filterObject instanceof JSONArray) {
					logger.debug("coming from click");
					// if there are more columns use IN clause cause we are coming from association click
					filterOperator = "IN";
					values = (JSONArray) filterObject;
				} else if (filterObject instanceof JSONObject) {
					logger.debug("coming from filters ");
					JSONObject fiulterJsonObject = (JSONObject) filterObject;
					filterOperator = fiulterJsonObject.opt("filterOperator").toString();
					values = fiulterJsonObject.getJSONArray("filterVals");
				} else {
					throw new SpagoBIRuntimeException("Not recognised filter object " + filterObject, null);
				}

				if (values.length() > 0 || getDatasetManagementAPI().isZeroOperandsOperator(filterOperator)) {
					List<String> columnsList = new ArrayList<>();
					String columnNames = fixColumnAliasesAndNames(columns, columnAliasToColumnName);
					columnsList.addAll(Arrays.asList(columnNames.split("\\s*,\\s*"))); // trim spaces while splitting

					for (int i = 0; i < columnsList.size(); i++) {
						String column = columnsList.get(i);
						if (column.contains(":")) {
							columnsList.set(i, getDatasetManagementAPI().getQbeDataSetColumn(dataSet, column));
						}
					}

					IDataSource dataSource = getDataSource(dataSet, isNearRealtime);

					boolean isJDBCDataSet = DatasetManagementAPI.isJDBCDataSet(dataSet);
					String dialect = dataSource != null ? dataSource.getHibDialectName() : "";
					boolean isBigDataDialect = SqlUtils.isBigDataDialect(dialect);
					boolean isSqlServerOrTeradataDialect = dialect.contains("sqlserver") || dialect.contains("teradata");

					List<String> dateColumnNamesList = getDateColumnNamesListRaw(dataSet, dataSource); // with aliases aposthrophe

					DatasetEvaluationStrategy strategy = getDatasetEvaluationStrategy(dataSet, isNearRealtime);
					if (strategy == DatasetEvaluationStrategy.NEAR_REALTIME) {
						for (int i = 0; i < columnsList.size(); i++) {
							columnsList.set(i, DEFAULT_TABLE_NAME_DOT + AbstractJDBCDataset.encapsulateColumnName(columnsList.get(i), null));
						}
						String joinedColumns = StringUtils.join(columnsList, ",");
						Operand leftOperand = new Operand(joinedColumns);

						StringBuilder valuesSB = new StringBuilder();
						String openingBracket = columnsList.size() > 1 ? "(" : "";
						String closingBracket = columnsList.size() > 1 ? ")" : "";

						List<String> distinctValues = new ArrayList<>();
						for (int i = 0; i < values.length(); i++) {
							String value = values.getString(i);
							distinctValues.addAll(Arrays.asList(getDistinctValues(value)));
						}
						for (int i = 0; i < distinctValues.size(); i++) {
							String value = distinctValues.get(i);
							String column = columnsList.get(i % columnsList.size());
							if (i % columnsList.size() == 0) { // 1st item of tuple of values
								if (i >= columnsList.size()) { // starting from 2nd tuple of values
									valuesSB.append(" OR ");
									valuesSB.append(openingBracket);
								}
							} else {
								valuesSB.append(" AND "); // starting from 2nd item of tuple of values
							}
							if (i > 0) {
								valuesSB.append(column);
								valuesSB.append("=");
							}
							valuesSB.append(getValueForQuery(value, dateColumnNamesList.contains(column), dataSource));
							if (i % columnsList.size() == columnsList.size() - 1) { // last item of tuple of values
								valuesSB.append(closingBracket);
							}
						}
						Operand rightOperand = new Operand(valuesSB.toString());

						FilterCriteria filterCriteria = new FilterCriteria(leftOperand, "=", rightOperand);
						filterCriterias.add(filterCriteria);

					} else if (isSqlServerOrTeradataDialect && filterOperator.equals("IN")) {
						for (int i = 0; i < columnsList.size(); i++) {
							columnsList.set(i, AbstractJDBCDataset.encapsulateColumnName(columnsList.get(i), dataSource));
						}

						String openingBracket = columnsList.size() > 1 ? "(" : "";
						String closingBracket = columnsList.size() > 1 ? ")" : "";

						Operand leftOperand = new Operand(openingBracket + columnsList.get(0));

						StringBuilder valuesSB = new StringBuilder();

						List<String> distinctValues = new ArrayList<>();
						for (int i = 0; i < values.length(); i++) {
							String value = values.getString(i);
							distinctValues.addAll(Arrays.asList(getDistinctValues(value)));
						}

						for (int i = 0; i < distinctValues.size(); i++) {
							String value = distinctValues.get(i);
							String column = columnsList.get(i % columnsList.size());
							if (i % columnsList.size() == 0) { // 1st item of tuple of values
								if (i >= columnsList.size()) { // starting from 2nd tuple of values
									valuesSB.append(" OR ");
									valuesSB.append(openingBracket);
								}
							} else {
								valuesSB.append(" AND "); // starting from 2nd item of tuple of values
							}
							if (i > 0) {
								valuesSB.append(column);
								valuesSB.append("=");
							}
							valuesSB.append(getValueForQuery(value, dateColumnNamesList.contains(column), dataSource));
							if (i % columnsList.size() == columnsList.size() - 1) { // last item of tuple of values
								valuesSB.append(closingBracket);
							}
						}
						Operand rightOperand = new Operand(valuesSB.toString());

						FilterCriteria filterCriteria = new FilterCriteria(leftOperand, "=", rightOperand);
						filterCriterias.add(filterCriteria);

					} else {
						Operand leftOperand = new Operand(StringUtils.join(columnsList, ","));

						List<String> valuesList = new ArrayList<>();
						for (int i = 0; i < values.length(); i++) {
							String[] valuesArray = getDistinctValues(values.getString(i));
							for (int j = 0; j < valuesArray.length; j++) {
								String column = columnsList.get(j % columnsList.size());
								String value = valuesArray[j];
								valuesList.add(getValueForQuery(value, dateColumnNamesList.contains(column), dataSource));
							}
						}

						// case for all operators
						// =, < , >, <= , >= , like ,is null , is not null ,min ,max ,range
						FilterCriteria filterCriteria = null;

						List<String> oneOperandOperators = Arrays.asList("=", "!=", "<", ">", "<=", ">=", "like", "IN");
						List<String> twoOperandOperators = Arrays.asList("range");
						List<String> markupOperandOperators = Arrays.asList("max", "min");
						List<String> zeroOperandOperators = Arrays.asList("is null", "is not null");

						if (filterOperator.equals("IN")) {
							if (!valuesList.isEmpty()) {
								Operand rightOperand = new Operand(valuesList);
								filterCriteria = new FilterCriteria(leftOperand, "IN", rightOperand);
							} else {
								filterCriteria = new FilterCriteria(leftOperand, "IS NULL", null);
							}
						} else if (oneOperandOperators.contains(filterOperator)) {
							// if val not found do not put criteria, it could be already handled by associations
							String val = "''";
							if (valuesList.size() >= 1) {
								val = valuesList.get(0);
							}

							// if operator is like add %%
							if (filterOperator.equals("like") && !val.equals("''")) {
								if (val.startsWith("'") && val.endsWith("'")) {
									val = "'%" + val.substring(1, val.length() - 1) + "%'";
								} else {
									val = "%" + val + "%";
								}
							}

							Operand rightOperand = new Operand(val);
							filterCriteria = new FilterCriteria(leftOperand, filterOperator, rightOperand);
							// } else {
							// logger.warn("No value found for criteria on column " + columnNames + " with operator " + filterOperator);
							// }
						} else if (twoOperandOperators.contains(filterOperator)) {
							Operand rightOperand = null;
							String val1 = "''";
							String val2 = "''";
							if (valuesList.size() >= 2) {

								val1 = valuesList.get(0);
								val2 = valuesList.get(1);
							}
							Object valueToInsert = null;
							if (filterOperator.equalsIgnoreCase("range")) {
								filterOperator = "BETWEEN";
								valueToInsert = " " + val1 + " AND " + val2;
							} else {
								valueToInsert = new ArrayList<String>();
								((List<String>) valueToInsert).add(val1);
								((List<String>) valueToInsert).add(val2);
							}

							rightOperand = new Operand(valueToInsert);

							filterCriteria = new FilterCriteria(leftOperand, filterOperator, rightOperand);
							// } else {
							// logger.warn("No value found for criteria on column " + columnNames + " with operator " + filterOperator);
							// }
						} else if (markupOperandOperators.contains(filterOperator)) {
							Operand rightOperand = new Operand(new ArrayList<String>());
							filterCriteria = new FilterCriteria(leftOperand, filterOperator, rightOperand);
						} else if (zeroOperandOperators.contains(filterOperator)) {
							filterCriteria = new FilterCriteria(leftOperand, filterOperator, null);

						}

						// Operand rightOperand = new Operand(valuesList);
						// FilterCriteria filterCriteria = new FilterCriteria(leftOperand, "IN", rightOperand);
						if (filterCriteria != null) {
							filterCriterias.add(filterCriteria);
						}
					}
				} else {
					isAnEmptySelection = true;
				}

			}

			if (isAnEmptySelection) {
				filterCriterias.clear();
				filterCriterias.add(new FilterCriteria(new Operand("0"), "=", new Operand("1")));
			}
		}

		return filterCriterias;
	}

	private List<String> getDateColumnNamesList(IDataSet dataSet, IDataSource dataSource) {
		List<String> dateColumnNamesList = new ArrayList<>();
		for (int i = 0; i < dataSet.getMetadata().getFieldCount(); i++) {
			IFieldMetaData fieldMeta = dataSet.getMetadata().getFieldMeta(i);
			if (Date.class.isAssignableFrom(fieldMeta.getType())) {
				dateColumnNamesList.add(AbstractJDBCDataset.encapsulateColumnName(fieldMeta.getName(), dataSource));
			}
		}
		return dateColumnNamesList;
	}

	private List<String> getDateColumnNamesListRaw(IDataSet dataSet, IDataSource dataSource) {
		List<String> dateColumnNamesList = new ArrayList<>();
		for (int i = 0; i < dataSet.getMetadata().getFieldCount(); i++) {
			IFieldMetaData fieldMeta = dataSet.getMetadata().getFieldMeta(i);
			if (Date.class.isAssignableFrom(fieldMeta.getType())) {
				dateColumnNamesList.add(fieldMeta.getName());
			}
		}
		return dateColumnNamesList;
	}

	@Override
	protected List<FilterCriteria> getLikeFilterCriteria(String datasetLabel, JSONObject likeSelectionsObject, boolean isNearRealtime,
			Map<String, String> columnAliasToColumnName, List<ProjectionCriteria> projectionCriteria, boolean getAttributes) throws JSONException {
		List<FilterCriteria> likeFilterCriteria = new ArrayList<>();

		if (likeSelectionsObject.has(datasetLabel)) {
			IDataSet dataSet = getDataSetDAO().loadDataSetByLabel(datasetLabel);
			boolean isAnEmptySelection = false;

			JSONObject datasetSelectionObject = likeSelectionsObject.getJSONObject(datasetLabel);
			Iterator<String> it = datasetSelectionObject.keys();
			while (!isAnEmptySelection && it.hasNext()) {
				String columns = it.next();
				String value = datasetSelectionObject.getString(columns);

				if (value != null && !value.isEmpty()) {
					List<String> columnsList = new ArrayList<>();
					String columnNames = fixColumnAliasesAndNames(columns, columnAliasToColumnName);
					columnsList.addAll(Arrays.asList(columnNames.split("\\s*,\\s*"))); // trim spaces while splitting

					List<String> attributesOrMeasures = getAttributesOrMeasures(columnsList, dataSet, projectionCriteria, isNearRealtime, getAttributes);
					if (!attributesOrMeasures.isEmpty()) {
						Operand leftOperand = null;
						StringBuilder rightOperandSB = new StringBuilder();
						for (String attributeOrMeasure : attributesOrMeasures) {
							if (leftOperand == null) {
								leftOperand = new Operand(attributeOrMeasure);

								rightOperandSB.append("'%");
								rightOperandSB.append(value);
								rightOperandSB.append("%'");
							} else {
								rightOperandSB.append(" OR ");
								rightOperandSB.append(attributeOrMeasure);
								rightOperandSB.append(" LIKE '%");
								rightOperandSB.append(value);
								rightOperandSB.append("%'");
							}
						}
						Operand rightOperand = new Operand(rightOperandSB.toString());
						FilterCriteria filterCriteria = new FilterCriteria(leftOperand, "LIKE", rightOperand);
						likeFilterCriteria.add(filterCriteria);
					}
				} else {
					isAnEmptySelection = true;
				}
			}

			if (isAnEmptySelection) {
				likeFilterCriteria.clear();
				likeFilterCriteria.add(new FilterCriteria(new Operand("0"), "=", new Operand("1")));
			}
		}

		return likeFilterCriteria;
	}

	private List<String> getAttributesOrMeasures(List<String> columnNames, IDataSet dataSet, List<ProjectionCriteria> projectionCriteria,
			boolean isNearRealtime, boolean getAttributes) {
		List<String> attributesOrMeasures = new ArrayList<>();

		boolean isJDBCDataSet = DatasetManagementAPI.isJDBCDataSet(dataSet);
		boolean isBigDataDialect = SqlUtils.isBigDataDialect(dataSet.getDataSource() != null ? dataSet.getDataSource().getHibDialectName() : "");

		String defaultTableNameDot = isNearRealtime && !(isJDBCDataSet && !isBigDataDialect && !dataSet.hasDataStoreTransformer()) ? DEFAULT_TABLE_NAME_DOT
				: "";

		String datasetLabel = dataSet.getLabel();

		IDataSource dataSource = getDataSource(dataSet, isNearRealtime);

		for (String columnName : columnNames) {
			for (ProjectionCriteria projection : projectionCriteria) {
				if (projection.getDataset().equals(datasetLabel) && projection.getColumnName().equals(columnName)) {
					IAggregationFunction aggregationFunction = AggregationFunctions.get(projection.getAggregateFunction());
					boolean isAttribute = aggregationFunction == null || aggregationFunction.equals(AggregationFunctions.NONE_FUNCTION);
					if (isAttribute == getAttributes) {
						if (columnName.contains(":")) {
							columnName = getDatasetManagementAPI().getQbeDataSetColumn(dataSet, columnName);
						}
						String encapsulatedColumnName = AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource);
						if (isAttribute) {
							attributesOrMeasures.add(defaultTableNameDot + encapsulatedColumnName);
						} else {
							attributesOrMeasures.add(defaultTableNameDot + aggregationFunction.apply(encapsulatedColumnName));
						}
					}
					break;
				}
			}
		}

		return attributesOrMeasures;
	}

	private String[] getDistinctValues(String values) {
		ArrayList<String> arrayList = new ArrayList<>();
		// get values between "'"
		int start = values.indexOf("'");
		while (start > -1) {
			int end = values.indexOf("'", start + 1);
			arrayList.add(values.substring(start + 1, end));
			values = values.substring(end + 1);
			start = values.indexOf("'");
		}
		return arrayList.toArray(new String[0]);
	}

	private String getValueForQuery(String value, boolean isDate, IDataSource dataSource) {
		if (isDate) {
			return getDateForQuery(value, dataSource);
		} else {
			if (value.startsWith("'") && value.endsWith("'")) {
				return value;
			} else {
				return "'" + value + "'";
			}
		}
	}

	private String fixColumnAliasesAndNames(String columns, Map<String, String> columnAliasToName) {
		if (columnAliasToName != null) {
			String[] columnsSplitted = columns.split("\\s*,\\s*");
			Set<String> aliases = columnAliasToName.keySet();

			for (int i = 0; i < columnsSplitted.length; i++) {
				String column = columnsSplitted[i].trim();
				if (aliases.contains(column)) {
					columnsSplitted[i] = columnAliasToName.get(column);
				}
			}
			return StringUtils.join(columnsSplitted, ",");
		} else {
			return columns;
		}
	}

	@POST
	@Path("/{label}/data")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getDataStorePost(@PathParam("label") String label, @QueryParam("parameters") String parameters, String selections,
			@QueryParam("likeSelections") String likeSelections, @DefaultValue("-1") @QueryParam("limit") int maxRowCount,
			@QueryParam("aggregations") String aggregations, @QueryParam("summaryRow") String summaryRow, @DefaultValue("-1") @QueryParam("offset") int offset,
			@DefaultValue("-1") @QueryParam("size") int fetchSize, @QueryParam("nearRealtime") boolean isNearRealtime) {
		logger.debug("IN");
		try {
			return getDataStore(label, parameters, selections, likeSelections, maxRowCount, aggregations, summaryRow, offset, fetchSize, isNearRealtime);
		} catch (Exception e) {
			throw new SpagoBIRestServiceException(buildLocaleFromSession(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	@POST
	@Path("/addDatasetInCache")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Response addDatasetInCache(@Context HttpServletRequest req) {
		logger.debug("IN");
		try {
			JSONArray requestBodyJSONArray = RestUtilities.readBodyAsJSONArray(req);
			for (int i = 0; i < requestBodyJSONArray.length(); i++) {
				JSONObject info = requestBodyJSONArray.getJSONObject(i);
				getDataStore(info.getString("datasetLabel"), info.getString("parameters"), null, null, -1, info.getString("aggregation"), null, 0, 1,
						info.optBoolean("nearRealtime"));
			}
			return Response.ok().build();
		} catch (Exception e) {
			throw new SpagoBIRestServiceException(buildLocaleFromSession(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	protected IDataWriter getDataSetWriter() throws JSONException {
		CockpitJSONDataWriter dataWriter = new CockpitJSONDataWriter(getDataSetWriterProperties());
		dataWriter.setLocale(buildLocaleFromSession());
		return dataWriter;
	}
}