/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.qbe.statement.graph.validator;

import it.eng.qbe.model.structure.IModelEntity;

import java.util.Set;

import org.jgrapht.Graph;
/**
 * This is the interface of the graph validators. In general a graph validator
 * should check if a Graph represent a valid query
 * 
 * 
 * @authors
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
import org.jgrapht.UndirectedGraph;

public interface IGraphValidator {

	/**
	 * Check if the graph G is valid
	 * @param G
	 * @param unjoinedEntities
	 * @return
	 */
	public boolean isValid(Graph G, Set<IModelEntity> unjoinedEntities);

}
