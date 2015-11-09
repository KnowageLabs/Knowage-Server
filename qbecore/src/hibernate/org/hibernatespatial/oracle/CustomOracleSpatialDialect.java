package org.hibernatespatial.oracle;


import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;
import org.hibernatespatial.GeometryUserType;
import org.hibernatespatial.oracle.OracleSpatial10gDialect;

public class CustomOracleSpatialDialect extends OracleSpatial10gDialect {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8633487928601565815L;

	public CustomOracleSpatialDialect() {
		super();
		registerFunction("dwithin", new StandardSQLFunction("SDO_WITHIN_DISTANCE", StandardBasicTypes.BOOLEAN));
		registerFunction("length", new StandardSQLFunction("SDO_GEOM.SDO_LENGTH", StandardBasicTypes.BIG_DECIMAL));
		registerFunction("filter", new StandardSQLFunction("SDO_FILTER", StandardBasicTypes.STRING));
		registerFunction("distance", new StandardSQLFunction("SDO_GEOM.SDO_DISTANCE", StandardBasicTypes.BIG_DECIMAL));
		registerFunction("union", new StandardSQLFunction("SDO_GEOM.SDO_UNION", GeometryUserType.TYPE));
		registerFunction("centroid", new StandardSQLFunction("SDO_GEOM.SDO_CENTROID", GeometryUserType.TYPE));
		registerFunction("covers", new StandardSQLFunction("SDO_COVERS", StandardBasicTypes.STRING));
		registerFunction("coveredby", new StandardSQLFunction("SDO_COVEREDBY", StandardBasicTypes.STRING));
		registerFunction("relate", new StandardSQLFunction("SDO_GEOM.RELATE", StandardBasicTypes.STRING));
		registerFunction("inside", new StandardSQLFunction("SDO_INSIDE", StandardBasicTypes.STRING));
	}
	
	boolean isOGCStrict() {
		return false;
	}
	
}
