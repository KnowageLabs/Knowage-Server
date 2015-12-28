package it.eng.spagobi.tools.hierarchiesmanagement.utils;

public class HierarchyConstants {

	public final static String HIERARCHIES_FILE_NAME = "hierarchies"; // for now is a constant

	// XML TAGS
	// GENERALS
	public final static String DIMENSIONS = "DIMENSIONS";
	public final static String DIMENSION = "DIMENSION";
	public final static String NAME = "NAME";
	public final static String LABEL = "LABEL";
	public final static String PREFIX = "PREFIX";
	public final static String HIERARCHY_TABLE = "HIERARCHY_TABLE";
	public final static String HIERARCHY_FK = "HIERARCHY_FK";
	public final static String DATASOURCE = "DATASOURCE";

	// CONFIGS SECTION
	public final static String CONFIGS = "CONFIGS";
	public final static String CONFIG = "CONFIG";
	public final static String NUM_LEVELS = "NUM_LEVELS";
	public final static String ALLOW_DUPLICATE = "ALLOW_DUPLICATE";
	public final static String NODE = "NODE";
	public final static String LEAF = "LEAF";
	public final static String ORIG_NODE = "ORIG_NODE";

	// DIM_FIELDS SECTION
	public final static String DIM_FIELDS = "DIM_FIELDS";

	// HIER_FIELDS SECTIONS
	public final static String HIER_FIELDS = "HIER_FIELDS";
	public final static String GENERAL_FIELDS = "GENERAL_FIELDS";
	public final static String NODE_FIELDS = "NODE_FIELDS";
	public final static String LEAF_FIELDS = "LEAF_FIELDS";

	// FIELDS
	public final static String FIELD = "FIELD";
	public final static String FIELD_ID = "ID";
	public final static String FIELD_NAME = "NAME";
	public final static String FIELD_VISIBLE = "VISIBLE";
	public final static String FIELD_EDITABLE = "EDITABLE";
	public final static String FIELD_TYPE = "TYPE";
	public final static String FIELD_SINGLE_VALUE = "SINGLE_VALUE";
	public final static String FIELD_REQUIRED = "REQUIRED";

	// dialects for correct definition of date's functions
	public static final String DIALECT_MYSQL = "org.hibernate.dialect.MySQLInnoDBDialect";
	public static final String DIALECT_TERADATA = "org.hibernate.dialect.TeradataDialect";
	public static final String DIALECT_POSTGRES = "org.hibernate.dialect.PostgreSQLDialect";
	public static final String DIALECT_ORACLE = "org.hibernate.dialect.OracleDialect";
	public static final String DIALECT_HSQL = "org.hibernate.dialect.HSQLDialect";
	public static final String DIALECT_ORACLE9i10g = "org.hibernate.dialect.Oracle9Dialect";
	public static final String DIALECT_SQLSERVER = "org.hibernate.dialect.SQLServerDialect";
	public static final String DIALECT_INGRES = "org.hibernate.dialect.IngresDialect";

	// CONDITIONS FIELDS (mandatory)
	public final static String HIER_NM = "HIER_NM";
	public final static String HIER_CD = "HIER_CD";
	public final static String HIER_TP = "HIER_TP";
	public final static String HIER_DS = "HIER_DS";
	public final static String HIER_SCOPE = "SCOPE";
	public final static String LEAF_PARENT_CD = "LEAF_PARENT_CD";
	public final static String LEAF_ORIG_PARENT_CD = "LEAF_ORIG_PARENT_CD";
	public final static String LEAF_PARENT_NM = "LEAF_PARENT_NM";
	public final static String BEGIN_DT = "BEGIN_DT";
	public final static String END_DT = "END_DT";

	// DIMENSION DATA
	public final static String ROOT = "root";
	public final static String COLUMNS = "columns";
	public final static String COLUMNS_SEARCH = "columns_search";
	public final static String DIM_FILTER_FIELD = "_CD";
	public final static String SELECT_HIER_FILTER_FIELD = "_CD_LEAF";

	public final static String BKP_LABEL = "_bkp";

}
