/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.

 **/
package it.eng.knowage.meta.initializer;

import it.eng.knowage.meta.initializer.properties.IPropertiesInitializer;
import it.eng.knowage.meta.initializer.properties.PhysicalModelPropertiesFromFileInitializer;
import it.eng.knowage.meta.model.Model;
import it.eng.knowage.meta.model.ModelObject;
import it.eng.knowage.meta.model.ModelProperty;
import it.eng.knowage.meta.model.business.BusinessRelationship;
import it.eng.knowage.meta.model.physical.PhysicalColumn;
import it.eng.knowage.meta.model.physical.PhysicalForeignKey;
import it.eng.knowage.meta.model.physical.PhysicalModel;
import it.eng.knowage.meta.model.physical.PhysicalModelFactory;
import it.eng.knowage.meta.model.physical.PhysicalPrimaryKey;
import it.eng.knowage.meta.model.physical.PhysicalTable;
import it.eng.knowage.meta.model.util.JDBCTypeMapper;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class PhysicalModelInitializer {

	IPropertiesInitializer propertiesInitializer;
	Model rootModel;
	private static Logger logger = LoggerFactory.getLogger(PhysicalModelInitializer.class);

	static public PhysicalModelFactory FACTORY = PhysicalModelFactory.eINSTANCE;
	static public String ORACLE_SPATIAL_GEOMETRY = "SDO_GEOMETRY";

	public PhysicalModelInitializer() {
		// setPropertiesInitializer(new PhysicalModelDefaultPropertiesInitializer());
		setPropertiesInitializer(new PhysicalModelPropertiesFromFileInitializer());

	}

	// Initialize PhysicalModel with table filter
	public PhysicalModel initialize(String modelName, Connection conn, String connectionName, String connectionDriver, String connectionUrl,
			String connectionUsername, String connectionPassword, String connectionDatabaseName, String defaultCatalog, String defaultSchema,
			List<String> selectedTables) {
		PhysicalModel model;
		DatabaseMetaData dbMeta;

		try {
			model = FACTORY.createPhysicalModel();
			model.setName(modelName);

			if (getRootModel() != null) {
				model.setParentModel(getRootModel());
			}

			dbMeta = conn.getMetaData();

			addDatabase(dbMeta, model);
			addCatalog(conn, model, defaultCatalog);
			addSchema(dbMeta, model, defaultSchema);

			addTables(dbMeta, model, selectedTables);

			for (int i = 0; i < model.getTables().size(); i++) {
				addPrimaryKey(dbMeta, model, model.getTables().get(i));
				addForeignKeys(dbMeta, model, model.getTables().get(i));
			}

			getPropertiesInitializer().addProperties(model);

			// Setting Connection properties values
			model.setProperty(PhysicalModelPropertiesFromFileInitializer.CONNECTION_NAME, connectionName);
			logger.debug("PhysicalModel Property: Connection name is [{}]",
					model.getProperties().get(PhysicalModelPropertiesFromFileInitializer.CONNECTION_NAME).getValue());

			model.setProperty(PhysicalModelPropertiesFromFileInitializer.CONNECTION_DRIVER, connectionDriver);
			logger.debug("PhysicalModel Property: Connection driver is [{}]",
					model.getProperties().get(PhysicalModelPropertiesFromFileInitializer.CONNECTION_DRIVER).getValue());

			model.setProperty(PhysicalModelPropertiesFromFileInitializer.CONNECTION_URL, connectionUrl);
			logger.debug("PhysicalModel Property: Connection url is [{}]", model.getProperties().get(PhysicalModelPropertiesFromFileInitializer.CONNECTION_URL)
					.getValue());

			model.setProperty(PhysicalModelPropertiesFromFileInitializer.CONNECTION_USERNAME, connectionUsername);
			logger.debug("PhysicalModel Property: Connection username is [{}]",
					model.getProperties().get(PhysicalModelPropertiesFromFileInitializer.CONNECTION_USERNAME).getValue());

			model.setProperty(PhysicalModelPropertiesFromFileInitializer.CONNECTION_PASSWORD, connectionPassword);
			logger.debug("PhysicalModel Property: Connection password is [{}]",
					model.getProperties().get(PhysicalModelPropertiesFromFileInitializer.CONNECTION_PASSWORD).getValue());

			model.setProperty(PhysicalModelPropertiesFromFileInitializer.CONNECTION_DATABASENAME, connectionDatabaseName);
			logger.debug("PhysicalModel Property: Connection databasename is [{}]",
					model.getProperties().get(PhysicalModelPropertiesFromFileInitializer.CONNECTION_DATABASENAME).getValue());

			// Quote string identification
			String quote = dbMeta.getIdentifierQuoteString();
			// check if escaping is needed
			if (quote.equals("\"")) {
				quote = "\\\"";
			}
			if (quote.equals(" ")) {
				quote = "";
			}

			model.setProperty(PhysicalModelPropertiesFromFileInitializer.CONNECTION_DATABASE_QUOTESTRING, quote);
			logger.debug("PhysicalModel Property: Connection databasequotestring is [{}]",
					model.getProperties().get(PhysicalModelPropertiesFromFileInitializer.CONNECTION_DATABASE_QUOTESTRING).getValue());

			/*
			 * model.getPropertyType("connection.name").setDefaultValue(connectionName); logger.debug("PhysicalModel Property: Connection name is [{}]",
			 * model.getPropertyType("connection.name").getDefaultValue());
			 * 
			 * model.getPropertyType("connection.driver").setDefaultValue(connectionDriver); logger.debug("PhysicalModel Property: Connection driver is [{}]",
			 * model.getPropertyType("connection.driver").getDefaultValue());
			 * 
			 * model.getPropertyType("connection.url").setDefaultValue(connectionUrl); logger.debug("PhysicalModel Property: Connection url is [{}]",
			 * model.getPropertyType("connection.url").getDefaultValue());
			 * 
			 * model.getPropertyType("connection.username").setDefaultValue(connectionUsername);
			 * logger.debug("PhysicalModel Property: Connection username is [{}]", model.getPropertyType("connection.username").getDefaultValue());
			 * 
			 * model.getPropertyType("connection.password").setDefaultValue(connectionPassword);
			 * logger.debug("PhysicalModel Property: Connection password is [{}]", model.getPropertyType("connection.password").getDefaultValue());
			 * 
			 * model.getPropertyType("connection.databasename").setDefaultValue(connectionDatabaseName);
			 * logger.debug("PhysicalModel Property: Connection databasename is [{}]", model.getPropertyType("connection.databasename").getDefaultValue());
			 * 
			 * // Quote string identification String quote = dbMeta.getIdentifierQuoteString(); // check if escaping is needed if (quote.equals("\"")) { quote =
			 * "\\\""; } model.getPropertyType("connection.databasequotestring").setDefaultValue(quote);
			 * logger.debug("PhysicalModel Property: Connection databasequotestring is [{}]", model.getPropertyType("connection.databasequotestring")
			 * .getDefaultValue());
			 */
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize physical model", t);
		}

		return model;

	}

	// Initialize Physical Model with ALL original Database Tables
	public PhysicalModel initialize(String modelName, Connection conn, String connectionName, String connectionDriver, String connectionUrl,
			String connectionUsername, String connectionPassword, String connectionDatabaseName, String defaultCatalog, String defaultSchema) {
		return initialize(modelName, conn, connectionName, connectionDriver, connectionUrl, connectionUsername, connectionPassword, connectionDatabaseName,
				defaultCatalog, defaultSchema, null);
	}

	private void addDatabase(DatabaseMetaData dbMeta, PhysicalModel model) {
		try {
			model.setDatabaseName(dbMeta.getDatabaseProductName());
			model.setDatabaseVersion(dbMeta.getDatabaseProductVersion());
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize database metadata", t);
		}
	}

	private void addCatalog(Connection conn, PhysicalModel model, String defaultCatalog) {
		String catalog;
		List<String> catalogs;
		DatabaseMetaData dbMeta;
		ResultSet rs;
		Iterator<String> it;

		catalog = null;

		try {

			catalog = conn.getCatalog();
			if (catalog == null) {
				dbMeta = conn.getMetaData();

				rs = dbMeta.getCatalogs();
				catalogs = new ArrayList();
				while (rs.next()) {
					String catalogName = rs.getString(1);
					if (catalogName != null) {
						catalogs.add(catalogName);
					}
				}
				if (catalogs.size() == 0) {
					log("No schema [" + dbMeta.getSchemaTerm() + "] defined");
				} else if (catalogs.size() == 1) {
					catalog = catalogs.get(0);
				} else {
					String targetCatalog = null;
					it = catalogs.iterator();
					while (it.hasNext()) {
						String s = it.next();
						log("s [" + s + "]");
						if (s.equalsIgnoreCase(defaultCatalog)) {
							targetCatalog = defaultCatalog;
							break;
						}
					}
					if (targetCatalog == null) {
						throw new RuntimeException("No catalog named [" + defaultCatalog + "] is available on db");
					}
					catalog = targetCatalog;
				}
				rs.close();
			}

			model.setCatalog(catalog);
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize catalog metadata", t);
		}
	}

	private void addSchema(DatabaseMetaData dbMeta, PhysicalModel model, String defaultSchema) {
		String schema;
		List<String> schemas;
		ResultSet rs;
		Iterator<String> it;

		schema = null;

		try {
			rs = dbMeta.getSchemas();
			schemas = new ArrayList();
			while (rs.next()) {
				String schemaName = rs.getString(1);
				if (schemaName != null) {
					schemas.add(rs.getString(1));
				}
			}

			if (schemas.size() == 0) {
				log("No schema [" + dbMeta.getSchemaTerm() + "] defined");
			} else if (schemas.size() == 1) {
				schema = schemas.get(0);
			} else {
				String targetSchema = null;
				it = schemas.iterator();
				while (it.hasNext()) {
					String s = it.next();
					if (s.equalsIgnoreCase(defaultSchema)) {
						targetSchema = defaultSchema;
						break;
					}
				}
				if (targetSchema == null) {
					throw new RuntimeException("No schema named [" + defaultSchema + "] is available on db");
				}
				schema = targetSchema;
			}
			rs.close();
			model.setSchema(schema);
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize schema metadata", t);
		}
	}

	private void addTables(DatabaseMetaData dbMeta, PhysicalModel model, List<String> selectedTables) {
		boolean filterTable = false;
		if (selectedTables != null) {
			filterTable = true;
		}

		Map<String, PhysicalTable> tabesLookupMap;
		ResultSet tableRs, columnRs;
		PhysicalTable table;
		List<PhysicalPrimaryKey> primaryKeys;
		PhysicalPrimaryKey primaryKey;
		List<PhysicalForeignKey> foreignKeys;
		PhysicalForeignKey foreignKey;

		try {
			tableRs = dbMeta.getTables(model.getCatalog(), model.getSchema(), null, new String[] { "TABLE", "VIEW" });

			/*
			 * -------------------------------------------------- resultset's structure -------------------------------------------------- 1. TABLE_CAT String
			 * => table catalog (may be null) 2. TABLE_SCHEM String => table schema (may be null) 3. TABLE_NAME String => table name
			 * 
			 * Data Warehouse Management Model 181 4. TABLE_TYPE String => table type. Typical types are �TABLE�, �VIEW�, �SYSTEM TABLE�,�GLOBAL TEMPORARY�,
			 * �LOCAL TEMPORARY�, �ALIAS�, �SYNONYM�. 5. REMARKS String => explanatory comment on the table 6. TYPE_CAT String => the types catalog (may be
			 * null) 7. TYPE_SCHEM String => the types schema (may be null) 8. TYPE_NAME String => type name (may be null) 9. SELF_REFERENCING_COL_NAME String
			 * => name of the designated �identifier� column of a typed table (may be null) 10. REF_GENERATION String => specifies how values in
			 * SELF_REFERENCING_COL_NAME are created. Values are �SYSTEM�, �USER�, �DERIVED�. (may be null)
			 */
			while (tableRs.next()) {
				if ((!filterTable) || ((selectedTables != null) && (selectedTables.contains(tableRs.getString("TABLE_NAME"))))) {
					table = FACTORY.createPhysicalTable();
					table.setModel(model);

					table.setName(tableRs.getString("TABLE_NAME"));
					table.setComment(getEscapedMetadataPropertyValue(tableRs, "REMARKS"));
					table.setType(tableRs.getString("TABLE_TYPE"));

					log("Table: " + table.getName() + "[" + table.getType() + "]");

					initColumnsMeta(dbMeta, model, table);

					model.getTables().add(table);
					getPropertiesInitializer().addProperties(table);
				}
			}
			tableRs.close();

		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize tables metadata", t);
		}
	}

	private void initColumnsMeta(DatabaseMetaData dbMeta, PhysicalModel model, PhysicalTable table) {
		ResultSet rs;
		PhysicalColumn column;

		try {
			rs = dbMeta.getColumns(model.getCatalog(), model.getSchema(), table.getName(), null);

			/*
			 * 1. TABLE_CAT String => table catalog (may be null) 2. TABLE_SCHEM String => table schema (may be null) 3. TABLE_NAME String => table name 4.
			 * COLUMN_NAME String => column name 5. DATA_TYPE short => SQL type from java.sql.Types 6. TYPE_NAME String => Data source dependent type name, for
			 * a UDT the type name is fully qualified 7. COLUMN_SIZE int => column size. For char or date types this is the maximum number of characters, for
			 * numeric or decimal types this is precision. 8. BUFFER_LENGTH is not used. 9. DECIMAL_DIGITS int => the number of fractional digits 10.
			 * NUM_PREC_RADIX int => Radix (typically either 10 or 2) 11. NULLABLE int => is NULL allowed. columnNoNulls - might not allow NULL values;
			 * columnNullable - definitely allows NULL values; columnNullableUnknown - nullability unknown 12. REMARKS String => comment describing column (may
			 * be null) 13. COLUMN_DEF String => default value (may be null) 14. SQL_DATA_TYPE int => unused 15. SQL_DATETIME_SUB int => unused 16.
			 * CHAR_OCTET_LENGTH int => for char types the maximum number of bytes in the column 17. ORDINAL_POSITION int => index of column in table (starting
			 * at 1) 18. IS_NULLABLE String => �NO� means column definitely does not allow NULL values; �YES� means the column might allow NULL values. An empty
			 * string means nobody knows. 19. SCOPE_CATLOG String => catalog of table that is the scope of a reference attribute (null if DATA_TYPE isn�t REF)
			 * 20. SCOPE_SCHEMA String => schema of table that is the scope of a
			 * 
			 * 182 Chapter 5 reference attribute (null if the DATA_TYPE isn�t REF) 21. SCOPE_TABLE String => table name that is the scope of a reference
			 * attribute (null if the DATA_TYPE isn�t REF) 22. SOURCE_DATA_TYPE short => source type of a distinct type or user-generated Ref type, SQL type
			 * from java.sql.Types (null if DATA_TYPE isn�t DISTINCT or user-generated REF)
			 */
			while (rs.next()) {
				column = FACTORY.createPhysicalColumn();

				// to prevent ojdbc bug
				try {
					column.setDefaultValue(rs.getString("COLUMN_DEF"));
				} catch (Throwable t) {
					log("Impossible to set Default column value");
					t.printStackTrace();
					column.setDefaultValue(null);
				}
				column.setName(rs.getString("COLUMN_NAME"));

				column.setComment(getEscapedMetadataPropertyValue(rs, "REMARKS"));

				column.setDataType(JDBCTypeMapper.getModelType(rs.getShort("DATA_TYPE")));
				String columnTypeName = rs.getString("TYPE_NAME");
				// Check if it's a geospatial column
				if (columnTypeName.equalsIgnoreCase(ORACLE_SPATIAL_GEOMETRY)) {
					// overwrite the DataType from "OTHER" to our custom type "GEOMETRY"
					column.setDataType("GEOMETRY");
				} else if (columnTypeName.contains("TIMESTAMP")) {
					column.setDataType("TIMESTAMP");
				}
				column.setTypeName(columnTypeName);
				column.setSize(rs.getInt("COLUMN_SIZE"));
				column.setOctectLength(rs.getInt("CHAR_OCTET_LENGTH"));
				column.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
				column.setRadix(rs.getInt("NUM_PREC_RADIX"));
				// column.setDefaultValue( rs.getString("COLUMN_DEF") );
				column.setNullable(!"NO".equalsIgnoreCase(rs.getString("IS_NULLABLE")));
				column.setPosition(rs.getInt("ORDINAL_POSITION"));

				table.getColumns().add(column);
				log("  - column: " + column.getName() + " [" + column.getTypeName() + "]" + " [" + column.getDefaultValue() + "]");
				getPropertiesInitializer().addProperties(column);

			}
			rs.close();
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize primaryKeys metadata", t);
		}
	}

	private String getEscapedMetadataPropertyValue(ResultSet rs, String propertyName) throws SQLException {
		String propertyValue;

		propertyValue = rs.getString(propertyName);
		if (propertyValue != null) {
			propertyValue = StringEscapeUtils.escapeXml(propertyValue);

			Pattern pattern = Pattern.compile("[\\000]*");
			Matcher matcher = pattern.matcher(propertyValue);
			if (matcher.find()) {
				propertyValue = matcher.replaceAll("");
			}
		}
		return propertyValue;
	}

	private void addPrimaryKey(DatabaseMetaData dbMeta, PhysicalModel model, PhysicalTable table) {
		PhysicalColumn column;
		PhysicalPrimaryKey primaryKey;
		ResultSet rs;

		primaryKey = null;

		try {
			rs = dbMeta.getPrimaryKeys(model.getCatalog(), model.getSchema(), table.getName());
			/*
			 * 1. TABLE_CAT String => table catalog (may be null) 2. TABLE_SCHEM String => table schema (may be null) 3. TABLE_NAME String => table name 4.
			 * COLUMN_NAME String => column name 5. KEY_SEQ short => sequence number within primary key 6. PK_NAME String => primary key name (may be null)
			 */

			while (rs.next()) {
				if (primaryKey == null) {
					primaryKey = FACTORY.createPhysicalPrimaryKey();
					primaryKey.setName(rs.getString("PK_NAME"));

					primaryKey.setTable(table);
					model.getPrimaryKeys().add(primaryKey);

					getPropertiesInitializer().addProperties(primaryKey);
				}

				column = table.getColumn(rs.getString("COLUMN_NAME"));
				if (column != null) {
					primaryKey.getColumns().add(column);
				}

			}
			rs.close();

		} catch (Throwable t) {
			throw new RuntimeException("Impossible to retrive primaryKeys metadata", t);
		}
	}

	private void addForeignKeys(DatabaseMetaData dbMeta, PhysicalModel model, PhysicalTable table) {
		List<PhysicalForeignKey> foreignKeys;
		ResultSet rs;
		PhysicalForeignKey foreignKey;

		foreignKeys = new ArrayList();
		foreignKey = null;

		try {
			rs = dbMeta.getImportedKeys(model.getCatalog(), model.getSchema(), table.getName());
			/*
			 * 1. PKTABLE_CAT String => primary key table catalog (may be null) 2. PKTABLE_SCHEM String => primary key table schema (may be null) 3.
			 * PKTABLE_NAME String => primary key table name 4. PKCOLUMN_NAME String => primary key column name 5. FKTABLE_CAT String => foreign key table
			 * catalog (may be null) being exported (may be null) 6. FKTABLE_SCHEM String => foreign key table schema (may be null) being exported (may be null)
			 * 7. FKTABLE_NAME String => foreign key table name being exported 8. FKCOLUMN_NAME String => foreign key column name being exported 9. KEY_SEQ
			 * short => sequence number within foreign key 10. UPDATE_RULE short => What happens to foreign key when primary is updated: importedNoAction - do
			 * not allow update of primary key if it has been imported importedKeyCascade - change imported key to agree with primary key update
			 * importedKeySetNull - change imported key to NULL if its primary key has been updated importedKeySetDefault - change imported key to default
			 * values if its primary key has been updated importedKeyRestrict - same as importedKeyNoAction (for ODBC 2.x compatibility) 11. DELETE_RULE short
			 * => What happens to the foreign key when primary is deleted: importedKeyNoAction - do not allow delete of primary key if it has been imported
			 * importedKeyCascade - delete rows that import a deleted key importedKeySetNull - change imported key to NULL if its primary key has been deleted
			 * importedKeyRestrict - same as importedKeyNoAction (for ODBC 2.x compatibility) importedKeySetDefault - change imported key to default if its
			 * primary key has been deleted 12. FK_NAME String => foreign key name (may be null) 13. PK_NAME String => primary key name (may be null) 14.
			 * DEFERRABILITY short => can the evaluation of foreign key constraints be deferred until commit: importedKeyInitiallyDeferred - see SQL92 for
			 * definition importedKeyInitiallyImmediate - see SQL92 for definition importedKeyNotDeferrable - see SQL92 for definition
			 */
			String fkName = null;
			PhysicalTable sourceTable = null;
			PhysicalTable destinationTable = null;
			String pkName = null;
			while (rs.next()) {
				fkName = rs.getString("FK_NAME");
				sourceTable = model.getTable(rs.getString("FKTABLE_NAME"));
				destinationTable = model.getTable(rs.getString("PKTABLE_NAME"));

				if (destinationTable == null || sourceTable == null) {
					// skip this foreign key because table is not found in the
					// physical model
					log("Foreign Key skipped because table was not found in the physical model");
				} else {
					if (foreignKey == null) { // OK it's the first iteration

						foreignKey = FACTORY.createPhysicalForeignKey();
						getPropertiesInitializer().addProperties(foreignKey);

						foreignKey.setName(fkName);
						foreignKey.setSourceName(fkName);
						foreignKey.setSourceTable(sourceTable);
						foreignKey.setDestinationName(rs.getString("PK_NAME"));
						foreignKey.setDestinationTable(destinationTable);

						pkName = rs.getString("PK_NAME");

					} else if (!foreignKey.getSourceName().equals(fkName)) { // we
																				// have
																				// finished
																				// with
																				// the
																				// previous
																				// fk

						// table.getForeignKeys().add(foreignKey);
						model.getForeignKeys().add(foreignKey);
						foreignKey = FACTORY.createPhysicalForeignKey();
						getPropertiesInitializer().addProperties(foreignKey);
						foreignKey.setName(fkName);
						foreignKey.setSourceName(fkName);
						foreignKey.setSourceTable(sourceTable);
						foreignKey.setDestinationName(rs.getString("PK_NAME"));
						foreignKey.setDestinationTable(destinationTable);

						pkName = rs.getString("PK_NAME");
					}

					PhysicalColumn c = sourceTable.getColumn(rs.getString("FKCOLUMN_NAME"));
					foreignKey.getSourceColumns().add(sourceTable.getColumn(rs.getString("FKCOLUMN_NAME")));

					c = destinationTable.getColumn(rs.getString("PKCOLUMN_NAME"));
					foreignKey.getDestinationColumns().add(destinationTable.getColumn(rs.getString("PKCOLUMN_NAME")));
				}

			}
			// add the last or the only foreign key found
			if (foreignKey != null) {
				if (destinationTable == null || sourceTable == null) {
					// skip this foreign key because table was not found in the
					// physical model
					log("Foreign Key skipped because table was not found in the physical model");
				} else {
					model.getForeignKeys().add(foreignKey);
					foreignKey = FACTORY.createPhysicalForeignKey();
					getPropertiesInitializer().addProperties(foreignKey);
					foreignKey.setName(fkName);
					foreignKey.setSourceName(fkName);
					foreignKey.setSourceTable(sourceTable);
					foreignKey.setDestinationName(pkName);
					foreignKey.setDestinationTable(destinationTable);
				}
			}

			rs.close();
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to initialize foreignKeys metadata", t);
		}
	}

	// ---------------------------------------------------------
	// Physical Model Update
	// ---------------------------------------------------------
	/**
	 * Get tables names that are present in the database but not in the passed physical model
	 *
	 * @param connection
	 *            jdbc connection to the database
	 * @param physicalModel
	 *            physical model to check
	 */
	public List<String> getMissingTablesNames(Connection connection, PhysicalModel physicalModel) {
		try {
			DatabaseMetaData dbMeta = connection.getMetaData();

			List<String> tablesOnDatabase = new ArrayList<String>();
			ResultSet tableRs = dbMeta.getTables(physicalModel.getCatalog(), physicalModel.getSchema(), null, new String[] { "TABLE", "VIEW" });

			while (tableRs.next()) {
				String tableName = tableRs.getString("TABLE_NAME");
				tablesOnDatabase.add(tableName);
			}
			tableRs.close();

			EList<PhysicalTable> originalTables = physicalModel.getTables();
			Iterator<String> tablesIterator = tablesOnDatabase.iterator();
			while (tablesIterator.hasNext()) {
				String tableName = tablesIterator.next();
				if (findTable(tableName, originalTables) != null) {
					// already present, remove table name from the list of tables that can be imported
					tablesIterator.remove();
				}
			}
			return tablesOnDatabase;

		} catch (SQLException e) {
			throw new RuntimeException("Physical Model - Impossible to get missing tables names", e);

		}

	}

	/**
	 * Get columns names that are present in the database but not in the passed physical model
	 *
	 * @param connection
	 *            jdbc connection to the database
	 * @param physicalModel
	 *            physical model to check
	 */
	public List<String> getMissingColumnsNames(Connection connection, PhysicalModel physicalModel) {
		try {
			DatabaseMetaData dbMeta = connection.getMetaData();

			List<String> tablesOnDatabase = new ArrayList<String>();
			List<String> newColumnsNames = new ArrayList<String>();
			ResultSet tableRs = dbMeta.getTables(physicalModel.getCatalog(), physicalModel.getSchema(), null, new String[] { "TABLE", "VIEW" });

			while (tableRs.next()) {
				String tableName = tableRs.getString("TABLE_NAME");
				tablesOnDatabase.add(tableName);
			}
			tableRs.close();

			EList<PhysicalTable> originalTables = physicalModel.getTables();
			Iterator<String> tablesIterator = tablesOnDatabase.iterator();

			// iterate for each table
			while (tablesIterator.hasNext()) {
				String tableName = tablesIterator.next();
				PhysicalTable physicalTable = findTable(tableName, originalTables);
				if (physicalTable != null) {
					ResultSet rs = dbMeta.getColumns(physicalModel.getCatalog(), physicalModel.getSchema(), physicalTable.getName(), null);
					while (rs.next()) {
						String columnName = rs.getString("COLUMN_NAME");
						// check if the column exists in the physicalModel
						PhysicalColumn physicalColumn = findColumn(columnName, physicalTable.getColumns());
						if (physicalColumn == null) {
							// new column on database
							newColumnsNames.add(tableName + "." + columnName);
						}
					}

				}
			}
			return newColumnsNames;

		} catch (SQLException e) {
			throw new RuntimeException("Physical Model - Impossible to get missing tables names", e);

		}
	}

	/**
	 * Get tables and columns names that are present in the database but not in the passed physical model
	 *
	 * @param connection
	 *            jdbc connection to the database
	 * @param physicalModel
	 *            physical model to check
	 */
	public List<String> getRemovedTablesAndColumnsNames(Connection connection, PhysicalModel physicalModel) {
		try {
			DatabaseMetaData dbMeta = connection.getMetaData();

			List<String> tablesOnDatabase = new ArrayList<String>();
			List<String> tablesRemoved = new ArrayList<String>();
			List<String> columnsRemoved = new ArrayList<String>();

			ResultSet tableRs = dbMeta.getTables(physicalModel.getCatalog(), physicalModel.getSchema(), null, new String[] { "TABLE", "VIEW" });

			while (tableRs.next()) {
				String tableName = tableRs.getString("TABLE_NAME");
				tablesOnDatabase.add(tableName);
			}
			tableRs.close();

			EList<PhysicalTable> originalTables = physicalModel.getTables();

			Iterator<PhysicalTable> physicalTablesIterator = originalTables.iterator();
			// 1- Check table existence
			while (physicalTablesIterator.hasNext()) {
				PhysicalTable originalTable = physicalTablesIterator.next();
				String tableToFind = originalTable.getName();
				if (!tablesOnDatabase.contains(tableToFind)) {
					// tables not found on database -> removed
					tablesRemoved.add(tableToFind);
				} else {
					// 2 - Check columns existence
					List<PhysicalColumn> physicalColumns = originalTable.getColumns();
					ResultSet rs = dbMeta.getColumns(physicalModel.getCatalog(), physicalModel.getSchema(), originalTable.getName(), null);
					List<String> columnsNamesOnDb = new ArrayList<String>();
					while (rs.next()) {
						String columnName = rs.getString("COLUMN_NAME");
						columnsNamesOnDb.add(columnName);
					}
					for (PhysicalColumn physicalColumn : physicalColumns) {
						if (!columnsNamesOnDb.contains(physicalColumn.getName())) {
							// column not found on database -> removed
							columnsRemoved.add(originalTable.getName() + "." + physicalColumn.getName());
						}
					}

				}
			}

			// merge two list
			tablesRemoved.addAll(columnsRemoved);

			return tablesRemoved;

		} catch (SQLException e) {
			throw new RuntimeException("Physical Model - Impossible to get missing tables names", e);

		}
	}

	/**
	 * Update originaModel with new tables and columns from updateModel, also mark as deleted the tables or columns not found in the updated model
	 *
	 */
	public PhysicalModel updateModel(PhysicalModel originalModel, PhysicalModel updatedModel, List<String> selectedTables) {
		EList<PhysicalTable> originalTables = originalModel.getTables();
		EList<PhysicalTable> updatedTables = updatedModel.getTables();

		// 1 - Find new tables and columns not present in the original Model
		List<PhysicalTable> tablesToAdd = new ArrayList<PhysicalTable>();
		List<PhysicalForeignKey> foreignKeysToAdd = new ArrayList<PhysicalForeignKey>();
		for (PhysicalTable updatedTable : updatedTables) {
			String updatedTableName = updatedTable.getName();
			PhysicalTable tableFound = findTable(updatedTableName, originalTables);
			if (tableFound == null) {
				// New table to add to the original model

				// Check also if the table was selected to be imported
				if (selectedTables.contains(updatedTableName)) {
					tablesToAdd.add(updatedTable);
					// Save (if found) foreign keys of the new table
					foreignKeysToAdd.addAll(updatedTable.getForeignKeys());
					// Add also the primary keys related to this table
					PhysicalPrimaryKey primaryKey = updatedTable.getPrimaryKey();
					if (primaryKey != null) {
						originalModel.getPrimaryKeys().add(primaryKey);
					}
				}
			} else {
				// Table already present in the original model

				// perform a compare of the columns of this table
				// in the two models
				updateTable(tableFound, updatedTable);

			}
		}

		// 2 - Find deleted tables and columns, namely not present in the updatedModel
		for (PhysicalTable originalTable : originalTables) {
			PhysicalTable tableFound = findTable(originalTable.getName(), updatedTables);
			if (tableFound == null) {
				// The table as to be marked as deleted in the original model
				setProperty(originalTable, PhysicalModelPropertiesFromFileInitializer.IS_DELETED, "true");

				// mark also all of its columns as deleted
				markAllColumnsAsDeleted(originalTable);
			}

		}

		// 3- Add new Tables
		originalModel.getTables().addAll(tablesToAdd);

		// 4- Add foreign keys for added tables
		addForeignKeysForAddedTables(foreignKeysToAdd, originalModel);

		// 5 - Check Foreign Keys for updated tables
		for (PhysicalTable updatedTable : updatedTables) {
			String updatedTableName = updatedTable.getName();
			PhysicalTable tableFound = findTable(updatedTableName, originalTables);
			if (tableFound != null) {
				checkForeignKeys(tableFound, updatedTable);
			}
		}

		return originalModel;
	}

	public void markAllColumnsAsDeleted(PhysicalTable physicalTable) {
		EList<PhysicalColumn> physicalColumns = physicalTable.getColumns();
		Iterator<PhysicalColumn> iterator = physicalColumns.iterator();
		while (iterator.hasNext()) {
			PhysicalColumn physicalColumn = iterator.next();
			if (physicalColumn.getProperties().get(PhysicalModelPropertiesFromFileInitializer.IS_DELETED) != null) {
				physicalColumn.getProperties().get(PhysicalModelPropertiesFromFileInitializer.IS_DELETED).setValue("true");
			}
		}
	}

	public void addForeignKeysForAddedTables(List<PhysicalForeignKey> physicalForeignKeys, PhysicalModel physicalModel) {
		EList<PhysicalTable> physicalTables = physicalModel.getTables();
		for (PhysicalForeignKey physicalForeignKey : physicalForeignKeys) {
			// check foreign keys consistency
			PhysicalTable sourceTable = physicalForeignKey.getSourceTable();
			PhysicalTable searchedSourceTable = findTable(sourceTable.getName(), physicalTables);
			if (searchedSourceTable != null) {
				// check also if the columns are present and not marked deleted
				if (!searchedSourceTable.containsAllNotDeleted(physicalForeignKey.getSourceColumns())) {
					continue;
				}

				PhysicalTable destinationTable = physicalForeignKey.getDestinationTable();
				PhysicalTable searchedDestinationTable = findTable(destinationTable.getName(), physicalTables);
				if (searchedDestinationTable != null) {
					// check also if the columns are present and not marked deleted
					if (!searchedDestinationTable.containsAllNotDeleted(physicalForeignKey.getDestinationColumns())) {
						continue;
					} else {
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
					}

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

	/**
	 * Update originalTable with new columns information found in the updatedTable
	 *
	 * @param originalTable
	 *            table to update
	 * @param updatedTable
	 *            table used to extract new informations
	 */
	public PhysicalTable updateTable(PhysicalTable originalTable, PhysicalTable updatedTable) {
		EList<PhysicalColumn> originalColumns = originalTable.getColumns();
		EList<PhysicalColumn> updatedColumns = updatedTable.getColumns();

		// 1 - Find new columns not present in the originalTable
		List<PhysicalColumn> columnsToAdd = new ArrayList<PhysicalColumn>();
		for (PhysicalColumn updatedColumn : updatedColumns) {
			PhysicalColumn columnFound = findColumn(updatedColumn.getName(), originalColumns);
			if (columnFound == null) {
				// New column to add to the original table
				columnsToAdd.add(updatedColumn);
			} else {
				// Column already present

				// check if the column has changed type and primary keys
				updateColumn(columnFound, updatedColumn);

			}

		}

		// 2- Find deleted columns, namely not present in the updatedTable
		for (PhysicalColumn originalColumn : originalColumns) {
			PhysicalColumn columnFound = findColumn(originalColumn.getName(), updatedColumns);
			if (columnFound == null) {
				// Column as to be marked as deleted in the original model
				setProperty(originalColumn, PhysicalModelPropertiesFromFileInitializer.IS_DELETED, "true");
			}

		}

		// Add new columns
		// check also if new columns are part of Primary Key
		for (PhysicalColumn columnToAdd : columnsToAdd) {
			addPhysicalColumn(originalTable, columnToAdd);
		}

		return originalTable;

	}

	/**
	 * Add a column to the passed physical table, also check if the column is a primary key
	 */
	private void addPhysicalColumn(PhysicalTable physicalTable, PhysicalColumn updatedPhysicalColumn) {
		boolean isPrimaryKey = updatedPhysicalColumn.isPrimaryKey();
		physicalTable.getColumns().add(updatedPhysicalColumn);

		PhysicalPrimaryKey primaryKey = physicalTable.getPrimaryKey();

		if (isPrimaryKey) {
			if (primaryKey != null) {
				// update
				primaryKey.getColumns().add(updatedPhysicalColumn);
			} else {
				// create new PK

				PhysicalPrimaryKey newPrimaryKey = FACTORY.createPhysicalPrimaryKey();
				String pkName = updatedPhysicalColumn.getTable().getPrimaryKey().getName();
				newPrimaryKey.setName(pkName);

				newPrimaryKey.setTable(physicalTable);
				PhysicalModel originalPhysicalModel = physicalTable.getModel();
				originalPhysicalModel.getPrimaryKeys().add(newPrimaryKey);

				getPropertiesInitializer().addProperties(newPrimaryKey);

				newPrimaryKey.getColumns().add(updatedPhysicalColumn);

			}
		}

	}

	/**
	 * Check foreign keys of the originalTable using informations from the updatedTable
	 *
	 * @param originalTable
	 * @param updatedTable
	 */
	private void checkForeignKeys(PhysicalTable originalTable, PhysicalTable updatedTable) {
		List<PhysicalForeignKey> originalPhysicalForeignKeys = originalTable.getForeignKeys();
		List<PhysicalForeignKey> updatedPhysicalForeignKeys = updatedTable.getForeignKeys();
		List<PhysicalForeignKey> foreignKeysToAdd = new ArrayList<PhysicalForeignKey>();
		foreignKeysToAdd.addAll(updatedPhysicalForeignKeys);

		Iterator<PhysicalForeignKey> iterator = originalPhysicalForeignKeys.iterator();
		while (iterator.hasNext()) {
			PhysicalForeignKey originalPhysicalForeignKey = iterator.next();
			// check if the foreign key still exists
			boolean fkFound = false;
			for (PhysicalForeignKey updatedPhysicalForeignKey : updatedPhysicalForeignKeys) {
				if (updatedPhysicalForeignKey.getSourceName().equals(originalPhysicalForeignKey.getSourceName())) {
					// remove from the list of foreign keys to add
					foreignKeysToAdd.remove(updatedPhysicalForeignKey);
					// UPDATE FK
					// check if the fk has changed
					updateForeignKey(originalPhysicalForeignKey, updatedPhysicalForeignKey);
					fkFound = true;

				}
			}
			if (!fkFound) {
				// REMOVE FK
				// fk not found in the updated model, the foreign key was removed from the db
				removePhysicalForeignKey(originalTable.getModel(), originalPhysicalForeignKey);
			}

		}

		// add new Foreign Keys found in the updated model
		// we need to create new foreign keys using information from the updated model
		for (PhysicalForeignKey updatedPhysicalForeignKey : foreignKeysToAdd) {
			// ADD FK
			addForeignKey(originalTable.getModel(), updatedPhysicalForeignKey);
		}

	}

	/**
	 * Update (if necessary) the originalPhysicalForeignKey with the information retrieved from the updatedPhysicalForeignKey
	 *
	 * Important: We skip the check of source tables because we call this method only in the checkForeignKeys() method
	 *
	 * @param originalPhysicalForeignKey
	 * @param updatedPhysicalForeignKey
	 */
	private void updateForeignKey(PhysicalForeignKey originalPhysicalForeignKey, PhysicalForeignKey updatedPhysicalForeignKey) {
		PhysicalTable originalSourceTable = originalPhysicalForeignKey.getSourceTable();
		PhysicalTable originalDestinationTable = originalPhysicalForeignKey.getDestinationTable();

		PhysicalModel originalPhysicalModel = originalSourceTable.getModel();

		PhysicalTable updatedDestinationTable = updatedPhysicalForeignKey.getDestinationTable();

		// We skip the source tables because we call this method only in the checkForeignKeys() method
		// so we assume that the source table is the same

		// Check source Columns
		List<PhysicalColumn> updatedSourceColumns = updatedPhysicalForeignKey.getSourceColumns();
		boolean resultSourceCheck = checkSourceForeignKeysColumns(originalPhysicalForeignKey, updatedSourceColumns);
		if (resultSourceCheck) {
			// Check Destination Tables
			if (originalDestinationTable.getName().equals(updatedDestinationTable.getName())) {
				// check destination columns
				List<PhysicalColumn> updatedDestinationColumns = updatedPhysicalForeignKey.getDestinationColumns();
				boolean resultDestinationCheck = checkDestinationForeignKeyColumns(originalPhysicalForeignKey, updatedDestinationColumns);
				if (!resultDestinationCheck) {
					// remove whole fk for missing columns in the original model
					removePhysicalForeignKey(originalPhysicalModel, originalPhysicalForeignKey);

				}
			} else {
				// different destination tables
				PhysicalTable newDestinationPhysicalTable = originalPhysicalModel.getTable(updatedDestinationTable.getName());
				if (newDestinationPhysicalTable != null) {
					// change the destination table
					originalPhysicalForeignKey.setDestinationTable(newDestinationPhysicalTable);
					// change the destination columns in the original fk
					List<PhysicalColumn> updatedDestinationColumns = updatedPhysicalForeignKey.getDestinationColumns();
					// check if all the destination columns are present in the original model, if false remove the whole fk
					boolean resultDestinationCheck = checkDestinationForeignKeyColumns(originalPhysicalForeignKey, updatedDestinationColumns);
					if (!resultDestinationCheck) {
						// remove whole fk for missing columns in the original model
						removePhysicalForeignKey(originalPhysicalModel, originalPhysicalForeignKey);
					}
				} else {
					// destination table not found, remove the whole fk
					removePhysicalForeignKey(originalPhysicalModel, originalPhysicalForeignKey);
				}
			}
		} else {
			// Cannot find a column of the FK so we remove the whole FK
			removePhysicalForeignKey(originalPhysicalModel, originalPhysicalForeignKey);
		}

	}

	private boolean checkSourceForeignKeysColumns(PhysicalForeignKey originalPhysicalForeignKey, List<PhysicalColumn> updatedSourceColumns) {
		PhysicalTable originalSourceTable = originalPhysicalForeignKey.getSourceTable();

		// remove all the source columns
		originalPhysicalForeignKey.getSourceColumns().clear();
		Iterator<PhysicalColumn> iterator = updatedSourceColumns.iterator();

		while (iterator.hasNext()) {
			PhysicalColumn updatedPhysicalColumn = iterator.next();
			PhysicalColumn columnToAdd = originalSourceTable.getColumn(updatedPhysicalColumn.getName());
			if (columnToAdd != null) {
				if (!columnToAdd.isMarkedDeleted()) {
					originalPhysicalForeignKey.getSourceColumns().add(columnToAdd);
				} else {
					return false;
				}
			} else {
				// source column not found in the original model
				return false;
			}
		}
		return true;

	}

	private boolean checkDestinationForeignKeyColumns(PhysicalForeignKey originalPhysicalForeignKey, List<PhysicalColumn> updatedDestinationColumns) {
		PhysicalTable originalDestinationTable = originalPhysicalForeignKey.getDestinationTable();

		// remove all destination columns
		originalPhysicalForeignKey.getDestinationColumns().clear();
		// add the new destination columns
		for (PhysicalColumn updatedDestinationColumn : updatedDestinationColumns) {
			PhysicalColumn newDestColumn = originalDestinationTable.getColumn(updatedDestinationColumn.getName());
			if (newDestColumn == null) {
				// destination column not found in the original model
				return false;
			} else {
				if (!newDestColumn.isMarkedDeleted()) {
					// add destination column
					originalPhysicalForeignKey.getDestinationColumns().add(newDestColumn);
				} else {
					return false;
				}

			}
		}
		return true;

	}

	/**
	 * Create a new foreign key to the passed model using the same informations found in the passed examplePhysicalForeignKey
	 *
	 * @param model
	 * @param examplePhysicalForeignKey
	 */
	public void addForeignKey(PhysicalModel model, PhysicalForeignKey examplePhysicalForeignKey) {

		String fkName = examplePhysicalForeignKey.getSourceName();
		PhysicalTable sourceTable = model.getTable(examplePhysicalForeignKey.getSourceTable().getName());
		PhysicalTable destinationTable = model.getTable(examplePhysicalForeignKey.getDestinationTable().getName());
		PhysicalForeignKey foreignKey = null;
		if (destinationTable == null || sourceTable == null) {
			// skip this foreign key because table is not found in the
			// physical model
			log("Foreign Key skipped because table was not found in the physical model");
		} else {
			foreignKey = FACTORY.createPhysicalForeignKey();
			getPropertiesInitializer().addProperties(foreignKey);
			foreignKey.setName(fkName);
			foreignKey.setSourceName(fkName);
			foreignKey.setSourceTable(sourceTable);
			foreignKey.setDestinationTable(destinationTable);

			// add source columns to FK
			List<PhysicalColumn> sourceColumns = examplePhysicalForeignKey.getSourceColumns();
			for (PhysicalColumn sourceColumn : sourceColumns) {
				PhysicalColumn sourceCol = sourceTable.getColumn(sourceColumn.getName());
				foreignKey.getSourceColumns().add(sourceCol);
			}
			// add destination columns to FK
			List<PhysicalColumn> destinationColumns = examplePhysicalForeignKey.getDestinationColumns();
			for (PhysicalColumn destinationColumn : destinationColumns) {
				PhysicalColumn destinationCol = destinationTable.getColumn(destinationColumn.getName());
				foreignKey.getDestinationColumns().add(destinationCol);
			}

		}
		// add the foreign key to the physical model

		if (foreignKey != null) {
			model.getForeignKeys().add(foreignKey);
		}

	}

	public PhysicalColumn updateColumn(PhysicalColumn originalPhysicalColumn, PhysicalColumn updatedPhysicalColumn) {
		// 1- check column type
		// **************************************************
		String originalColumnDataType = originalPhysicalColumn.getDataType();
		String updatedColumnDataType = updatedPhysicalColumn.getDataType();

		if (!originalColumnDataType.equalsIgnoreCase(updatedColumnDataType)) {
			// Column type is changed
			// data type
			originalPhysicalColumn.setDataType(updatedColumnDataType);
			// type name
			originalPhysicalColumn.setTypeName(updatedPhysicalColumn.getTypeName());
		}

		// 2- Check if column is already a part of a primary key
		// **************************************************
		boolean updatedColumnIsPK = updatedPhysicalColumn.isPrimaryKey();
		boolean originalColumnIsPK = originalPhysicalColumn.isPrimaryKey();

		if (updatedColumnIsPK) {
			// it wasn't in the original model
			if (!originalColumnIsPK) {
				// create a new pk for the table or update the existing
				PhysicalTable originalPhysicalTable = originalPhysicalColumn.getTable();

				PhysicalPrimaryKey primaryKey = originalPhysicalColumn.getTable().getPrimaryKey();
				if (primaryKey != null) {
					// update
					primaryKey.getColumns().add(originalPhysicalColumn);
				} else {
					// create new PK

					PhysicalPrimaryKey newPrimaryKey = FACTORY.createPhysicalPrimaryKey();
					String pkName = updatedPhysicalColumn.getTable().getPrimaryKey().getName();
					newPrimaryKey.setName(pkName);

					newPrimaryKey.setTable(originalPhysicalColumn.getTable());
					PhysicalModel originalPhysicalModel = originalPhysicalTable.getModel();
					originalPhysicalModel.getPrimaryKeys().add(newPrimaryKey);

					getPropertiesInitializer().addProperties(newPrimaryKey);

					newPrimaryKey.getColumns().add(originalPhysicalColumn);

				}
			}
		} else {
			if (originalColumnIsPK) {
				// Column is no more a pk (it was)
				PhysicalPrimaryKey primaryKey = originalPhysicalColumn.getTable().getPrimaryKey();
				primaryKey.getColumns().remove(originalPhysicalColumn);

				// remove PhysicalPrimaryKey if empty
				if (primaryKey.getColumns().isEmpty()) {
					PhysicalModel physicalModel = originalPhysicalColumn.getTable().getModel();
					physicalModel.getPrimaryKeys().remove(primaryKey);
				}
			}
		}

		return originalPhysicalColumn;

	}

	/**
	 * Return a collection of elements (tables and columns) that are marked as deleted in the passed physical model
	 *
	 * @param physicalModel
	 * @return markedElements elements marked as deleted (tables and columns)
	 */
	public List<ModelObject> getElementsMarkedAsDeleted(PhysicalModel physicalModel) {
		List<PhysicalTable> physicalTables = physicalModel.getTables();
		List<ModelObject> markedElements = new ArrayList<ModelObject>();
		for (PhysicalTable physicalTable : physicalTables) {
			// check table
			String isDeleted = getProperty(physicalTable, PhysicalModelPropertiesFromFileInitializer.IS_DELETED);
			if ((isDeleted != null) && (isDeleted.equalsIgnoreCase("true"))) {
				markedElements.add(physicalTable);
			} else {
				// check columns
				List<PhysicalColumn> physicalColumns = physicalTable.getColumns();
				for (PhysicalColumn physicalColumn : physicalColumns) {
					String columnIsDeleted = getProperty(physicalColumn, PhysicalModelPropertiesFromFileInitializer.IS_DELETED);
					if ((columnIsDeleted != null) && (columnIsDeleted.equalsIgnoreCase("true"))) {
						markedElements.add(physicalColumn);
					}
				}
			}
		}
		return markedElements;
	}

	// --------------------------------------------------------
	// Accessor methods
	// --------------------------------------------------------

	/**
	 * Remove the physical foreign key from the Physical Model and also remove pending references (ex in BusinessRelationship)
	 *
	 */
	public void removePhysicalForeignKey(PhysicalModel physicalModel, PhysicalForeignKey physicalForeignKey) {
		physicalModel.getForeignKeys().remove(physicalForeignKey);

		// remove inverse references (if any)
		ModelSingleton modelSingleton = ModelSingleton.getInstance();
		ECrossReferenceAdapter adapter = modelSingleton.getCrossReferenceAdapter();
		Collection<Setting> settings = adapter.getInverseReferences(physicalForeignKey, true);
		for (Setting setting : settings) {
			EObject eobject = setting.getEObject();
			if (eobject instanceof BusinessRelationship) {
				BusinessRelationship businessRelationship = (BusinessRelationship) eobject;
				if (businessRelationship.getPhysicalForeignKey().equals(physicalForeignKey)) {
					// remove reference
					businessRelationship.setPhysicalForeignKey(null);
				}
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

	public PhysicalTable findTable(String tableName, EList<PhysicalTable> physicalTables) {

		for (PhysicalTable physicalTable : physicalTables) {
			if (physicalTable.getName().equals(tableName)) {
				return physicalTable;
			}
		}
		return null;
	}

	private String getProperty(PhysicalTable physicalTable, String propertyName) {
		ModelProperty property = physicalTable.getProperties().get(propertyName);
		return property != null ? property.getValue() : null;
	}

	private String getProperty(PhysicalColumn physicalColumn, String propertyName) {
		ModelProperty property = physicalColumn.getProperties().get(propertyName);
		return property != null ? property.getValue() : null;
	}

	private void setProperty(PhysicalTable physicalTable, String propertyName, String value) {
		ModelProperty property = physicalTable.getProperties().get(propertyName);
		if (property != null) {
			property.setValue(value);
		}
	}

	private void setProperty(PhysicalColumn physicalColumn, String propertyName, String value) {
		ModelProperty property = physicalColumn.getProperties().get(propertyName);
		if (property != null) {
			property.setValue(value);
		}
	}

	public IPropertiesInitializer getPropertiesInitializer() {
		return propertiesInitializer;
	}

	public void setPropertiesInitializer(IPropertiesInitializer propertyInitializer) {
		this.propertiesInitializer = propertyInitializer;
	}

	public Model getRootModel() {
		return rootModel;
	}

	public void setRootModel(Model rootModel) {
		this.rootModel = rootModel;
	}

	// --------------------------------------------------------
	// Static methods
	// --------------------------------------------------------

	private static void log(String msg) {
		// System.out.println(msg);
	}

}
