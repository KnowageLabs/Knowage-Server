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
package it.eng.spagobi.api.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.tools.dataset.metasql.query.item.AbstractSelectionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.DataStoreCalculatedField;
import it.eng.spagobi.tools.dataset.metasql.query.item.SimpleSelectionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;
import it.eng.spagobi.utilities.MockDataSet;

public class AbstractDataSetResourceFormulaTest {

	private static final String CALCULATED_ALIAS = "calc_alias";

	private TestAbstractDataSetResource resource;
	private IDataSet dataSet;
	private Map<String, String> columnAliasToName;

	@Before
	public void setUp() {
		resource = new TestAbstractDataSetResource();
		dataSet = buildDataSet();
		columnAliasToName = new HashMap<>();
		columnAliasToName.put(CALCULATED_ALIAS, CALCULATED_ALIAS);
	}

	@Test
	public void shouldValidateQuotedFormulaSyntax() {
		resource.validateFormula("SUM(\"num_cars_owned\") + MAX(\"member_card\")",
				buildColumns("num_cars_owned", "member_card"));
	}

	@Test
	public void shouldValidateFieldPlaceholderSyntax() {
		resource.validateFormula("SUM($F{num_cars_owned}) + MAX($F{member_card})",
				buildColumns("num_cars_owned", "member_card"));
	}

	@Test
	public void shouldRejectUnknownPlaceholderFieldAsUnknownField() {
		try {
			resource.validateFormula("SUM($F{missing_field})", buildColumns("num_cars_owned", "member_card"));
			fail("Expected formula validation to fail");
		} catch (ValidationException e) {
			assertEquals("common.errors.formulas.unknownField", e.getMessage());
		}
	}

	@Test
	public void shouldNormalizePlaceholdersWhenBuildingCalculatedProjection() throws Exception {
		JSONArray measures = new JSONArray().put(formulaMeasure("SUM($F{num_cars_owned})"));

		List<AbstractSelectionField> projections = resource.exposeProjections(dataSet, new JSONArray(), measures,
				columnAliasToName);

		assertEquals(1, projections.size());
		assertTrue(projections.get(0) instanceof DataStoreCalculatedField);

		DataStoreCalculatedField projection = (DataStoreCalculatedField) projections.get(0);
		assertEquals("SUM(\"num_cars_owned\")", projection.getFormula());
		assertEquals("SUM(\"num_cars_owned\")", projection.getName());
	}

	@Test
	public void shouldNormalizePlaceholdersWhenBuildingSorting() throws Exception {
		JSONArray measures = new JSONArray().put(formulaMeasure("SUM($F{num_cars_owned})")
				.put("orderType", "ASC")
				.put("orderColumn", CALCULATED_ALIAS));

		List<Sorting> sortings = resource.exposeSortings(dataSet, new JSONArray(), measures, columnAliasToName);

		assertEquals(1, sortings.size());
		assertTrue(sortings.get(0).getProjection() instanceof DataStoreCalculatedField);
		assertTrue(sortings.get(0).isAscending());

		DataStoreCalculatedField projection = (DataStoreCalculatedField) sortings.get(0).getProjection();
		assertEquals("SUM(\"num_cars_owned\")", projection.getFormula());
	}

	private IDataSet buildDataSet() {
		MockDataSet mockDataSet = new MockDataSet();
		MetaData metadata = new MetaData();
		metadata.addFiedMeta(buildField("num_cars_owned"));
		metadata.addFiedMeta(buildField("member_card"));
		mockDataSet.setMetadata(metadata);
		return mockDataSet;
	}

	private FieldMetadata buildField(String fieldName) {
		FieldMetadata fieldMetadata = new FieldMetadata(fieldName, Integer.class);
		fieldMetadata.setAlias(fieldName);
		return fieldMetadata;
	}

	private List<SimpleSelectionField> buildColumns(String... columnNames) {
		List<SimpleSelectionField> columns = new ArrayList<>(columnNames.length);
		for (String columnName : columnNames) {
			SimpleSelectionField column = new SimpleSelectionField();
			column.setName(columnName);
			columns.add(column);
		}
		return columns;
	}

	private JSONObject formulaMeasure(String formula) throws JSONException {
		return new JSONObject()
				.put("alias", CALCULATED_ALIAS)
				.put("formula", formula)
				.put("funct", "NONE");
	}

	private static class TestAbstractDataSetResource extends AbstractDataSetResource {

		List<AbstractSelectionField> exposeProjections(IDataSet dataSet, JSONArray categories, JSONArray measures,
				Map<String, String> columnAliasToName) throws JSONException, ValidationException {
			return getProjections(dataSet, categories, measures, columnAliasToName);
		}

		List<Sorting> exposeSortings(IDataSet dataSet, JSONArray categories, JSONArray measures,
				Map<String, String> columnAliasToName) throws JSONException {
			return getSortings(dataSet, categories, measures, columnAliasToName);
		}
	}
}
