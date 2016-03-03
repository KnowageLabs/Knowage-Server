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
package it.eng.qbe.statement.graph.bean;

import it.eng.qbe.model.structure.IModelEntity;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.graph.DirectedMultigraph;

/**
 * 
 * A Graph where the nodes are the entities involved in the query
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */


public class QueryGraph extends DirectedMultigraph<IModelEntity, Relationship> {


	private static final long serialVersionUID = 4245741171067061646L;
	
	private List<Relationship> connections;
	
	public QueryGraph(Class<? extends Relationship> edgeClass) {
		super(edgeClass);
		connections = new ArrayList<Relationship>();
	}
	
	@Override
	public boolean addEdge(IModelEntity sourceVertex, IModelEntity targetVertex, Relationship e) {
		boolean added = super.addEdge(sourceVertex, targetVertex, e);
		if(added){
			connections.add(e);
		}
		return added;
	}

	public List<Relationship> getConnections() {
		return connections;
	}



}
