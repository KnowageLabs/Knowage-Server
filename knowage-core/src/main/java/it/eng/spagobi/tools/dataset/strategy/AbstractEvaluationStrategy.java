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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.metasql.query.item.AbstractSelectionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.DataStoreCalculatedField;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;
import it.eng.spagobi.tools.dataset.metasql.query.item.UnsatisfiedFilter;
import it.eng.spagobi.utilities.assertion.Assert;

public abstract class AbstractEvaluationStrategy implements IDatasetEvaluationStrategy {

	private static final String TOTAL_PREFIX = "TOTAL_";

	private static final String TOTAL_COUNT_DISTINCT = "TOTAL_COUNT_DISTINCT";

	private static final Logger logger = Logger.getLogger(AbstractEvaluationStrategy.class);

	protected IDataSet dataSet;

	public AbstractEvaluationStrategy(IDataSet dataSet) {
		this.dataSet = dataSet;
	}

	@Override
	public IDataStore executeQuery(List<AbstractSelectionField> projections, Filter filter, List<AbstractSelectionField> groups, List<Sorting> sortings,
			List<List<AbstractSelectionField>> summaryRowProjections, int offset, int fetchSize, int maxRowCount, Set<String> indexes) {
		IDataStore dataStore;
		if (isUnsatisfiedFilter(filter)) {
			dataStore = new DataStore(dataSet.getMetadata());
		} else {
			List<AbstractSelectionField> newProjections = applyTotalsFunctionsToFormulas(dataSet, projections, filter, maxRowCount, indexes);
			dataStore = execute(newProjections, filter, groups, sortings, summaryRowProjections, offset, fetchSize, maxRowCount, indexes);
			IMetaData dataStoreToUseMeta = dataStore.getMetaData();
			if (!isSummaryRowIncluded() && summaryRowProjections != null && !summaryRowProjections.isEmpty()) {
				int i = 0;
				for (List<AbstractSelectionField> listProj : summaryRowProjections) {
					List<AbstractSelectionField> replacedSelectionFieldsList = applyTotalsFunctionsToFormulas(dataSet, listProj, filter, maxRowCount, indexes);

					IDataStore summaryRowDataStore = executeSummaryRow(replacedSelectionFieldsList, dataStoreToUseMeta, filter, maxRowCount);
					appendSummaryRowToPagedDataStore(newProjections, replacedSelectionFieldsList, dataStore, summaryRowDataStore, i);
					i++;
				}
			}
		}
		return dataStore;
	}

	private List<AbstractSelectionField> applyTotalsFunctionsToFormulas(IDataSet dataSet, List<AbstractSelectionField> projections, Filter filter,
			int maxRowCount, Set<String> indexes) {

		List<AbstractSelectionField> toReturnList = new ArrayList<AbstractSelectionField>();
		Set<String> totalFunctionsSet = new HashSet<String>();
		for (AbstractSelectionField abstractSelectionField : projections) {
			if (abstractSelectionField instanceof DataStoreCalculatedField) {
				String formula = ((DataStoreCalculatedField) abstractSelectionField).getFormula();
				if (formula.contains(TOTAL_PREFIX)) {

					Matcher m = getMatcherWithQuotes(formula);
					while (m.find()) {
						if (m.group().contains(TOTAL_COUNT_DISTINCT)) {
							totalFunctionsSet.add(m.group().replace(TOTAL_COUNT_DISTINCT, "COUNT(DISTINCT") + ")");
						} else {
							totalFunctionsSet.add(m.group().replace(TOTAL_PREFIX, ""));
						}
					}

					m = getMatcherWithParameters(formula);
					while (m.find()) {
						if (m.group().contains(TOTAL_COUNT_DISTINCT)) {
							totalFunctionsSet.add(m.group().replace(TOTAL_COUNT_DISTINCT, "COUNT(DISTINCT") + ")");
						} else {
							totalFunctionsSet.add(m.group().replace(TOTAL_PREFIX, ""));
						}
					}
				}
			}
		}

		if (!totalFunctionsSet.isEmpty()) {

			IDataStore totalsFunctionDataStore = executeTotalsFunctions(dataSet, totalFunctionsSet, filter, maxRowCount, indexes);

			HashMap<String, String> totalsMap = new HashMap<String, String>();
			int i = 0;
			for (String function : totalFunctionsSet) {
				totalsMap.put(function, String.valueOf(totalsFunctionDataStore.getRecordAt(0).getFieldAt(i).getValue()));
				i++;
			}

			for (AbstractSelectionField abstractSelectionField : projections) {
				AbstractSelectionField tmp = abstractSelectionField;
				if (tmp instanceof DataStoreCalculatedField) {
					String formula = ((DataStoreCalculatedField) tmp).getFormula();
					if (formula.contains(TOTAL_PREFIX)) {

						for (String totalFunction : totalsMap.keySet()) {

							if (formula.contains(TOTAL_COUNT_DISTINCT) && totalFunction.startsWith("COUNT(DISTINCT")) {

								boolean replaced = false;

								Matcher m = getMatcherWithQuotes(formula);
								while (m.find()) {
									if ((m.group().replace(TOTAL_COUNT_DISTINCT, "COUNT(DISTINCT") + ")").equals(totalFunction)) {
										formula = formula.replace(m.group(), totalsMap.get(totalFunction));
										replaced = true;
									}
								}

								if (!replaced) {
									m = getMatcherWithParameters(formula);
									while (m.find()) {
										if ((m.group().replace(TOTAL_COUNT_DISTINCT, "COUNT(DISTINCT") + ")").equals(totalFunction))
											formula = formula.replace(m.group(), totalsMap.get(totalFunction));
									}
								}

							} else {
								formula = formula.replace(TOTAL_PREFIX + totalFunction, totalsMap.get(totalFunction));
							}

						}
						((DataStoreCalculatedField) tmp).setFormula(formula);
					}
				}
				toReturnList.add(tmp);
			}
		} else {
			toReturnList = projections;
		}
		return toReturnList;
	}

	private Matcher getMatcherWithQuotes(String formula) {
		String pattern = "((?:TOTAL_SUM|TOTAL_AVG|TOTAL_MIN|TOTAL_MAX|TOTAL_COUNT|TOTAL_COUNT_DISTINCT)\\()(\\\"[a-zA-Z0-9\\-\\_\\s]*\\\")(\\))";
		Pattern r = Pattern.compile(pattern);

		return r.matcher(formula);
	}

	private Matcher getMatcherWithParameters(String formula) {
		String pattern = "((?:TOTAL_SUM|TOTAL_AVG|TOTAL_MIN|TOTAL_MAX|TOTAL_COUNT|TOTAL_COUNT_DISTINCT)\\()([a-zA-Z0-9\\-\\+\\/\\*\\_\\s\\$\\{\\}\\\"]*)(\\))";
		Pattern r = Pattern.compile(Pattern.quote(pattern));

		return r.matcher(formula);
	}

	@Override
	public IDataStore executeSummaryRowQuery(List<AbstractSelectionField> summaryRowProjections, Filter filter, int maxRowCount) {
		return executeSummaryRow(summaryRowProjections, dataSet.getMetadata(), filter, maxRowCount);
	}

	protected abstract IDataStore execute(List<AbstractSelectionField> projections, Filter filter, List<AbstractSelectionField> groups, List<Sorting> sortings,
			List<List<AbstractSelectionField>> summaryRowProjections, int offset, int fetchSize, int maxRowCount, Set<String> indexes);

	protected abstract IDataStore executeSummaryRow(List<AbstractSelectionField> summaryRowProjections, IMetaData metaData, Filter filter, int maxRowCount);

	protected boolean isSummaryRowIncluded() {
		return false;
	}

	protected Date getDate() {
		return now();
	}

	private Date now() {
		return new Date();
	}

	private boolean isUnsatisfiedFilter(Filter filter) {
		return filter instanceof UnsatisfiedFilter;
	}

	private void appendSummaryRowToPagedDataStore(List<AbstractSelectionField> projections, List<AbstractSelectionField> summaryRowProjections,
			IDataStore pagedDataStore, IDataStore summaryRowDataStore, int row) {
		IMetaData pagedMetaData = pagedDataStore.getMetaData();
		IMetaData summaryRowMetaData = summaryRowDataStore.getMetaData();

		Assert.assertTrue(pagedMetaData.getFieldCount() >= summaryRowMetaData.getFieldCount(), "Summary row field count cannot be less than data field count");

		// calc a map for summaryRowProjections -> projections
		Map<Integer, Integer> projectionToSummaryRowProjection = new HashMap<>();
		for (int i = 0; i < summaryRowProjections.size(); i++) {
			Projection summaryRowProjection = null;
			DataStoreCalculatedField summaryRowCalculatedField = null;
			if (summaryRowProjections.get(i) instanceof Projection)
				summaryRowProjection = (Projection) summaryRowProjections.get(i);
			else if (summaryRowProjections.get(i) instanceof DataStoreCalculatedField)
				summaryRowCalculatedField = (DataStoreCalculatedField) summaryRowProjections.get(i);

			for (int j = 0; j < projections.size(); j++) {

				AbstractSelectionField selections = projections.get(j);

				if (selections instanceof Projection) {
					Projection proj = (Projection) selections;
					String projectionAlias = proj.getAlias();
					if (summaryRowProjection != null
							&& (summaryRowProjection.getAlias().equals(projectionAlias) || summaryRowProjection.getName().equals(projectionAlias))) {
						projectionToSummaryRowProjection.put(j, i);
						break;
					}
				} else {

					if (row == 0 && selections instanceof DataStoreCalculatedField) {
						DataStoreCalculatedField calcs = (DataStoreCalculatedField) selections;
						String projectionAlias = calcs.getAlias();
						if (summaryRowCalculatedField != null && (summaryRowCalculatedField.getAlias().equals(projectionAlias)
								|| summaryRowCalculatedField.getName().equals(projectionAlias))) {
							projectionToSummaryRowProjection.put(j, i);
							break;
						}
					}
				}
			}
		}

		// append summary row
		IRecord summaryRowRecord = summaryRowDataStore.getRecordAt(0);
		Record newRecord = new Record();
		for (int projectionIndex = 0; projectionIndex < pagedMetaData.getFieldCount(); projectionIndex++) {
			Field field = new Field(null);
			if (projectionToSummaryRowProjection.containsKey(projectionIndex)) {
				Integer summaryRowIndex = projectionToSummaryRowProjection.get(projectionIndex);
				Object value = summaryRowRecord.getFieldAt(summaryRowIndex).getValue();
				field.setValue(value);
			}
			newRecord.appendField(field);
		}
		pagedDataStore.appendRecord(newRecord);

		// copy metadata from summary row
		for (Integer projectionIndex : projectionToSummaryRowProjection.keySet()) {
			Integer summaryRowIndex = projectionToSummaryRowProjection.get(projectionIndex);
			if (pagedMetaData.getFieldMeta(projectionIndex) != null && (pagedMetaData.getFieldMeta(projectionIndex).getType() == Double.class)) {
				pagedMetaData.getFieldMeta(projectionIndex).setType(Double.class);
			} else if (pagedMetaData.getFieldMeta(projectionIndex) != null && pagedMetaData.getFieldMeta(projectionIndex).getType() == BigDecimal.class) {
				pagedMetaData.getFieldMeta(projectionIndex).setType(BigDecimal.class);
			} else
				pagedMetaData.getFieldMeta(projectionIndex).setType(summaryRowMetaData.getFieldType(summaryRowIndex));
		}

	}

	protected IDataStore executeTotalsFunctions(IDataSet dataSet, Set<String> totalFunctionsProjections, Filter filter, int maxRowCount, Set<String> indexes) {
		// TODO Auto-generated method stub
		return null;
	}
}
