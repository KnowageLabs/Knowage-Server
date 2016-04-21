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
package it.eng.spagobi.metadata.cwm.jmi;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.CwmCatalog;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.CwmColumn;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.CwmForeignKey;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.CwmPrimaryKey;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.CwmSchema;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.CwmSqlsimpleType;
import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.CwmTable;
import it.eng.spagobi.meta.model.physical.PhysicalColumn;
import it.eng.spagobi.meta.model.physical.PhysicalForeignKey;
import it.eng.spagobi.meta.model.physical.PhysicalModel;
import it.eng.spagobi.meta.model.physical.PhysicalModelFactory;
import it.eng.spagobi.meta.model.physical.PhysicalPrimaryKey;
import it.eng.spagobi.meta.model.physical.PhysicalTable;
import it.eng.spagobi.metadata.cwm.ICWM;
import it.eng.spagobi.metadata.cwm.ICWMMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class SpagoBICWMMapperJMIImpl extends ICWMMapper {

	static public PhysicalModelFactory FACTORY = PhysicalModelFactory.eINSTANCE;

	// -----------------------------------------------------------------------------
	// DECODE
	// -----------------------------------------------------------------------------

	@Override
	public PhysicalModel decodeICWM(ICWM cwm) {
		return decodeModel((SpagoBICWMJMIImpl) cwm);
	}

	public PhysicalModel decodeModel(SpagoBICWMJMIImpl cwm) {
		PhysicalModel model = FACTORY.createPhysicalModel();
		model.setName(cwm.getName());

		model.setCatalog(cwm.getCatalog().getName());

		return model;
	}

	// -----------------------------------------------------------------------------
	// ENCODE
	// -----------------------------------------------------------------------------

	@Override
	public SpagoBICWMJMIImpl encodeICWM(PhysicalModel model) {

		SpagoBICWMJMIImpl cwm = new SpagoBICWMJMIImpl(model.getName());
		// Create Catalog
		CwmCatalog catalog = cwm.createCatalog(model.getCatalog());
		// Create Schema and attach to catalog
		CwmSchema schema = cwm.createSchema(model.getSchema());
		catalog.getOwnedElement().add(schema);
		schema.setNamespace(catalog);

		// Create tables and attach them to the schema
		List<PhysicalTable> tables = model.getTables();
		CwmTable cwmTable;
		Collection<CwmTable> ts = schema.getOwnedElement();
		List<CwmPrimaryKey> addedCwmPrimaryKeys = new ArrayList<CwmPrimaryKey>();
		for (int i = 0; i < tables.size(); i++) {
			cwmTable = encodeTable(cwm, tables.get(i));
			ts.add(cwmTable);
			cwmTable.setNamespace(schema);
			// Check and Create primary keys
			PhysicalPrimaryKey physicalPrimaryKey = model.getPrimaryKey(tables.get(i));
			CwmPrimaryKey cwmPrimaryKey = null;
			List<PhysicalColumn> primaryKeyColumns = null;
			if (physicalPrimaryKey != null) {
				cwmPrimaryKey = encodePrimaryKey(cwm, physicalPrimaryKey.getName());
				addedCwmPrimaryKeys.add(cwmPrimaryKey);
				primaryKeyColumns = physicalPrimaryKey.getColumns();
			}
			// Check and create foreign keys

			List<PhysicalForeignKey> physicalForeignKeys = model.getForeignKeys(tables.get(i));
			List<CwmForeignKey> cwmForeignKeys = new ArrayList<CwmForeignKey>();
			for (PhysicalForeignKey physicalForeignKey : physicalForeignKeys) {
				CwmForeignKey cwmForeignKey = encodeForeignKey(cwm, physicalForeignKey.getName());
				cwmForeignKey.setNamespace(cwmTable);
				cwmTable.getOwnedElement().add(cwmForeignKey);
				cwmForeignKeys.add(cwmForeignKey);
			}

			// Create columns and attach to table
			List<PhysicalColumn> columns = tables.get(i).getColumns();
			for (PhysicalColumn column : columns) {
				CwmColumn cwmColumn = encodeColumn(cwm, column);
				cwmColumn.setLength(column.getSize());
				cwmColumn.setType(encodeSQLSimpleType(cwm, column.getTypeName()));
				cwmColumn.setOwner(cwmTable);
				cwmTable.getFeature().add(cwmColumn);

				if (physicalPrimaryKey != null) {
					if (primaryKeyColumns.contains(column)) {
						// column is part of a pk, add it to the cwmPrimaryKey
						cwmPrimaryKey.getFeature().add(cwmColumn);
					}
				}

				// check if column is used in a foreign key (source columns)
				for (PhysicalForeignKey physicalForeignKey : physicalForeignKeys) {
					List<PhysicalColumn> fkSourceColumns = physicalForeignKey.getSourceColumns();
					if (fkSourceColumns.contains(column)) {
						for (CwmForeignKey cwmForeignKey : cwmForeignKeys) {
							if (cwmForeignKey.getName().equals(physicalForeignKey.getName())) {
								cwmForeignKey.getFeature().add(cwmColumn);
							}
						}
					}
				}
			}
			if (physicalPrimaryKey != null) {
				cwmTable.getOwnedElement().add(cwmPrimaryKey);
				cwmPrimaryKey.setNamespace(cwmTable);
			}
		}

		// TODO: Set the the referenced primary keys of the foreign keys
		Collection<CwmForeignKey> foreignKeys = cwm.getForeignKeys();
		Collection<CwmPrimaryKey> primaryKeys = cwm.getPrimaryKeys();

		for (CwmForeignKey foreignKey : foreignKeys) {
			List<PhysicalForeignKey> physicalForeignKeys = model.getForeignKeys();
			for (PhysicalForeignKey physicalForeignKey : physicalForeignKeys) {
				if (physicalForeignKey.getName().equals(foreignKey.getName())) {
					List<PhysicalColumn> destinationColumns = physicalForeignKey.getDestinationColumns();
					for (PhysicalColumn destinationColumn : destinationColumns) {
						for (CwmPrimaryKey primaryKey : primaryKeys) {

						}
					}
				}
			}
		}

		return cwm;
	}

	public CwmTable encodeTable(SpagoBICWMJMIImpl cwm, PhysicalTable table) {
		CwmTable t;
		t = cwm.createTable(table.getName());
		return t;
	}

	public CwmColumn encodeColumn(SpagoBICWMJMIImpl cwm, PhysicalColumn column) {
		CwmColumn c;
		c = cwm.createColumn(column.getName());

		return c;
	}

	public CwmSqlsimpleType encodeSQLSimpleType(SpagoBICWMJMIImpl cwm, String name) {
		CwmSqlsimpleType t;
		t = cwm.createSQLSimpleType(name);
		return t;
	}

	public CwmPrimaryKey encodePrimaryKey(SpagoBICWMJMIImpl cwm, String name) {
		CwmPrimaryKey pk;
		pk = cwm.createPrimaryKey(name);
		return pk;
	}

	public CwmForeignKey encodeForeignKey(SpagoBICWMJMIImpl cwm, String name) {
		CwmForeignKey fk;
		fk = cwm.createForeignKey(name);
		return fk;
	}

}
