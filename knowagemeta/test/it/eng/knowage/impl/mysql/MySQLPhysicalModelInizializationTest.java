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
package it.eng.knowage.impl.mysql;

import it.eng.knowage.common.TestConstants;
import it.eng.knowage.initializer.AbstractKnowageMetaTest;
import it.eng.knowage.initializer.DataSourceFactory;
import it.eng.knowage.initializer.TestModelFactory;
import it.eng.knowage.meta.model.physical.PhysicalColumn;
import it.eng.knowage.meta.model.physical.PhysicalForeignKey;
import it.eng.knowage.meta.model.physical.PhysicalPrimaryKey;
import it.eng.knowage.meta.model.physical.PhysicalTable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */
public class MySQLPhysicalModelInizializationTest extends AbstractKnowageMetaTest {

	static private Logger logger = Logger.getLogger(MySQLPhysicalModelInizializationTest.class);

	@Override
	public void setUp() throws Exception {
		try {
			super.setUp();

			if (dbType == null)
				dbType = TestConstants.DatabaseType.MYSQL;

			if (rootModel == null) {
				setRootModel(TestModelFactory.createModel(dbType));
			}
		} catch (Exception t) {
			System.err.println("An unespected error occurred during setUp: ");
			t.printStackTrace();
			throw t;
		}
	}

	// generic tests imported from parent class ...

	@Override
	public void testModelInitializationSmoke() {
		super.testModelInitializationSmoke();
	}

	@Override
	public void testPhysicalModelInitializationSmoke() {
		super.testPhysicalModelInitializationSmoke();
	}

	@Override
	public void testBusinessModelInitializationSmoke() {
		super.testBusinessModelInitializationSmoke();
	}

	@Override
	public void testPhysicalModelSourceDatabase() {
		super.testPhysicalModelSourceDatabase();
		Assert.assertTrue("Database name [" + physicalModel.getDatabaseName().toLowerCase() + "] does not contain word [mysql]", physicalModel
				.getDatabaseName().toLowerCase().contains("mysql"));
	}

	public void testPhysicalModelCatalog() {
		Assert.assertNotNull("If the source database is MySql the catalog property cannot be null", physicalModel.getCatalog());
		Assert.assertEquals("meta_test", physicalModel.getCatalog());
	}

	public void testPhysicalModelSchema() {
		Assert.assertNull("If the source database is MySql the schema property must be null", physicalModel.getSchema());
	}

	// Create Datasources specific for this test
	public void testCreateDataSources() {
		dataSourceReading = DataSourceFactory.createDataSource(TestConstants.DatabaseType.MYSQL);
		Assert.assertNotNull("DataSource on MYSQL not defined correctly", dataSourceReading);
	}

	// =======================================================
	// TABLES
	// =======================================================

	/**
	 * Test that all tables contained in the database are imported in the physical model
	 */
	public void testPhysicalModelTables() {

		Assert.assertEquals(TestConstants.MYSQL_TABLE_NAMES.length, physicalModel.getTables().size());

		for (int i = 0; i < TestConstants.MYSQL_TABLE_NAMES.length; i++) {
			PhysicalTable table = physicalModel.getTable(TestConstants.MYSQL_TABLE_NAMES[i]);
			Assert.assertNotNull("Physical model does not contain table [" + TestConstants.MYSQL_TABLE_NAMES[i] + "]", table);
		}
	}

	/**
	 * Test that table comments are imported properly in the physical model
	 *
	 * TODO find out why MYSQL driver is unable to return comments as defined into database
	 */
	public void testPhysicalModelTableComments() {
		PhysicalTable table = null;
		table = physicalModel.getTable("currency");
		Assert.assertEquals("currency table comment", table.getComment());
		// Assert.assertEquals("", table.getComment());
	}

	/**
	 * Test that table types (TABLE or VIEW) are imported properly in the physical model
	 */
	public void testPhysicalModelTableTypes() {
		PhysicalTable table = null;

		table = physicalModel.getTable("currency");
		Assert.assertEquals("BASE TABLE", table.getType());

		table = physicalModel.getTable("currency_view");
		Assert.assertEquals("VIEW", table.getType());
	}

	// =======================================================
	// COLUMNS
	// =======================================================

	/**
	 * Test that column of table <code>currency</code> and view <code>currency_view</code> are imported properly in the physical model
	 */
	public void testPhysicalModelColumns() {
		PhysicalTable table = null;
		PhysicalColumn column = null;

		String[] columnNames = new String[] { "currency_id", "date", "currency", "conversion_ratio" };

		table = physicalModel.getTable("currency");
		Assert.assertEquals(4, table.getColumns().size());
		for (int i = 0; i < columnNames.length; i++) {
			column = table.getColumn(columnNames[i]);
			Assert.assertNotNull("Table [currency] does not cotain column [" + columnNames[i] + "] as expected", column);
			Assert.assertEquals(columnNames[i], column.getName());
			Assert.assertEquals(table, column.getTable());
			Assert.assertEquals((i + 1), column.getPosition());
		}

		table = physicalModel.getTable("currency_view");
		Assert.assertEquals(4, table.getColumns().size());
		for (int i = 0; i < columnNames.length; i++) {
			column = table.getColumn(columnNames[i]);
			Assert.assertNotNull("Table [currency_view] does not cotain column [" + columnNames[i] + "] as expected", column);
			Assert.assertEquals(columnNames[i], column.getName());
			Assert.assertEquals(table, column.getTable());
			Assert.assertEquals((i + 1), column.getPosition());
		}
	}

	/**
	 * Test that properties id and uniqueName of physical columns is null. This two properties are used in the business model but not in the physical model.
	 */
	public void testPhysicalModelColumnId() {
		PhysicalTable table = null;
		PhysicalColumn column = null;

		table = physicalModel.getTable("currency");
		column = table.getColumn("currency_id");
		Assert.assertEquals(null, column.getId());
		Assert.assertEquals(null, column.getUniqueName());
		column = table.getColumn("conversion_ratio");
		Assert.assertEquals(null, column.getId());
		Assert.assertEquals(null, column.getUniqueName());

		table = physicalModel.getTable("currency_view");
		Assert.assertEquals(null, column.getId());
		Assert.assertEquals(null, column.getUniqueName());
		column = table.getColumn("conversion_ratio");
		Assert.assertEquals(null, column.getId());
		Assert.assertEquals(null, column.getUniqueName());

	}

	/**
	 * Test numeric column properties related to column type:
	 *
	 * - Type name - Data type - Radix - Decimal digits - Octect length - Size
	 *
	 * @see http://dev.mysql.com/doc/refman/5.5/en/numeric-type-overview.html
	 */
	public void testPhysicalModelNumericColumnTypes() {
		PhysicalTable table = null;
		PhysicalColumn column = null;

		table = physicalModel.getTable("test_types");

		/**
		 * `t_bit` bit(8) NOT NULL
		 *
		 * COMMENT: A bit-field type. M indicates the number of bits per value, from 1 to 64. The default is 1 if M is omitted.
		 */
		column = table.getColumn("t_bit");
		Assert.assertEquals("BIT", column.getTypeName());
		Assert.assertEquals("BIT", column.getDataType());
		Assert.assertEquals(10, column.getRadix());
		Assert.assertEquals(0, column.getDecimalDigits());
		Assert.assertEquals(0, column.getOctectLength());
		Assert.assertEquals(8, column.getSize());

		/**
		 * `t_tinyint` tinyint(3) unsigned NOT NULL
		 *
		 * COMMENT: A very small integer. The signed range is -128 to 127. The unsigned range is 0 to 255.
		 */
		column = table.getColumn("t_tinyint");
		Assert.assertEquals("TINYINT UNSIGNED", column.getTypeName());
		Assert.assertEquals("TINYINT", column.getDataType());
		Assert.assertEquals(10, column.getRadix());
		Assert.assertEquals(0, column.getDecimalDigits());
		Assert.assertEquals(0, column.getOctectLength());
		Assert.assertEquals(3, column.getSize());

		/**
		 * `t_boolean` tinyint(1) NOT NULL
		 *
		 * COMMENT These types are synonyms for TINYINT(1)
		 */
		column = table.getColumn("t_boolean");
		Assert.assertEquals("TINYINT", column.getTypeName());
		Assert.assertEquals("BIT", column.getDataType());
		Assert.assertEquals(10, column.getRadix());
		Assert.assertEquals(0, column.getDecimalDigits());
		Assert.assertEquals(0, column.getOctectLength());
		Assert.assertEquals(3, column.getSize());

		/**
		 * `t_smallint` smallint(5) unsigned NOT NULL
		 *
		 * COMMENT: A small integer. The signed range is -32768 to 32767. The unsigned range is 0 to 65535.
		 */
		column = table.getColumn("t_smallint");
		Assert.assertEquals("SMALLINT UNSIGNED", column.getTypeName());
		Assert.assertEquals("INTEGER", column.getDataType());
		Assert.assertEquals(10, column.getRadix());
		Assert.assertEquals(0, column.getDecimalDigits());
		Assert.assertEquals(0, column.getOctectLength());
		Assert.assertEquals(5, column.getSize());

		/**
		 * `t_mediumint` mediumint(8) unsigned NOT NULL
		 *
		 * COMMENT 'A medium-sized integer. The signed range is -8388608 to 8388607. The unsigned range is 0 to 16777215. ',
		 */
		column = table.getColumn("t_mediumint");
		Assert.assertEquals("MEDIUMINT UNSIGNED", column.getTypeName());
		Assert.assertEquals("INTEGER", column.getDataType());
		Assert.assertEquals(10, column.getRadix());
		Assert.assertEquals(0, column.getDecimalDigits());
		Assert.assertEquals(0, column.getOctectLength());
		Assert.assertEquals(7, column.getSize());

		/**
		 * `t_int` int(10) unsigned NOT NULL
		 *
		 * COMMENT 'A normal-size integer. The signed range is -2147483648 to 2147483647. The unsigned range is 0 to 4294967295.
		 */
		column = table.getColumn("t_int");
		Assert.assertEquals("INT UNSIGNED", column.getTypeName());
		Assert.assertEquals("BIGINT", column.getDataType());
		Assert.assertEquals(10, column.getRadix());
		Assert.assertEquals(0, column.getDecimalDigits());
		Assert.assertEquals(0, column.getOctectLength());
		Assert.assertEquals(10, column.getSize());

		/**
		 * `t_integer` int(11) NOT NULL
		 *
		 * COMMENT This type is a synonym for INT.
		 */
		column = table.getColumn("t_integer");
		Assert.assertEquals("INT", column.getTypeName());
		Assert.assertEquals("INTEGER", column.getDataType());
		Assert.assertEquals(10, column.getRadix());
		Assert.assertEquals(0, column.getDecimalDigits());
		Assert.assertEquals(0, column.getOctectLength());
		Assert.assertEquals(10, column.getSize());

		/**
		 * `t_bigint` bigint(20) unsigned NOT NULL
		 *
		 * COMMENT: A large integer. The signed range is -9223372036854775808 to 9223372036854775807. The unsigned range is 0 to 18446744073709551615.
		 */
		column = table.getColumn("t_bigint");
		Assert.assertEquals("BIGINT UNSIGNED", column.getTypeName());
		Assert.assertEquals("BIGINT", column.getDataType());
		Assert.assertEquals(10, column.getRadix());
		Assert.assertEquals(0, column.getDecimalDigits());
		Assert.assertEquals(0, column.getOctectLength());
		Assert.assertEquals(20, column.getSize());

		/**
		 * `t_decimal` decimal(12,3) NOT NULL
		 *
		 * COMMENT A packed “exact” fixed-point number. M is the total number of digits (the precision) and D is the number of digits after the decimal point
		 * (the scale).
		 */
		column = table.getColumn("t_decimal");
		Assert.assertEquals("DECIMAL", column.getTypeName());
		Assert.assertEquals("DECIMAL", column.getDataType());
		Assert.assertEquals(10, column.getRadix());
		Assert.assertEquals(3, column.getDecimalDigits());
		Assert.assertEquals(0, column.getOctectLength());
		Assert.assertEquals(12, column.getSize());

		/**
		 * `t_dec` decimal(9,3) NOT NULL
		 *
		 * COMMENT These types are synonyms for DECIMAL
		 */
		column = table.getColumn("t_dec");
		Assert.assertEquals("DECIMAL", column.getTypeName());
		Assert.assertEquals("DECIMAL", column.getDataType());
		Assert.assertEquals(10, column.getRadix());
		Assert.assertEquals(3, column.getDecimalDigits());
		Assert.assertEquals(0, column.getOctectLength());
		Assert.assertEquals(9, column.getSize());

		/**
		 * `t_float` float(9,3) NOT NULL
		 *
		 * COMMENT A small (single-precision) floating-point number
		 */
		column = table.getColumn("t_float");
		Assert.assertEquals("FLOAT", column.getTypeName());
		Assert.assertEquals("FLOAT", column.getDataType());
		Assert.assertEquals(10, column.getRadix());
		Assert.assertEquals(3, column.getDecimalDigits());
		Assert.assertEquals(0, column.getOctectLength());
		Assert.assertEquals(9, column.getSize());

		/**
		 * `t_double` double(12,3) NOT NULL
		 *
		 * COMMENT A normal-size (double-precision) floating-point number
		 */
		column = table.getColumn("t_double");
		Assert.assertEquals("DOUBLE", column.getTypeName());
		Assert.assertEquals("DOUBLE", column.getDataType());
		Assert.assertEquals(10, column.getRadix());
		Assert.assertEquals(3, column.getDecimalDigits());
		Assert.assertEquals(0, column.getOctectLength());
		Assert.assertEquals(12, column.getSize());
	}

	/**
	 * Test date/time column properties related to column type:
	 *
	 * - Type name - Data type - Radix - Decimal digits - Octect length - Size
	 *
	 * @see http://dev.mysql.com/doc/refman/5.5/en/date-and-time-type-overview.html
	 */
	public void testPhysicalModelDateTimeColumnTypes() {
		PhysicalTable table = null;
		PhysicalColumn column = null;

		table = physicalModel.getTable("test_types");

		/**
		 * `t_date` date NOT NULL
		 *
		 * COMMENT: A date. The supported range is ''1000-01-01'' to ''9999-12-31''. MySQL displays DATE values in ''YYYY-MM-DD'' format, but permits assignment
		 * of values to DATE columns using either strings or numbers.
		 */
		column = table.getColumn("t_date");
		Assert.assertEquals("DATE", column.getTypeName());
		Assert.assertEquals("DATE", column.getDataType());
		Assert.assertEquals(10, column.getRadix());
		Assert.assertEquals(0, column.getDecimalDigits());
		Assert.assertEquals(0, column.getOctectLength());
		Assert.assertEquals(10, column.getSize());

		/**
		 * `t_datetime` datetime NOT NULL
		 *
		 * COMMENT: A date and time combination. The supported range is ''1000-01-01 00:00:00'' to ''9999-12-31 23:59:59''. MySQL displays DATETIME values in
		 * ''YYYY-MM-DD HH:MM:SS'' format, but permits assignment of values to DATETIME columns using either strings or numbers.
		 */
		column = table.getColumn("t_datetime");
		Assert.assertEquals("DATETIME", column.getTypeName());
		Assert.assertEquals("TIMESTAMP", column.getDataType());
		Assert.assertEquals(10, column.getRadix());
		Assert.assertEquals(0, column.getDecimalDigits());
		Assert.assertEquals(0, column.getOctectLength());
		Assert.assertEquals(19, column.getSize());

		/**
		 * `t_timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP
		 *
		 * COMMENT A timestamp. The range is ''1970-01-01 00:00:01'' UTC to ''2038-01-19 03:14:07'' UTC. TIMESTAMP values are stored as the number of seconds
		 * since the epoch (''1970-01-01 00:00:00'' UTC).
		 */
		column = table.getColumn("t_timestamp");
		Assert.assertEquals("TIMESTAMP", column.getTypeName());
		Assert.assertEquals("TIMESTAMP", column.getDataType());
		Assert.assertEquals(10, column.getRadix());
		Assert.assertEquals(0, column.getDecimalDigits());
		Assert.assertEquals(0, column.getOctectLength());
		Assert.assertEquals(19, column.getSize());

		/**
		 * `t_time` time NOT NULL
		 *
		 * COMMENT: 'A time. The range is ''-838:59:59'' to ''838:59:59''. MySQL displays TIME values in ''HH:MM:SS'' format
		 */
		column = table.getColumn("t_time");
		Assert.assertEquals("TIME", column.getTypeName());
		Assert.assertEquals("TIME", column.getDataType());
		Assert.assertEquals(10, column.getRadix());
		Assert.assertEquals(0, column.getDecimalDigits());
		Assert.assertEquals(0, column.getOctectLength());
		Assert.assertEquals(8, column.getSize());

		/**
		 * `t_year` year(4) NOT NULL
		 *
		 * COMMENT: A year in two-digit or four-digit format. The default is four-digit format.
		 */
		column = table.getColumn("t_year");
		Assert.assertEquals("YEAR", column.getTypeName());
		Assert.assertEquals("DATE", column.getDataType());
		Assert.assertEquals(10, column.getRadix());
		Assert.assertEquals(0, column.getDecimalDigits());
		Assert.assertEquals(0, column.getOctectLength());
		Assert.assertEquals(0, column.getSize());
	}

	/**
	 * Test date/time column properties related to column type:
	 *
	 * - Type name - Data type - Radix - Decimal digits - Octect length - Size
	 *
	 * @see http://dev.mysql.com/doc/refman/5.5/en/string-type-overview.html
	 */
	public void testPhysicalModelStringColumnTypes() {
		PhysicalTable table = null;
		PhysicalColumn column = null;

		table = physicalModel.getTable("test_types");

		/**
		 * `t_char` char(1) NOT NULL
		 *
		 * COMMENT is shorthand for CHARACTER'
		 */
		column = table.getColumn("t_char");
		Assert.assertEquals("CHAR", column.getTypeName());
		Assert.assertEquals("CHAR", column.getDataType());
		Assert.assertEquals(10, column.getRadix());
		Assert.assertEquals(0, column.getDecimalDigits());
		Assert.assertEquals(1, column.getOctectLength());
		Assert.assertEquals(1, column.getSize());

		/**
		 * `t_binary` binary(1) NOT NULL
		 *
		 * COMMENT: The BINARY type is similar to the CHAR type, but stores binary byte strings rather than nonbinary character strings. M represents the column
		 * length in bytes.
		 */
		column = table.getColumn("t_binary");
		Assert.assertEquals("BINARY", column.getTypeName());
		Assert.assertEquals("BINARY", column.getDataType());
		Assert.assertEquals(10, column.getRadix());
		Assert.assertEquals(0, column.getDecimalDigits());
		Assert.assertEquals(1, column.getOctectLength());
		Assert.assertEquals(1, column.getSize());

		/*
		 * `t_nchar` char(1) character set utf8 NOT NULL COMMENT 'NCHAR is the standard SQL way to define that a CHAR column should use some predefined
		 * character set', `t_varchar` varchar(45) NOT NULL COMMENT 'VARCHAR is shorthand for', `t_varbinary` varbinary(45) NOT NULL COMMENT 'The VARBINARY type
		 * is similar to the VARCHAR type, but stores binary byte strings rather than nonbinary character strings. M represents the maximum column length in
		 * bytes. ', `t_national_varchar` varchar(45) character set utf8 NOT NULL COMMENT 'NATIONAL VARCHAR is the standard SQL way to define that a VARCHAR
		 * column should use some predefined character set', `t_tinyblob` tinyblob NOT NULL COMMENT 'A BLOB column with a maximum length of 255 ', `t_blob` blob
		 * NOT NULL COMMENT 'A BLOB column with a maximum length of 65,535 ', `t_text` text NOT NULL COMMENT 'A TEXT column with a maximum length of 65,535 ',
		 * `t_tinytext` tinytext NOT NULL COMMENT 'A TEXT column with a maximum length of 255 ', `t_mediumblob` mediumblob NOT NULL COMMENT 'A BLOB column with
		 * a maximum length of 16,777,215 ', `t_mediumtext` mediumtext NOT NULL COMMENT 'A TEXT column with a maximum length of 16,777,215 ', `t_longblob`
		 * longblob NOT NULL COMMENT 'A BLOB column with a maximum length of 4,294,967,295 or 4GB', `t_longtext` longtext NOT NULL COMMENT 'A TEXT column with a
		 * maximum length of 4,294,967,295 or 4GB', `t_enum` enum('rosso','bianco','verde') NOT NULL COMMENT 'An enumeration. A string object that can have only
		 * one value, chosen from the list of values ''value1'', ''value2'', ..., NULL or the special '''' error value', `t_set` set('sala','pepe','olio') NOT
		 * NULL COMMENT 'A set. A string object that can have zero or more values, each of which must be chosen from the list of values ''value1'', ''value2'',
		 * ...'
		 */
	}

	/**
	 * Test that column comments are imported properly in the physical model
	 *
	 * NOTE: if comment is not set MYSQL returns an empty string while POSTGRES returns null.
	 *
	 * TODO uniform the behaviour in the two cases
	 */
	public void testPhysicalModelColumnComments() {
		PhysicalTable table = null;
		PhysicalColumn column = null;

		table = physicalModel.getTable("currency");
		column = table.getColumn("currency_id");
		Assert.assertEquals("Composed id column 1", column.getComment());
		Assert.assertEquals(null, column.getDescription());
		column = table.getColumn("date");
		Assert.assertEquals("Composed id column 2", column.getComment());
		Assert.assertEquals(null, column.getDescription());
		column = table.getColumn("currency");
		Assert.assertEquals("", column.getComment());
		Assert.assertEquals(null, column.getDescription());
	}

	/**
	 * Test the physical columns nullable property
	 */
	public void testPhysicalModelColumnNullableProperty() {
		PhysicalTable table = null;
		PhysicalColumn column = null;

		table = physicalModel.getTable("currency");
		column = table.getColumn("currency_id");
		Assert.assertEquals(false, column.isNullable());

		column = table.getColumn("date");
		Assert.assertEquals(false, column.isNullable());

		column = table.getColumn("currency");
		Assert.assertEquals(false, column.isNullable());

		column = table.getColumn("conversion_ratio");
		Assert.assertEquals(false, column.isNullable());

		table = physicalModel.getTable("currency_view");
		column = table.getColumn("currency_id");
		Assert.assertEquals(false, column.isNullable());

		column = table.getColumn("date");
		Assert.assertEquals(false, column.isNullable());

		column = table.getColumn("currency");
		Assert.assertEquals(false, column.isNullable());

		column = table.getColumn("conversion_ratio");
		Assert.assertEquals(false, column.isNullable());

		table = physicalModel.getTable("customer");
		column = table.getColumn("address1");
		Assert.assertEquals(true, column.isNullable());
	}

	/**
	 * Test the physical columns default value
	 */
	public void testPhysicalModelColumnDefaultValue() {
		PhysicalTable table = null;
		PhysicalColumn column = null;

		table = physicalModel.getTable("currency");
		column = table.getColumn("currency_id");
		Assert.assertEquals(null, column.getDefaultValue());
		column = table.getColumn("currency");
		Assert.assertEquals("USD", column.getDefaultValue());

		table = physicalModel.getTable("currency_view");
		column = table.getColumn("currency_id");
		Assert.assertEquals(null, column.getDefaultValue());
		column = table.getColumn("currency");
		Assert.assertEquals("USD", column.getDefaultValue());
	}

	// =======================================================
	// P-KEYS
	// =======================================================

	/**
	 * Test that if a table have no primary key in the database it also have no primary key in the physical model
	 */
	public void testPhysicalModelNoPK() {
		PhysicalTable table = physicalModel.getTable("currency_view");
		PhysicalPrimaryKey pk = table.getPrimaryKey();
		Assert.assertNull("PrimaryKey of table [tmpsbiqbe_bi] must be null", pk);

		for (PhysicalColumn column : table.getColumns()) {
			Assert.assertFalse(column.isPrimaryKey());
			Assert.assertFalse(column.isPartOfCompositePrimaryKey());
		}

	}

	/**
	 * Test that simple primary key (i.e. primary key composed by only one column) are imported properly in the physical model.
	 *
	 * NOTE: The name of the primary key in MYSQL is always equals to PRIMARY while in POSTGRES it is equals to the name specified by the user or automatically
	 * generated by the database
	 */
	public void testPhysicalModelSimplePK() {
		PhysicalTable table = physicalModel.getTable("customer");
		PhysicalPrimaryKey pk = table.getPrimaryKey();
		Assert.assertNotNull("PrimaryKey of table [customer] cannot be null", pk);
		// @see NOTE above
		// Assert.assertEquals("PRIMARY", pk.getName());

		Assert.assertTrue("PrimaryKey of table [customer] is composed by [" + pk.getColumns().size() + "] column(s) and not 1 as expected", pk.getColumns()
				.size() == 1);
		Set<String> pkColumnNames = new HashSet<String>();
		pkColumnNames.add(pk.getColumns().get(0).getName());
		Assert.assertTrue("Column [customer_id] of table [customer] is not part of the PK as expected", pkColumnNames.contains("customer_id"));

		PhysicalColumn column = table.getColumn("customer_id");
		Assert.assertTrue(column == pk.getColumns().get(0));
		Assert.assertTrue(column.equals(pk.getColumns().get(0)));
		Assert.assertTrue(column.isPrimaryKey());
		Assert.assertFalse(column.isPartOfCompositePrimaryKey());
	}

	/**
	 * Test that composed primary keys (i.e. primary key composed by more than one column) are imported properly in the physical model.
	 */
	public void testPhysicalModelComposedPK() {
		PhysicalTable table = physicalModel.getTable("currency");
		PhysicalPrimaryKey pk = table.getPrimaryKey();
		Assert.assertNotNull("PrimaryKey of table [currency] cannot be null", pk);
		// Assert.assertEquals("PRIMARY", pk.getName());

		Assert.assertTrue("PrimaryKey of table [currency] is composed by [" + pk.getColumns().size() + "] column(s) and not 2 as expected", pk.getColumns()
				.size() == 2);
		Set<String> pkColumnNames = new HashSet<String>();
		pkColumnNames.add(pk.getColumns().get(0).getName());
		pkColumnNames.add(pk.getColumns().get(1).getName());
		Assert.assertTrue("Column [currency_id] of table [currency] is not part of the PK as expected", pkColumnNames.contains("currency_id"));
		Assert.assertTrue("Column [date] of table [currency] is not part of the PK as expected", pkColumnNames.contains("date"));

		PhysicalColumn column = table.getColumn("currency_id");
		Assert.assertTrue(column == pk.getColumns().get(0) || column == pk.getColumns().get(1));
		Assert.assertTrue(column.equals(pk.getColumns().get(0)) || column.equals(pk.getColumns().get(1)));
		Assert.assertTrue(column.isPrimaryKey());
		Assert.assertTrue(column.isPartOfCompositePrimaryKey());
	}

	// =======================================================
	// F-KEYS
	// =======================================================

	/**
	 * Test that if a table have no foreign key in the database it also have no foreign key in the physical model
	 */
	public void testPhysicalModeNoFK() {
		PhysicalTable table = physicalModel.getTable("_product_class_");

		List<PhysicalForeignKey> foreignKeys = table.getForeignKeys();
		assertNotNull(foreignKeys);
		assertEquals(0, foreignKeys.size());
	}

	/**
	 * Test that if a table have one foreign key in the database it also have one foreign key in the physical model
	 */
	public void testPhysicalModeSingleFK() {
		PhysicalTable table = physicalModel.getTable("store");

		List<PhysicalForeignKey> foreignKeys = table.getForeignKeys();
		assertNotNull(foreignKeys);
		assertEquals(1, foreignKeys.size());

		PhysicalForeignKey foreignKey = foreignKeys.get(0);

		assertEquals(null, foreignKey.getId());
		// assertEquals(null, foreignKey.getName());
		assertEquals(null, foreignKey.getUniqueName());
		assertEquals(null, foreignKey.getDescription());

		// source
		assertEquals("FK_store_1", foreignKey.getSourceName());

		assertNotNull(foreignKey.getSourceTable());
		assertEquals(table, foreignKey.getSourceTable());

		assertNotNull(foreignKey.getSourceColumns());
		assertEquals(1, foreignKey.getSourceColumns().size());
		PhysicalColumn sourceColumn = foreignKey.getSourceColumns().get(0);
		assertNotNull(sourceColumn);
		assertEquals("region_id", sourceColumn.getName());
		assertEquals(table, sourceColumn.getTable());

		// destination
		assertEquals(null, foreignKey.getDestinationName());

		assertNotNull(foreignKey.getDestinationTable());
		PhysicalTable destinationTable = physicalModel.getTable("region");
		assertEquals(destinationTable, foreignKey.getDestinationTable());

		assertNotNull(foreignKey.getDestinationColumns());
		assertEquals(1, foreignKey.getDestinationColumns().size());
		PhysicalColumn destinationColumn = foreignKey.getDestinationColumns().get(0);
		assertNotNull(destinationColumn);
		assertEquals("region_id", destinationColumn.getName());
		assertEquals(destinationTable, destinationColumn.getTable());
		assertTrue(destinationColumn.isPrimaryKey());
		assertFalse(destinationColumn.isPartOfCompositePrimaryKey());

		assertEquals(physicalModel, foreignKey.getModel());
		assertEquals(0, foreignKey.getProperties().size());
	}

	/**
	 * Test that if a table have more than one foreign key in the database it also have more than one foreign key in the physical model
	 */
	public void testPhysicalModeMultipleFK() {
		PhysicalTable table = physicalModel.getTable("sales_fact_1998");

		List<PhysicalForeignKey> foreignKeys = table.getForeignKeys();
		assertNotNull(foreignKeys);
		assertEquals(5, foreignKeys.size());
	}

	/**
	 * Test that composite FK (i.e. FK composed by more than one column) are imported properly in the physical model.
	 */
	public void testPhysicalModeCompositeFK() {
		PhysicalTable table = physicalModel.getTable("salary");

		List<PhysicalForeignKey> foreignKeys = table.getForeignKeys();
		assertNotNull(foreignKeys);
		assertEquals(3, foreignKeys.size());

		PhysicalForeignKey foreignKey = null;
		for (PhysicalForeignKey fk : foreignKeys) {
			if (fk.getSourceName().equals("FK_salary_3")) {
				foreignKey = fk;
				break;
			}
		}
		assertNotNull(foreignKey);

		assertEquals(null, foreignKey.getId());
		// assertEquals(null, foreignKey.getName());
		assertEquals(null, foreignKey.getUniqueName());
		assertEquals(null, foreignKey.getDescription());

		// source
		assertEquals("FK_salary_3", foreignKey.getSourceName());

		assertNotNull(foreignKey.getSourceTable());
		assertEquals(table, foreignKey.getSourceTable());

		assertNotNull(foreignKey.getSourceColumns());
		assertEquals(2, foreignKey.getSourceColumns().size());
		PhysicalColumn sourceColumn1 = foreignKey.getSourceColumns().get(0);
		assertNotNull(sourceColumn1);
		assertEquals("currency_id", sourceColumn1.getName());
		assertEquals(table, sourceColumn1.getTable());
		PhysicalColumn sourceColumn2 = foreignKey.getSourceColumns().get(1);
		assertNotNull(sourceColumn2);
		assertEquals("pay_date", sourceColumn2.getName());
		assertEquals(table, sourceColumn2.getTable());

		// destination
		assertEquals(null, foreignKey.getDestinationName());

		assertNotNull(foreignKey.getDestinationTable());
		PhysicalTable destinationTable = physicalModel.getTable("currency");
		assertEquals(destinationTable, foreignKey.getDestinationTable());

		assertNotNull(foreignKey.getDestinationColumns());
		assertEquals(2, foreignKey.getDestinationColumns().size());

		PhysicalColumn destinationColumn1 = foreignKey.getDestinationColumns().get(0);
		assertNotNull(destinationColumn1);
		assertEquals("currency_id", destinationColumn1.getName());
		assertEquals(destinationTable, destinationColumn1.getTable());
		assertTrue(destinationColumn1.isPrimaryKey());
		assertTrue(destinationColumn1.isPartOfCompositePrimaryKey());

		PhysicalColumn destinationColumn2 = foreignKey.getDestinationColumns().get(1);
		assertNotNull(destinationColumn2);
		assertEquals("date", destinationColumn2.getName());
		assertEquals(destinationTable, destinationColumn2.getTable());
		assertTrue(destinationColumn2.isPrimaryKey());
		assertTrue(destinationColumn2.isPartOfCompositePrimaryKey());

		assertEquals(physicalModel, foreignKey.getModel());
		assertEquals(0, foreignKey.getProperties().size());
	}

	@Override
	public void tearDown() throws Exception {
		try {
			super.tearDown();
		} catch (Exception t) {
			System.err.println("An unespected error occurred during tearDown: ");
			t.printStackTrace();
			throw t;
		}
	}

}
