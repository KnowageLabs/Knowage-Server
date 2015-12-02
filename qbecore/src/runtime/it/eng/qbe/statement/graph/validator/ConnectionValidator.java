/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
				if(iModelField.getType().toLowerCase().indexOf("geometry")!=-1)
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
