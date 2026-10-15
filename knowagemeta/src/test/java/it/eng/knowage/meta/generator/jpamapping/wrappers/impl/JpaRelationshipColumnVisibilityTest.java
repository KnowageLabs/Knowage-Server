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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.meta.generator.jpamapping.wrappers.impl;

import java.io.StringWriter;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import it.eng.knowage.meta.generator.jpamapping.wrappers.IJpaColumn;
import it.eng.knowage.meta.generator.jpamapping.wrappers.JpaProperties;
import it.eng.knowage.meta.model.ModelFactory;
import it.eng.knowage.meta.model.ModelObject;
import it.eng.knowage.meta.model.ModelProperty;
import it.eng.knowage.meta.model.business.BusinessIdentifier;
import it.eng.knowage.meta.model.business.BusinessModel;
import it.eng.knowage.meta.model.business.BusinessModelFactory;
import it.eng.knowage.meta.model.business.BusinessRelationship;
import it.eng.knowage.meta.model.business.BusinessTable;
import it.eng.knowage.meta.model.business.SimpleBusinessColumn;
import it.eng.knowage.meta.model.physical.PhysicalColumn;
import it.eng.knowage.meta.model.physical.PhysicalModelFactory;
import it.eng.knowage.meta.model.physical.PhysicalTable;
import junit.framework.TestCase;

public class JpaRelationshipColumnVisibilityTest extends TestCase {

	private BusinessTable source;
	private BusinessTable target;
	private SimpleBusinessColumn sourceId;
	private SimpleBusinessColumn sourceJoin;
	private SimpleBusinessColumn targetJoin;

	@Override
	protected void setUp() {
		BusinessModel model = BusinessModelFactory.eINSTANCE.createBusinessModel();
		model.setPhysicalModel(PhysicalModelFactory.eINSTANCE.createPhysicalModel());
		source = createTable(model, "Source");
		target = createTable(model, "Target");
		sourceId = createColumn(source, "id", "true");
		sourceId.setIdentifier(true);
		sourceJoin = createColumn(source, "targetCode", "true");
		createColumn(target, "id", "true").setIdentifier(true);
		targetJoin = createColumn(target, "code", "true");

		BusinessRelationship relationship = BusinessModelFactory.eINSTANCE.createBusinessRelationship();
		relationship.setName("sourceToTarget");
		relationship.setSourceTable(source);
		relationship.setDestinationTable(target);
		relationship.getSourceColumns().add(sourceJoin);
		relationship.getDestinationColumns().add(targetJoin);
		model.getRelationships().add(relationship);
	}

	public void testVisibleOutboundColumnIsGeneratedAsScalar() {
		JpaTable table = new JpaTable(source);
		IJpaColumn column = findColumn(table, sourceJoin);
		assertTrue(column.isColumnInRelationship());
		assertTrue(table.getSimpleColumns().contains(column));
	}

	public void testVisibleInboundColumnIsGeneratedAsScalar() {
		JpaTable table = new JpaTable(target);
		assertTrue(table.getSimpleColumns().contains(findColumn(table, targetJoin)));
	}

	public void testHiddenOutboundColumnKeepsExistingGenerationBehavior() {
		putProperty(sourceJoin, "structural.visible", "false");
		JpaTable table = new JpaTable(source);
		assertFalse(table.getSimpleColumns().contains(findColumn(table, sourceJoin)));
	}

	public void testOutboundColumnWithoutExplicitVisibilityRemainsExcluded() {
		sourceJoin.getProperties().remove("structural.visible");
		JpaTable table = new JpaTable(source);
		assertFalse(table.getSimpleColumns().contains(findColumn(table, sourceJoin)));
	}

	public void testCompositeKeyColumnsAreNotGeneratedTwice() {
		sourceJoin.setIdentifier(true);
		JpaTable table = new JpaTable(source);
		assertTrue(table.hasCompositeKey());
		assertTrue(table.getSimpleColumns().isEmpty());
	}

	public void testFakeKeyColumnsAreNotGeneratedTwice() {
		source.getModel().getIdentifiers().remove(source.getIdentifier());
		JpaTable table = new JpaTable(source);
		assertTrue(table.hasFakePrimaryKey());
		assertTrue(table.getSimpleColumns().isEmpty());
	}

	public void testVisibleOutboundScalarMappingIsReadOnly() {
		JpaTable table = new JpaTable(source);
		String mapping = renderColumn(table, findColumn(table, sourceJoin));
		assertTrue(mapping.contains("name=\"targetCode\""));
		assertTrue(mapping.contains("insertable = false, updatable = false"));
	}

	public void testOrdinaryAndPrimaryKeyMappingsRemainWritable() {
		JpaTable table = new JpaTable(target);
		assertFalse(renderColumn(table, findColumn(table, targetJoin)).contains("insertable = false"));
		JpaTable sourceTable = new JpaTable(source);
		assertFalse(renderColumn(sourceTable, findColumn(sourceTable, sourceId)).contains("insertable = false"));
	}

	public void testRelationshipPrimaryKeyMappingRemainsWritable() {
		sourceJoin.setIdentifier(true);
		JpaTable table = new JpaTable(source);
		assertFalse(renderColumn(table, findColumn(table, sourceJoin)).contains("insertable = false"));
	}

	public void testFakeKeyMappingRemainsWritable() {
		source.getModel().getIdentifiers().remove(source.getIdentifier());
		JpaTable table = new JpaTable(source);
		assertFalse(renderColumn(table, findColumn(table, sourceJoin)).contains("insertable = false"));
	}

	private BusinessTable createTable(BusinessModel model, String name) {
		PhysicalTable physicalTable = PhysicalModelFactory.eINSTANCE.createPhysicalTable();
		physicalTable.setName(name);
		model.getPhysicalModel().getTables().add(physicalTable);
		BusinessTable table = BusinessModelFactory.eINSTANCE.createBusinessTable();
		table.setName(name);
		table.setId(name);
		table.setUniqueName(name);
		table.setModel(model);
		table.setPhysicalTable(physicalTable);
		model.getTables().add(table);
		BusinessIdentifier identifier = BusinessModelFactory.eINSTANCE.createBusinessIdentifier();
		identifier.setId(name + "Identifier");
		identifier.setName(name + "Identifier");
		identifier.setTable(table);
		identifier.setModel(model);
		model.getIdentifiers().add(identifier);
		return table;
	}

	private SimpleBusinessColumn createColumn(BusinessTable table, String name, String visible) {
		PhysicalColumn physicalColumn = PhysicalModelFactory.eINSTANCE.createPhysicalColumn();
		physicalColumn.setName(name);
		table.getPhysicalTable().getColumns().add(physicalColumn);
		SimpleBusinessColumn column = BusinessModelFactory.eINSTANCE.createSimpleBusinessColumn();
		column.setName(name);
		column.setId(table.getName() + "." + name);
		column.setUniqueName(name);
		column.setTable(table);
		column.setPhysicalColumn(physicalColumn);
		table.getColumns().add(column);
		putProperty(column, JpaProperties.COLUMN_DATATYPE, "INTEGER");
		putProperty(column, "structural.visible", visible);
		return column;
	}

	private void putProperty(ModelObject object, String name, String value) {
		ModelProperty property = ModelFactory.eINSTANCE.createModelProperty();
		property.setValue(value);
		object.getProperties().put(name, property);
	}

	private IJpaColumn findColumn(JpaTable table, SimpleBusinessColumn businessColumn) {
		List<IJpaColumn> columns = table.getColumns();
		for (IJpaColumn column : columns) {
			if (column.getName().equals(businessColumn.getName())) {
				return column;
			}
		}
		throw new AssertionError("Missing column " + businessColumn.getName());
	}

	private String renderColumn(JpaTable table, IJpaColumn column) {
		VelocityEngine engine = new VelocityEngine();
		engine.setProperty("resource.loaders", "class");
		engine.setProperty("resource.loader.class.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		engine.init();
		VelocityContext context = new VelocityContext();
		context.put("jpaTable", table);
		context.put("jpaColumn", column);
		StringWriter writer = new StringWriter();
		engine.getTemplate("it/eng/knowage/meta/generator/templates/sbi_column.vm").merge(context, writer);
		return writer.toString();
	}
}
