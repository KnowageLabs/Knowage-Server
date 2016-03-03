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
