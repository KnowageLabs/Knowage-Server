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
