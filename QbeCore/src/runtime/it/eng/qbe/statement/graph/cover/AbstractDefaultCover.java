/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.statement.graph.cover;

import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.statement.graph.GraphUtilities;
import it.eng.qbe.statement.graph.ModelFieldPaths;
import it.eng.qbe.statement.graph.bean.PathChoice;
import it.eng.qbe.statement.graph.bean.PathChoicePathLengthComparator;
import it.eng.qbe.statement.graph.bean.QueryGraph;
import it.eng.qbe.statement.graph.bean.Relationship;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;

public abstract class AbstractDefaultCover implements IDefaultCoverGraph {

	public void applyDefault(Set<ModelFieldPaths> ambiguousModelField,  Graph<IModelEntity, Relationship> rootEntitiesGraph, Set<IModelEntity> entities){
		Map<IModelEntity, Set<GraphPath<IModelEntity, Relationship>>> defaultConnections =   getConnectingRelatiosnhips(rootEntitiesGraph, entities);	
		applyDefault(defaultConnections, ambiguousModelField);
	}
	
	public void applyDefault(Set<ModelFieldPaths> ambiguousModelField,   QueryGraph monimumGraph, Set<IModelEntity> entities){
		Map<IModelEntity, Set<GraphPath<IModelEntity, Relationship>>> defaultConnections =   getConnectingRelatiosnhips( monimumGraph, entities);	
		applyDefault(defaultConnections, ambiguousModelField);
	}

	public void applyDefault(Map<IModelEntity, Set<GraphPath<IModelEntity, Relationship>>> defaultConnections, Set<ModelFieldPaths> ambiguousModelField){			
		applyDefault(defaultConnections, ambiguousModelField, false);
	}
	
	
	public void applyDefault(Map<IModelEntity, Set<GraphPath<IModelEntity, Relationship>>> defaultConnections, Set<ModelFieldPaths> ambiguousModelField, boolean withSubpaths){			
		if(ambiguousModelField!=null && defaultConnections!=null){
			Iterator<ModelFieldPaths> mfpIter = ambiguousModelField.iterator();
			//for each ambiguous field
			while (mfpIter.hasNext()) {
				ModelFieldPaths modelFieldPaths = (ModelFieldPaths) mfpIter.next();
				IModelEntity entity = modelFieldPaths.getModelEntity();
				if(modelFieldPaths.getChoices()!=null){
					
					Iterator<PathChoice> amfpChoicesIter =modelFieldPaths.getChoices().iterator();
					List<PathChoice> activePaths = new ArrayList<PathChoice>();
					
					//iterate on the choices to active them
					while (amfpChoicesIter.hasNext()) {
						PathChoice pathChoice = (PathChoice) amfpChoicesIter.next();
						Set<GraphPath<IModelEntity, Relationship>> shortest = defaultConnections.get(entity);
						if(shortest!=null){
							Iterator<GraphPath<IModelEntity, Relationship>> pathIter = shortest.iterator();
							
							//check if the choice is contained in the shortestPath
							while (pathIter.hasNext()) {
								GraphPath<IModelEntity, Relationship> graphPath = (GraphPath<IModelEntity, Relationship>) pathIter.next();
								boolean activeChoice = pathChoice.isTheSamePath(graphPath);
								if(activeChoice){
									activePaths.add(pathChoice);
									break;
								}
							}
						}
					}
					
					PathChoicePathLengthComparator comparator = new PathChoicePathLengthComparator("ASC");
					Collections.sort(activePaths, comparator);
					
					//check if an active path is a subpath of other active paths
					for(int i=0; i<activePaths.size(); i++){
						boolean isSubpath = false;
						PathChoice firstPath = activePaths.get(i);
						for(int j=i+1; j<activePaths.size(); j++){
							if(GraphUtilities.isSubPath(firstPath, activePaths.get(j))){
								isSubpath = true;//if it's a sub active path we don't set it as active
								break;
							}
						}
						if(!isSubpath){
							firstPath.setActive(true);
						}
					}
					
				}
			}
		}
	}
	
	public abstract  Map<IModelEntity, Set<GraphPath<IModelEntity, Relationship>>>  getConnectingRelatiosnhips( QueryGraph monimumGraph, Set<IModelEntity> entities);
	
	public abstract Map<IModelEntity, Set<GraphPath<IModelEntity, Relationship>>>  getConnectingRelatiosnhips( Graph<IModelEntity, Relationship> rootEntitiesGraph, Set<IModelEntity> entities);
		
	public abstract QueryGraph  getCoverGraph( Graph<IModelEntity, Relationship> rootEntitiesGraph, Set<IModelEntity> entities);
	
	
	
}
