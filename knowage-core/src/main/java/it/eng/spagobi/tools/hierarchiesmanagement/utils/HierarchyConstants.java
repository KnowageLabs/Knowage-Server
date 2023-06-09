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
package it.eng.spagobi.tools.hierarchiesmanagement.utils;

public class HierarchyConstants {

	public static final String HIERARCHIES_FILE_NAME = "hierarchies"; // for now is a constant

	// XML TAGS
	// GENERALS
	public static final String DIMENSIONS = "DIMENSIONS";
	public static final String DIMENSION = "DIMENSION";
	public static final String NAME = "NAME";
	public static final String LABEL = "LABEL";
	public static final String PREFIX = "PREFIX";
	public static final String HIERARCHY_TABLE = "HIERARCHY_TABLE";
	public static final String HIERARCHY_FK = "HIERARCHY_FK";
	public static final String DATASOURCE = "DATASOURCE";
	public static final String PRIMARY_KEY = "PRIMARY_KEY";

	// CONFIGS SECTION
	public static final String CONFIGS = "CONFIGS";
	public static final String CONFIG = "CONFIG";
	public static final String NUM_LEVELS = "NUM_LEVELS";
	public static final String ALLOW_DUPLICATE = "ALLOW_DUPLICATE";
	public static final String UNIQUE_NODE = "UNIQUE_NODE";
	public static final String FORCE_NAME_AS_LEVEL = "FORCE_NAME_AS_LEVEL";
	public static final String NODE = "NODE";
	public static final String LEAF = "LEAF";
	public static final String ORIG_NODE = "ORIG_NODE";

	// DIM_FIELDS SECTION
	public static final String DIM_FIELDS = "DIM_FIELDS";

	// DIM_FILTERS SECTION
	public static final String DIM_FILTERS = "DIM_FILTERS";

	// HIER_FIELDS SECTIONS
	public static final String HIER_FIELDS = "HIER_FIELDS";
	public static final String GENERAL_FIELDS = "GENERAL_FIELDS";
	public static final String NODE_FIELDS = "NODE_FIELDS";
	public static final String LEAF_FIELDS = "LEAF_FIELDS";

	// FIELDS
	public static final String FIELD = "FIELD";
	public static final String FIELD_ID = "ID";
	public static final String FIELD_NAME = "NAME";
	public static final String FIELD_ALIAS = "ALIAS";
	public static final String FIELD_VISIBLE = "VISIBLE";
	public static final String FIELD_EDITABLE = "EDITABLE";
	public static final String FIELD_PARENT = "PARENT";
	public static final String FIELD_TYPE = "TYPE";
	public static final String FIELD_SINGLE_VALUE = "SINGLE_VALUE";
	public static final String FIELD_IS_ORDER = "ORDER_FIELD";
	public static final String FIELD_REQUIRED = "REQUIRED";
	public static final String FIELD_FIX_VALUE = "FIX_VALUE";
	public static final String FIELD_UNIQUE_CODE = "UNIQUE_CODE";

	// FILTERS
	public static final String FILTER = "FILTER";
	public static final String FILTER_NAME = "NAME";
	public static final String FILTER_TYPE = "TYPE";
	public static final String FILTER_VALUE = "VALUE";
	public static final String FILTER_DEFAULT = "DEFAULT";
	public static final String FILTER_CONDITION = "CONDITION";

	// dialects for correct definition of date's functions
	public static final String DIALECT_MYSQL_INNODB = "org.hibernate.dialect.MySQLInnoDBDialect";
	public static final String DIALECT_MYSQL = "org.hibernate.dialect.MySQLDialect";
	public static final String DIALECT_TERADATA = "org.hibernate.dialect.TeradataDialect";
	public static final String DIALECT_POSTGRES = "org.hibernate.dialect.PostgreSQLDialect";
	public static final String DIALECT_ORACLE = "org.hibernate.dialect.OracleDialect";
	public static final String DIALECT_HSQL = "org.hibernate.dialect.HSQLDialect";
	public static final String DIALECT_ORACLE9i10g = "org.hibernate.dialect.Oracle9Dialect";
	public static final String DIALECT_SQLSERVER = "org.hibernate.dialect.SQLServerDialect";
	public static final String DIALECT_INGRES = "org.hibernate.dialect.IngresDialect";

	// CONDITIONS FIELDS (mandatory)
	public static final String HIER_NM = "HIER_NM";
	public static final String HIER_CD = "HIER_CD";
	public static final String HIER_TP = "HIER_TP";
	public static final String HIER_DS = "HIER_DS";
	public static final String LEAF_PARENT_CD = "LEAF_PARENT_CD";
	public static final String LEAF_ORIG_PARENT_CD = "LEAF_ORIG_PARENT_CD";
	public static final String LEAF_PARENT_NM = "LEAF_PARENT_NM";
	public static final String BEGIN_DT = "BEGIN_DT";
	public static final String END_DT = "END_DT";

	// DIMENSION DATA
	public static final String ROOT = "root";
	public static final String COLUMNS = "columns";
	public static final String COLUMNS_SEARCH = "columns_search";
	public static final String DIM_FILTER_ID_FIELD = "_ID";
	public static final String DIM_FILTER_FIELD = "_CD";
	public static final String SELECT_HIER_FILTER_FIELD = "_CD_LEAF";
	public static final String SUFFIX_CD_LEV = "_CD_LEV";
	public static final String SUFFIX_NM_LEV = "_NM_LEV";
	public static final String SUFFIX_CD_LEAF = "_CD_LEAF";
	public static final String SUFFIX_NM_LEAF = "_NM_LEAF";

	public static final String BKP_COLUMN = "BACKUP";
	public static final String BKP_TIMESTAMP_COLUMN = "BACKUP_TIMESTAMP";
	public static final String[] BKP_GEN_FIELDS = new String[] { HIER_NM, HIER_CD, HIER_DS };

	public static final String LEVEL = "LEVEL";

	// TREE
	public static final String ID = "id";
	public static final String TREE_NAME = "name";
	public static final String LEAF_ID = "LEAF_ID";
	public static final String MAX_DEPTH = "MAX_DEPTH";
	public static final String TREE_NODE_ID = "TREE_NODE_ID";
	public static final String TREE_NODE_CD = "TREE_NODE_CD";
	public static final String TREE_NODE_NM = "TREE_NODE_NM";
	public static final String TREE_LEAF_ID = "TREE_LEAF_ID";
	public static final String TREE_LEAF_CD = "TREE_LEAF_CD";
	public static final String TREE_LEAF_NM = "TREE_LEAF_NM";
	public static final String DIMENSION_ID = "DIMENSION_ID";
	public static final String DIMENSION_CD = "DIMENSION_CD";
	public static final String DIMENSION_NM = "DIMENSION_NM";

	// FIELDS TYPES
	public static final String FIELD_TP_STRING = "String";
	public static final String FIELD_TP_NUMBER = "Number";
	public static final String FIELD_TP_DATE = "Date";

	public static final String MATCH_LEAF_FIELDS = "MATCH_LEAF_FIELDS";
	public static final String JSON_CD_PARENT = "CD_PARENT";
	public static final String JSON_NM_PARENT = "NM_PARENT";

	public static final String HIER_TP_MASTER = "MASTER";
	public static final String HIER_TP_TECHNICAL = "TECHNICAL";

	public static final String HIER_MASTERS_CONFIG_TABLE = "HIER_MASTERS_CONFIG";
	public static final String HIER_MASTERS_CONFIG_ID = "HIER_MASTER_ID";
	public static final String HIER_MASTERS_CONFIG = "CONFIGURATION";

	public static final int CD_VALUE_POSITION = 0;
	public static final int NM_VALUE_POSITION = 1;

	// fill options
	public static final String FILL_EMPTY = "FILL_EMPTY";
	public static final String FILL_EMPTY_YES = "YES";
	public static final String FILL_EMPTY_NO = "NO";
	public static final String FILL_VALUE = "FILL_VALUE";

	// PROPAGATION FIELDS
	public static final String REL_MASTER_TECH_TABLE_NAME = "HIER_MASTER_TECHNICAL";
	public static final String REL_MASTER_TECH_TABLE_NAME_ID = "MT_ID";
	public static final String HIER_CD_T = "HIER_CD_T";
	public static final String HIER_NM_T = "HIER_NM_T";
	public static final String NODE_CD_T = "NODE_CD_T";
	public static final String NODE_NM_T = "NODE_NM_T";
	public static final String NODE_LEV_T = "NODE_LEV_T";
	public static final String PATH_CD_T = "PATH_CD_T";
	public static final String PATH_NM_T = "PATH_NM_T";
	public static final String HIER_CD_M = "HIER_CD_M";
	public static final String HIER_NM_M = "HIER_NM_M";
	public static final String NODE_CD_M = "NODE_CD_M";
	public static final String NODE_NM_M = "NODE_NM_M";
	public static final String NODE_LEV_M = "NODE_LEV_M";
	public static final String GENERAL_INFO_T = "GENERAL_INFO_T";

}
