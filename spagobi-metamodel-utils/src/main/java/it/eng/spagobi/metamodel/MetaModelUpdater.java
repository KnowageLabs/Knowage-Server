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
package it.eng.spagobi.metamodel;

import it.eng.spagobi.meta.model.physical.PhysicalColumn;
import it.eng.spagobi.meta.model.physical.PhysicalForeignKey;
import it.eng.spagobi.meta.model.physical.PhysicalModel;
import it.eng.spagobi.meta.model.physical.PhysicalPrimaryKey;
import it.eng.spagobi.meta.model.physical.PhysicalTable;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class MetaModelUpdater {

	/**
	 * Add tables to the original physical model from the physical model derived from CWM
	 * 
	 * @param originalModel
	 * @param modelFromCWM
	 */
	public PhysicalModel updateModelfromCWM(PhysicalModel originalModel, PhysicalModel modelFromCWM) {
		EList<PhysicalTable> originalTables = originalModel.getTables();
		EList<PhysicalTable> cwmTables = modelFromCWM.getTables();

		// 1 - Find new tables and columns not present in the original Model
		List<PhysicalTable> tablesToAdd = new ArrayList<PhysicalTable>();
		List<PhysicalForeignKey> foreignKeysToAdd = new ArrayList<PhysicalForeignKey>();
		for (PhysicalTable cwmTable : cwmTables) {
			String cwmTableName = cwmTable.getName();
			PhysicalTable tableFound = findTable(cwmTableName, originalTables);
			if (tableFound == null) {
				// New table to add to the original model

				tablesToAdd.add(cwmTable);
				// Save (if found) foreign keys of the new table
				foreignKeysToAdd.addAll(cwmTable.getForeignKeys());
				// Add also the primary keys related to this table
				PhysicalPrimaryKey primaryKey = cwmTable.getPrimaryKey();
				if (primaryKey != null) {
					originalModel.getPrimaryKeys().add(primaryKey);
				}

			}
		}

		// 2- Add new Tables
		originalModel.getTables().addAll(tablesToAdd);

		// 3- Add foreign keys for added tables
		addForeignKeysForAddedTables(foreignKeysToAdd, originalModel);

		return originalModel;
	}

	public PhysicalTable findTable(String tableName, EList<PhysicalTable> physicalTables) {

		for (PhysicalTable physicalTable : physicalTables) {
			if (physicalTable.getName().equals(tableName)) {
				return physicalTable;
			}
		}
		return null;
	}

	public void addForeignKeysForAddedTables(List<PhysicalForeignKey> physicalForeignKeys, PhysicalModel physicalModel) {
		EList<PhysicalTable> physicalTables = physicalModel.getTables();
		for (PhysicalForeignKey physicalForeignKey : physicalForeignKeys) {
			// check foreign keys consistency
			PhysicalTable sourceTable = physicalForeignKey.getSourceTable();
			PhysicalTable searchedSourceTable = findTable(sourceTable.getName(), physicalTables);
			if (searchedSourceTable != null) {

				PhysicalTable destinationTable = physicalForeignKey.getDestinationTable();
				PhysicalTable searchedDestinationTable = findTable(destinationTable.getName(), physicalTables);
				if (searchedDestinationTable != null) {
					// point to the corresponding source table and columns in the original model
					physicalForeignKey.setSourceTable(searchedSourceTable);

					EList<PhysicalColumn> sourceColumns = physicalForeignKey.getSourceColumns();
					List<PhysicalColumn> searchedSourceColumns = new ArrayList<PhysicalColumn>();
					for (PhysicalColumn sourceColumn : sourceColumns) {
						PhysicalColumn searchedColumn = findColumn(sourceColumn.getName(), searchedSourceTable.getColumns());
						searchedSourceColumns.add(searchedColumn);
					}
					physicalForeignKey.getSourceColumns().clear();
					physicalForeignKey.getSourceColumns().addAll(searchedSourceColumns);

					// point to the corresponding destination table and columns in the original model
					physicalForeignKey.setDestinationTable(searchedDestinationTable);

					EList<PhysicalColumn> destinationColumns = physicalForeignKey.getDestinationColumns();
					List<PhysicalColumn> searchedDestinationColumns = new ArrayList<PhysicalColumn>();
					for (PhysicalColumn destinationColumn : destinationColumns) {
						PhysicalColumn searchedColumn = findColumn(destinationColumn.getName(), searchedDestinationTable.getColumns());
						searchedDestinationColumns.add(searchedColumn);
					}

					physicalForeignKey.getDestinationColumns().clear();
					physicalForeignKey.getDestinationColumns().addAll(searchedDestinationColumns);

					// all right, add this fk to the model
					physicalModel.getForeignKeys().add(physicalForeignKey);

				} else {
					// skip, do not add this fk
					continue;
				}
			} else {
				// skip, do not add this fk
				continue;
			}
		}

	}

	public PhysicalColumn findColumn(String columnName, EList<PhysicalColumn> physicalColumns) {

		for (PhysicalColumn physicalColumn : physicalColumns) {
			if (physicalColumn.getName().equals(columnName)) {
				return physicalColumn;
			}
		}
		return null;
	}

}
