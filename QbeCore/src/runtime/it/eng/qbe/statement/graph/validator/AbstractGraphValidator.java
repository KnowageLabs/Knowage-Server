/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
