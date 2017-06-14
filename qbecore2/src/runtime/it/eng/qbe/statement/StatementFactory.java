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

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.datasource.dataset.DataSetDataSource;
import it.eng.qbe.datasource.hibernate.IHibernateDataSource;
import it.eng.qbe.datasource.jpa.JPADataSource;
import it.eng.qbe.query.Query;
import it.eng.qbe.statement.hibernate.HQLStatement;
import it.eng.qbe.statement.jpa.JPQLStatement;

import java.lang.reflect.Constructor;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class StatementFactory {
	
	
	public static IStatement createStatement(IDataSource dataSource, Query query) {
		IStatement statement;
		
		statement = null;
		
		if(dataSource instanceof IHibernateDataSource) {
			statement = new HQLStatement((IHibernateDataSource)dataSource, query);
		} else if (dataSource instanceof JPADataSource) {
			statement = new JPQLStatement((JPADataSource)dataSource, query);
		} else if (dataSource instanceof DataSetDataSource) {
			DataSetDataSource ds = (DataSetDataSource)dataSource;
			Constructor c = null;
			Object object = null;
			try{
				c = ds.getStatementType().getConstructor(IDataSource.class, Query.class);
				object = c.newInstance( (DataSetDataSource)dataSource, query);
				statement = (IStatement) object;
				//statement = new SQLStatement((DataSetDataSource)dataSource, query);
			}catch(Exception e){
				throw new RuntimeException("Impossible to create statement from a datasource of type DataSetDataSource [" + ds.getStatementType() + "]");
			}
				
		}else {
			throw new RuntimeException("Impossible to create statement from a datasource of type [" + dataSource.getClass().getName() + "]");
		}
		
		return statement;
	}
}
