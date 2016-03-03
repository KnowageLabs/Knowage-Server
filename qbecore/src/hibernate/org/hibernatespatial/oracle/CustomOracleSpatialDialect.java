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
package org.hibernatespatial.oracle;


import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.hibernatespatial.GeometryUserType;
import org.hibernatespatial.oracle.OracleSpatial10gDialect;

public class CustomOracleSpatialDialect extends OracleSpatial10gDialect {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8633487928601565815L;

	public CustomOracleSpatialDialect() {
		super();
		registerFunction("dwithin", new StandardSQLFunction("SDO_WITHIN_DISTANCE", StandardBasicTypes.STRING));
		registerFunction("length", new StandardSQLFunction("SDO_GEOM.SDO_LENGTH", StandardBasicTypes.BIG_DECIMAL));
		registerFunction("filter", new StandardSQLFunction("SDO_FILTER", StandardBasicTypes.STRING));
		registerFunction("distance", new StandardSQLFunction("SDO_GEOM.SDO_DISTANCE", StandardBasicTypes.BIG_DECIMAL));
		registerFunction("union", new StandardSQLFunction("SDO_GEOM.SDO_UNION", GeometryUserType.TYPE));
		registerFunction("centroid", new StandardSQLFunction("SDO_GEOM.SDO_CENTROID", GeometryUserType.TYPE));
		registerFunction("covers", new StandardSQLFunction("SDO_COVERS", StandardBasicTypes.STRING));
		registerFunction("coveredby", new StandardSQLFunction("SDO_COVEREDBY", StandardBasicTypes.STRING));
		registerFunction("relate", new StandardSQLFunction("SDO_GEOM.RELATE", StandardBasicTypes.STRING));
		registerFunction("inside", new StandardSQLFunction("SDO_INSIDE", StandardBasicTypes.STRING));
		registerFunction("to_km", new SQLFunctionTemplate(StandardBasicTypes.BIG_DECIMAL, "((?1) * 1.852)"));
		registerFunction("to_nm", new SQLFunctionTemplate(StandardBasicTypes.BIG_DECIMAL, "((?1) / 1.852)"));
		registerFunction("extract", new SQLFunctionTemplate(StandardBasicTypes.LONG, "extract (?1 from (?2))"));
		registerFunction("to_timezone", new SQLFunctionTemplate(StandardBasicTypes.TIMESTAMP, "((?1) + ?2/24)"){
			@Override
			public String render(Type argumentType, List args,
					SessionFactoryImplementor factory) {
				if(args==null || args.size()!=2){
					throw new QueryException("to_timezone() requires two arguments");
				}
				if(!((String)args.get(1)).matches("\\-?((1?[0-9])|(2[0-3]))")){
					throw new QueryException("to_timezone()'s second parameter must be a number from -23 to +23");
				}
				return super.render(argumentType, args, factory);
			}
		});
		registerFunction("latitude", new SQLFunctionTemplate(StandardBasicTypes.STRING, 
			"CASE ?1.Get_GType() WHEN 1 THEN to_char(?1.sdo_point.y) ELSE '' END"));
		registerFunction("longitude", new SQLFunctionTemplate(StandardBasicTypes.STRING, 
				"CASE ?1.Get_GType() WHEN 1 THEN to_char(?1.sdo_point.x) ELSE '' END"));
	}
	
	boolean isOGCStrict() {
		return false;
	}
	
	
}
