/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.hibernate.dialect;

import java.sql.Types;

import org.hibernate.Hibernate;
import org.hibernate.dialect.function.VarArgsSQLFunction;

public class ExtendedSQLServerDialect extends SQLServerDialect {
	public ExtendedSQLServerDialect() {
		super();
		registerColumnType(Types.DATE, "DATE");
		registerColumnType(Types.TIME, "TIME");
		registerColumnType(Types.TIMESTAMP, "TIMESTAMP");
		// registerFunction("DATEADD", new
		// StandardSQLFunction("DATEADD",Hibernate.TIMESTAMP));
		registerFunction("dateadd", new VarArgsSQLFunction(Hibernate.TIMESTAMP, "dateadd(", ",", ")"));
		registerFunction("datediff", new VarArgsSQLFunction(Hibernate.INTEGER, "datediff(", ",", ")"));
		registerFunction("convert", new VarArgsSQLFunction(Hibernate.STRING, "convert(", ",", ")"));
	}

}
