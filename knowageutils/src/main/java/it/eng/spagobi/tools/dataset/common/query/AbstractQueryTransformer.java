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
package it.eng.spagobi.tools.dataset.common.query;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public abstract class AbstractQueryTransformer implements IQueryTransformer {
	private IQueryTransformer previousTransformer;
	
	public AbstractQueryTransformer() {
		this(null);
	}
	
	public AbstractQueryTransformer(IQueryTransformer previousTransformer) {
		setPreviousTransformer( previousTransformer );
	}

	public IQueryTransformer getPreviousTransformer() {
		return previousTransformer;
	}

	public void setPreviousTransformer(IQueryTransformer previousTransformer) {
		this.previousTransformer = previousTransformer;
	}
	
	public boolean hasPreviousTransformer() {
		return  getPreviousTransformer() != null;
	}


	public Object transformQuery(Object query) {
		if( hasPreviousTransformer() ) {
			query = getPreviousTransformer().transformQuery(query);
		}
		return execTransformation(query);
	}
	
	public abstract Object execTransformation(Object query);
}
