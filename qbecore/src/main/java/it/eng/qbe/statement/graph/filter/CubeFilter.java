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
package it.eng.qbe.statement.graph.filter;

import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.statement.graph.ModelFieldPaths;
import it.eng.qbe.statement.graph.bean.PathChoice;
import it.eng.qbe.statement.graph.bean.Relationship;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * 
 * A path filter that remove all the paths containing cubes not involved in the query (for example a path that pass for a cube where no field is selected in the query) 
 * 
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class CubeFilter implements IPathsFilter{
	
	public static transient Logger logger = Logger.getLogger(CubeFilter.class);
	public static String PROPERTY_ENTITIES = "PROPERTY_ENTITIES";
	public static String PROPERTY_MODEL_STRUCTURE = "PROPERTY_ROOT_GRAPH";

	public void filterPaths(Set<ModelFieldPaths> paths,	Map<String, Object> properties) {
		
		Set<IModelEntity> modelEntities = (Set<IModelEntity>)properties.get(PROPERTY_ENTITIES);
		Assert.assertNotNull(modelEntities, "For the cube validator need a list of entities");
		
		IModelStructure modelStructure = (IModelStructure)properties.get(PROPERTY_MODEL_STRUCTURE);
		Assert.assertNotNull(modelStructure, "For the cube validator need the model structure");
		
		//Get all the cubes of the model
		List<String> modelEntityCubesList = new ArrayList<String>();
		Iterator<IModelEntity> entitiesIter = modelEntities.iterator();
		while (entitiesIter.hasNext()) {
			IModelEntity iModelEntity = (IModelEntity) entitiesIter.next();
			String type = (String) iModelEntity.getProperty("type");
			if ("cube".equalsIgnoreCase( type )) {
				modelEntityCubesList.add(iModelEntity.getUniqueName());
			}
		}
		
		logger.debug("Filtering the paths");
		Iterator<ModelFieldPaths> pathIter = paths.iterator();
		while (pathIter.hasNext()) {
			ModelFieldPaths modelFieldPaths = (ModelFieldPaths) pathIter.next();
			Set<PathChoice> choices = modelFieldPaths.getChoices();
			Set<PathChoice> filteredChoices = new HashSet<PathChoice>();
			if(choices!=null){
				Iterator<PathChoice> choicesIter = choices.iterator();
				while (choicesIter.hasNext()) {
					PathChoice pathChoice = (PathChoice) choicesIter.next();
					List<Relationship> relations = pathChoice.getRelations();
					boolean choiceToFilter=false;
					for(int i=0; i<relations.size(); i++){
						Relationship r = relations.get(i);
						IModelEntity source = r.getSourceEntity();
						IModelEntity target = r.getTargetEntity();
						if(isAnotherCube(modelStructure, source.getUniqueName(), modelEntityCubesList) || isAnotherCube(modelStructure, target.getUniqueName(), modelEntityCubesList)){
							choiceToFilter = true;
							logger.debug("Filtered a path where a node is a cube ["+source.getName()+" or "+target.getName()+"]");
						}
					}
					if(!choiceToFilter){
						filteredChoices.add(pathChoice);
					}
				}
			}
			modelFieldPaths.setChoices(filteredChoices);
		}
		logger.debug("Finish to filter paths");
	}

		
	private boolean isAnotherCube(IModelStructure modelStructure, String entityUniqueName, List<String> modelEntityCubesList){
		IModelEntity me = modelStructure.getEntity(entityUniqueName);
		String type = (String) me.getProperty("type");
		if ("cube".equalsIgnoreCase( type )) {
			return !modelEntityCubesList.contains(entityUniqueName);
		}
		
		return false;
	}
	
	
	
}
