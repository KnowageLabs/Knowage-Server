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
package it.eng.qbe.statement.graph.validator;

import it.eng.qbe.model.structure.IModelEntity;

import java.util.Set;

import org.jgrapht.Graph;

public abstract class AbstractGraphValidator implements IGraphValidator {

	
	public boolean isValid(Graph G, Set<IModelEntity> unjoinedEntities){

		
		// the empty graph is always valid
		if((unjoinedEntities==null || unjoinedEntities.size()==0) && (G==null || G.vertexSet().size()==0)){
			return true;
		}
		
		return validate(G, unjoinedEntities);
		
	}

	public abstract boolean validate(Graph G, Set<IModelEntity> unjoinedEntities);
	
}
