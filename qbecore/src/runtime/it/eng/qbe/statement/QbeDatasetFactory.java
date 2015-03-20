/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.statement;

import it.eng.qbe.statement.hibernate.HQLDataSet;
import it.eng.qbe.statement.hibernate.HQLStatement;
import it.eng.qbe.statement.hive.HiveQLDataSet;
import it.eng.qbe.statement.hive.HiveQLStatement;
import it.eng.qbe.statement.jpa.JPQLDataSet;
import it.eng.qbe.statement.jpa.JPQLStatement;
import it.eng.qbe.statement.sql.SQLDataSet;
import it.eng.qbe.statement.sql.SQLStatement;
import it.eng.spagobi.tools.dataset.bo.IDataSet;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class QbeDatasetFactory {
	public static IDataSet createDataSet(IStatement statement) {
		IDataSet dataSet;

		dataSet = null;
		if(statement instanceof HQLStatement) {
			dataSet = new HQLDataSet( (HQLStatement)statement );
		} else if(statement instanceof JPQLStatement) {
			dataSet = new JPQLDataSet( (JPQLStatement)statement );
		}else if(statement instanceof HiveQLStatement) {
			dataSet = new HiveQLDataSet( (HiveQLStatement)statement );
		} else if(statement instanceof SQLStatement) {
			dataSet = new SQLDataSet( (SQLStatement)statement );
		} else {
			throw new RuntimeException("Impossible to create dataset from a statement of type [" + statement.getClass().getName() + "]");
		}

		return dataSet;
	}
}
