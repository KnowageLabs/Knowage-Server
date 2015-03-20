/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
