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

package it.eng.spagobi.tools.dataset.strategy;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FieldStatsInfo;
import org.json.JSONException;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.SolrDataSet;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.SolrFacetPivotDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.XmlDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.tools.dataset.metasql.query.item.AbstractSelectionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.DataStoreCalculatedField;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;
import it.eng.spagobi.tools.dataset.solr.ExtendedSolrQuery;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.scripting.SpagoBIScriptManager;

class SolrFacetPivotEvaluationStrategy extends SolrEvaluationStrategy {

	private static final Logger logger = Logger.getLogger(SolrFacetPivotEvaluationStrategy.class);

	public SolrFacetPivotEvaluationStrategy(IDataSet dataSet) {
		super(dataSet);
	}

	@Override
	protected IDataStore execute(List<AbstractSelectionField> projections, Filter filter, List<AbstractSelectionField> groups, List<Sorting> sortings,
			List<List<AbstractSelectionField>> summaryRowProjections, int offset, int fetchSize, int maxRowCount, Set<String> columns) {
		SolrDataSet solrDataSet = dataSet.getImplementation(SolrDataSet.class);
		solrDataSet.setSolrQueryParameters(solrDataSet.getSolrQuery(), solrDataSet.getParamsMap());
		SolrQuery solrQuery;
		boolean hasCalculatedFields = false;
		List<AbstractSelectionField> prjList = new ArrayList<AbstractSelectionField>();
		List<AbstractSelectionField> grpList = new ArrayList<AbstractSelectionField>();
		List<AbstractSelectionField> calcuList = new ArrayList<AbstractSelectionField>();
		List<AbstractSelectionField> calcuGrpList = new ArrayList<AbstractSelectionField>();
		try {
			for (AbstractSelectionField entry : projections) {
				if (entry instanceof DataStoreCalculatedField) {
					hasCalculatedFields = true;
					calcuList.add(entry);
				} else {
					prjList.add(entry);
				}

			}
			for (AbstractSelectionField entry : groups) {
				if (entry instanceof DataStoreCalculatedField) {
					hasCalculatedFields = true;
					calcuGrpList.add(entry);
				} else {
					grpList.add(entry);
				}

			}

			solrQuery = new ExtendedSolrQuery(solrDataSet.getSolrQuery()).filter(filter).jsonFacets(prjList, grpList, sortings);
		} catch (JSONException e) {
			throw new SpagoBIRuntimeException(e);
		}
		solrQuery.setRows(0);

		solrDataSet.setSolrQuery(solrQuery, null);

		JSONPathDataReader dataReader = solrDataSet.getDataReader();
		SolrFacetPivotDataReader solrFacetPivotDataReader = new SolrFacetPivotDataReader(dataReader.getJsonPathItems(), dataReader.getJsonPathAttributes());
		solrDataSet.setDataReader(solrFacetPivotDataReader);

		dataSet.loadData(offset, fetchSize, maxRowCount);
		IDataStore dataStore = dataSet.getDataStore();

		if (hasCalculatedFields) {
			try {
				for (AbstractSelectionField abstractSelectionField : calcuList) {
					dataStore = appendCalculatedFieldColumn(abstractSelectionField, calcuGrpList, dataStore);
				}
			} catch (Throwable t) {
				throw new RuntimeException("An unexpected error occured while loading datastore", t);
			}
		}
		dataStore.setCacheDate(getDate());
		return dataStore;
	}

	private IDataStore appendCalculatedFieldColumn(AbstractSelectionField abstractSelectionField, List<AbstractSelectionField> groupdFieldList,
			IDataStore pagedDataStore) throws URISyntaxException {

		IDataStore datastoresToAdd = new DataStore();

		DataStoreCalculatedField field = (DataStoreCalculatedField) abstractSelectionField;

		IMetaData pagedMetaData = pagedDataStore.getMetaData();
		pagedMetaData.addFiedMeta(new FieldMetadata(field.getAlias(), BigDecimal.class));

		// build new datastore calculated fields columns

		XmlDataReader dataReader = new XmlDataReader();
		SpagoBIScriptManager scriptManager = new SpagoBIScriptManager();

		List<File> imports = new ArrayList<File>();
		URL url = Thread.currentThread().getContextClassLoader().getResource("predefinedGroovyScript.groovy");
		File scriptFile = new File(url.toURI());
		imports.add(scriptFile);

		Map<String, Object> bindings = new HashMap<String, Object>();

		// add columns to result datastore

		datastoresToAdd.setMetaData(pagedMetaData);

		for (int projectionIndex = 0; projectionIndex < pagedDataStore.getRecordsCount(); projectionIndex++) {
			Record newRecord = new Record();
			newRecord = (Record) pagedDataStore.getRecordAt(projectionIndex);

			// method that calculates formula result getting each real value field

			String resultingCalculation = transformFormula(newRecord, pagedMetaData, field.getFormula());
			try {
				Object o = scriptManager.runScript(resultingCalculation, "groovy", bindings, imports);
				String data = (o == null) ? "" : o.toString();

				IField fieldNew = null;
				if (data != null && data.isEmpty()) {
					fieldNew = new Field();
				} else {
					fieldNew = new Field(new BigDecimal(data));
				}
				newRecord.appendField(fieldNew);

				datastoresToAdd.appendRecord(newRecord);
			} catch (Exception ex) {

				if (ex.getCause().getMessage().contains("Division by zero")) {
					IField fieldNew = new Field();
					newRecord.appendField(fieldNew);

					datastoresToAdd.appendRecord(newRecord);
				} else
					throw ex;
			}
		}

		// IDataStore columnsDataStore = new
		return datastoresToAdd;

	}

	private IDataStore appendCalculatedFieldColumnToSummaryRow(AbstractSelectionField abstractSelectionField, List<AbstractSelectionField> groupdFieldList,
			IDataStore pagedDataStore) throws URISyntaxException {

		IDataStore datastoresToAdd = new DataStore();

		DataStoreCalculatedField field = (DataStoreCalculatedField) abstractSelectionField;

		IMetaData pagedMetaData = pagedDataStore.getMetaData();
		pagedMetaData.addFiedMeta(new FieldMetadata(field.getAlias(), BigDecimal.class));

		// build new datastore calculated fields columns

		XmlDataReader dataReader = new XmlDataReader();
		SpagoBIScriptManager scriptManager = new SpagoBIScriptManager();

		List<File> imports = new ArrayList<File>();
		URL url = Thread.currentThread().getContextClassLoader().getResource("predefinedGroovyScript.groovy");
		File scriptFile = new File(url.toURI());
		imports.add(scriptFile);

		// add columns to result datastore

		datastoresToAdd.setMetaData(pagedMetaData);

		for (int projectionIndex = 0; projectionIndex < pagedDataStore.getRecordsCount(); projectionIndex++) {
			Record newRecord = new Record();
			newRecord = (Record) pagedDataStore.getRecordAt(projectionIndex);

			// method that calculates formula result getting each real value field

			String resultingCalculation = field.getFormula().replaceAll("\"", "");
			// transformFormula(newRecord, pagedMetaData, field.getFormula());

			Map<String, Object> bindings = findBindings(newRecord, pagedMetaData, field.getFormula());

			Object o = scriptManager.runScript(resultingCalculation, "groovy", bindings, imports);
			String data = (o == null) ? "" : o.toString();

			IField fieldNew = new Field(new BigDecimal(data));
			newRecord.appendField(fieldNew);

			datastoresToAdd.appendRecord(newRecord);
		}

		return datastoresToAdd;

	}

	public String transformFormula(Record record, IMetaData metadata, String formula) {
		formula = formula.replaceAll("\"", "");

		for (int i = 0; i < metadata.getFieldCount(); i++) {

			if (formula.contains(metadata.getFieldName(i)) || formula.contains(metadata.getFieldAlias(i))) {
				String fieldToReplace = metadata.getFieldName(i);

				if (formula.contains(metadata.getFieldAlias(i))) {
					fieldToReplace = metadata.getFieldAlias(i);
				}
				String pattern = "((?:AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)\\()(" + fieldToReplace + ")(\\))";
				Pattern r = Pattern.compile(pattern);

				Matcher m = r.matcher(formula);

				while (m.find()) {
					formula = formula.replace(m.group(), record.getFieldAt(i).getValue().toString());
				}

				pattern = "((?:AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)\\()([a-zA-Z0-9\\-\\+\\/\\*\\_\\s\\$\\{\\}\\\"]*)(\\))";
				r = Pattern.compile(pattern);
				m = r.matcher(formula);

				while (m.find()) {
					formula = formula.replace(m.group(), record.getFieldAt(i).getValue().toString());
				}

			}

		}

		return formula;

	}

	public Map<String, Object> findBindings(Record record, IMetaData metadata, String formula) {

		Map<String, Object> bindings = new HashMap<String, Object>();
		bindings.put("parameters", new HashMap());

		for (int i = 0; i < metadata.getFieldCount(); i++) {

			if (formula.contains(metadata.getFieldName(i))) {

				BigDecimal value = new BigDecimal(record.getFieldAt(i).getValue().toString());

				bindings.put(metadata.getFieldName(i), value);

			}

		}

		return bindings;

	}

	@Override
	protected IDataStore executeSummaryRow(List<AbstractSelectionField> summaryRowProjections, IMetaData metaData, Filter filter, int maxRowCount) {

		List<AbstractSelectionField> prjList = new ArrayList<AbstractSelectionField>();
		List<AbstractSelectionField> calcList = new ArrayList<AbstractSelectionField>();
		for (AbstractSelectionField entry : summaryRowProjections) {
			if (entry instanceof DataStoreCalculatedField) {
				calcList.add(entry);
			} else {
				prjList.add(entry);
			}
		}

		IDataStore dataStore = new DataStore(metaData);
		SolrDataSet solrDataSet = dataSet.getImplementation(SolrDataSet.class);
		SolrQuery solrQuery;
		try {
			solrQuery = new ExtendedSolrQuery(solrDataSet.getSolrQuery()).fields(prjList).filter(filter).stats(prjList);
			logger.debug("Solr query for summary row: " + solrQuery);
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while loading datastore", t);
		}
		SolrClient solrClient = new HttpSolrClient.Builder(solrDataSet.getSolrUrlWithCollection()).build();
		Map<String, FieldStatsInfo> fieldStatsInfo;
		try {
			fieldStatsInfo = solrClient.query(solrQuery).getFieldStatsInfo();
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}
		IRecord summaryRow = new Record(dataStore);
		for (int i = 0; i < dataStore.getMetaData().getFieldCount(); i++) {
			String fieldName = dataStore.getMetaData().getFieldName(i);
			boolean found = false;
			for (AbstractSelectionField proj : prjList) {
				if (proj instanceof Projection) {
					Projection projection = (Projection) proj;
					if (projection.getName().equals(fieldName)) {
						Object value = getValue(fieldStatsInfo.get(fieldName), projection.getAggregationFunction());
						IField field = new Field(value);
						dataStore.getMetaData().getFieldMeta(i).setType(value.getClass());
						summaryRow.appendField(field);
						found = true;
						break;
					}
				}

				else {
					DataStoreCalculatedField projection = (DataStoreCalculatedField) proj;
					if (projection.getName().equals(fieldName)) {
						Object value = getValue(fieldStatsInfo.get(fieldName), projection.getAggregationFunction());
						IField field = new Field(value);
						dataStore.getMetaData().getFieldMeta(i).setType(value.getClass());
						summaryRow.appendField(field);
						found = true;
						break;
					}
				}
			}
			if (!found) {
				summaryRow.appendField(null);
			}
		}

		dataStore.appendRecord(summaryRow);
		dataStore.getMetaData().setProperty("resultNumber", 1);
		try {
			for (AbstractSelectionField abstractSelectionField : calcList) {
				dataStore = appendCalculatedFieldColumnToSummaryRow(abstractSelectionField, null, dataStore);
			}
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while loading datastore", t);
		}
		IDataStore dataStoreFinal = new DataStore();
		Record newRecord = new Record();
		MetaData meta = new MetaData();

		for (int i = 0; i < metaData.getFieldCount(); i++) {

			for (AbstractSelectionField entry : summaryRowProjections) {
				if (entry instanceof DataStoreCalculatedField) {
					if (metaData.getFieldName(i).equals(((DataStoreCalculatedField) entry).getAlias())) {
						int realIndex = 0;
						for (int j = 0; j < dataStore.getMetaData().getFieldCount(); j++) {
							if (dataStore.getMetaData().getFieldName(j).equals(((DataStoreCalculatedField) entry).getAlias())) {
								realIndex = j;
							}
						}

						IField field = dataStore.getRecordAt(0).getFieldAt(realIndex);
						meta.addFiedMeta(metaData.getFieldMeta(i));
						newRecord.appendField(field);
					}
				} else {
					if (metaData.getFieldName(i).equals(((Projection) entry).getAlias())) {
						if (metaData.getFieldAlias(i).equalsIgnoreCase(((Projection) entry).getName())) {
							int realIndex = 0;
							for (int j = 0; j < dataStore.getMetaData().getFieldCount(); j++) {
								if (dataStore.getMetaData().getFieldName(j).equals(((Projection) entry).getName())) {
									realIndex = j;
								}
							}
							IField field = dataStore.getRecordAt(0).getFieldAt(realIndex);
							meta.addFiedMeta(metaData.getFieldMeta(i));
							newRecord.appendField(field);

						}
					}
				}

			}

		}

		dataStoreFinal.setMetaData(meta);
		dataStoreFinal.appendRecord(newRecord);
		return dataStoreFinal;
	}

	private Map<String, String> getFacetsWithAggregation(List<AbstractSelectionField> groups) {
		Map<String, String> facets = new HashMap<>(groups.size());
		for (AbstractSelectionField facet : groups) {

			if (facet instanceof Projection) {
				Projection proj = (Projection) facet;
				facets.put(proj.getName(), proj.getAggregationFunction().getName().toLowerCase());
			} else {
				DataStoreCalculatedField proj = (DataStoreCalculatedField) facet;
				facets.put(proj.getName(), proj.getAggregationFunction().getName().toLowerCase());
			}
		}
		return facets;
	}

}
