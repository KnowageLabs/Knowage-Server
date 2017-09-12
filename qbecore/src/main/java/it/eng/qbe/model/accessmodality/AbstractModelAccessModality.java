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
package it.eng.qbe.model.accessmodality;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class AbstractModelAccessModality implements IModelAccessModality {

	Boolean recursiveFiltering = Boolean.TRUE;

	public static final String ATTR_RECURSIVE_FILTERING = "recursiveFiltering";

	public boolean isEntityAccessible(IModelEntity entity) {
		return true;
	}

	public boolean isFieldAccessible(IModelField field) {
		return true;
	}

	public List getEntityFilterConditions(String entityName) {
		return new ArrayList();
	}

	public List getEntityFilterConditions(String entityName, Properties parameters) {
		return new ArrayList();
	}

	public Boolean getRecursiveFiltering() {
		return recursiveFiltering;
	}

	public void setRecursiveFiltering(Boolean recursiveFiltering) {
		this.recursiveFiltering = recursiveFiltering;
	}

	public Query getFilteredStatement(Query query, IDataSource iDataSource, Map userProfileAttributes) {
		return query;
	}

}
