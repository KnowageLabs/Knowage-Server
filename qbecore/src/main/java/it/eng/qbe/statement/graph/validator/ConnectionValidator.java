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
import it.eng.qbe.model.structure.IModelField;

import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;

/**
 * This implementation of the Graph Validator check if the Graph is connected
 * 
 * 
 * @authors
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */

public class ConnectionValidator extends AbstractGraphValidator {

	/**
	 * Check if the graph is connected
	 */
	public boolean validate(Graph G,  Set<IModelEntity> unjoinedEntities) {
		if(unjoinedEntities==null || unjoinedEntities.size()<2){
			return true;
		}
		/**
		 * Temporary fix to avoid validation of entities handling spatial data
		 * TODO Find a better way to check this
		 */
		for (IModelEntity iModelEntity : unjoinedEntities) {
			for (IModelField iModelField : iModelEntity.getAllFields()) {
				if(iModelEntity!=null && iModelField.getType()!=null && iModelField.getType().toLowerCase().indexOf("geometry")!=-1)
					return true;
			}
		}
		if(G==null){
			return false;
		}
		
		if(G!=null && G.vertexSet()!=null && unjoinedEntities.size()>G.vertexSet().size()){
			return false;
		}
		
			
		Set<IModelEntity> vertex = G.vertexSet();
		
		if(vertex==null || vertex.size()<2){
			return true;
		}
		

		ConnectivityInspector inspector = null;
		if(G instanceof DirectedGraph){
			inspector = new ConnectivityInspector((DirectedGraph)G);
		} else if(G instanceof UndirectedGraph){
			inspector = new ConnectivityInspector((UndirectedGraph)G);
		}
		
		return inspector.isGraphConnected() ;
	}

}
